package com.example.cargoconnectapp.ui.models.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.cargoconnectapp.R
import com.example.cargoconnectapp.data.RetrofitClient
import com.example.cargoconnectapp.ui.models.datamodels.DriverLocationRequestDto
import kotlinx.coroutines.*
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.util.GeoPoint
import java.time.LocalDateTime
import kotlin.math.*

/**
 * Foreground service that drives the truck animation loop.
 *
 * Lifecycle:
 *  - startForegroundService() starts it and keeps it alive even when the app
 *    is backgrounded or the screen changes.
 *  - Binding (IBinder) lets the UI observe progress and subscribe to callbacks.
 *  - Call stopSelf() (via stopAnimation()) when the animation finishes or is
 *    cancelled.
 *
 * The service sends location updates to the API while animating so that the
 * customer's tracking screen stays in sync.
 */
@RequiresApi(Build.VERSION_CODES.O)
class TruckAnimationService : Service() {

    // ── Binder ────────────────────────────────────────────────────────────────

    inner class LocalBinder : Binder() {
        fun getService(): TruckAnimationService = this@TruckAnimationService
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent): IBinder = binder

    // ── State exposed to the UI ───────────────────────────────────────────────


    /** Current interpolated position of the truck (updated every frame). */
    var currentPosition: GeoPoint? = null
        private set

    /** Current bearing so the UI can rotate the truck icon correctly. */
    var currentBearing: Double = 0.0
        private set

    /** Progress 0.0 → 1.0. */
    var progress: Float = 0f
        private set

    /** True while the animation coroutine is running. */
    var isAnimating: Boolean = false
        private set

    // ── Callbacks (set by the UI after binding) ───────────────────────────────

    /**
     * Called on every animation step so the composable can redraw the truck.
     * Runs on the Main dispatcher.
     */
    var onPositionChanged: ((position: GeoPoint, bearing: Double, progress: Float) -> Unit)? = null

    /**
     * Called once when the animation finishes (truck reached destination).
     * Runs on the Main dispatcher.
     */
    var onAnimationComplete: (() -> Unit)? = null  // for UI (navigation)
    var checkpointId: Int = -1                     // set before startAnimation()
    var driverIdForConfirm: Int = -1

    // ── Private ───────────────────────────────────────────────────────────────

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var animationJob: Job? = null

    // Parameters handed in via startAnimation()
    private var driverId: Int = -1
    private var fromPoint: GeoPoint? = null
    private var toPoint: GeoPoint? = null
    private var durationMs: Long = 15_000L

    // ── Service lifecycle ─────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Promote to foreground immediately so Android won't kill us
        startForeground(NOTIFICATION_ID, buildNotification("Truck is on its way…"))
        return START_STICKY   // restart automatically if the process is killed
    }

    override fun onDestroy() {
        animationJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    // ── Public API (called via the binder from DriverViewModel) ───────────────

    /**
     * Begin animating the truck from [from] to [to].
     * Safe to call multiple times; any running animation is cancelled first.
     */
    fun startAnimation(
        driverId: Int,
        from: GeoPoint,
        to: GeoPoint,
        durationMs: Long = 15_000L
    ) {
        this.driverId   = driverId
        this.fromPoint  = from
        this.toPoint    = to
        this.durationMs = durationMs

        animationJob?.cancel()
        animationJob = serviceScope.launch { runAnimation(from, to, durationMs) }
    }

    /** Cancel any running animation and stop the service. */
    fun stopAnimation() {
        animationJob?.cancel()
        stopSelf()
    }

    // ── Animation coroutine ───────────────────────────────────────────────────

    private suspend fun CoroutineScope.runAnimation(from: GeoPoint, to: GeoPoint, durationMs: Long) {
        isAnimating = true
        updateNotification("Truck is on its way…")

        // Fetch road geometry on IO so we get a realistic path
        val roadPoints: List<GeoPoint> = withContext(Dispatchers.IO) {
            try {
                val rm = OSRMRoadManager(applicationContext, packageName).apply {
                    setMean(OSRMRoadManager.MEAN_BY_CAR)
                    setService("https://router.project-osrm.org/")
                }
                val road = rm.getRoad(arrayListOf(from, to))
                road.mRouteHigh.ifEmpty { listOf(from, to) }
            } catch (e: Exception) {
                Log.w(TAG, "OSRM failed, falling back to straight line: ${e.message}")
                listOf(from, to)
            }
        }

        val totalPoints = roadPoints.size
        val steps       = 120                        // smooth enough, not too heavy
        val stepDelay   = durationMs / steps

        for (step in 0..steps) {
            if (!isActive) break                     // check for cancellation

            val fraction  = step.toFloat() / steps
            val pathIndex = (fraction * (totalPoints - 1)).toInt().coerceIn(0, totalPoints - 2)
            val segFrac   = fraction * (totalPoints - 1) - pathIndex

            val current  = interpolate(roadPoints[pathIndex], roadPoints[pathIndex + 1], segFrac.toDouble())
            val nextPt   = roadPoints.getOrNull(pathIndex + 1) ?: to
            val bearing  = bearingBetween(current, nextPt)

            // Store for binding observers
            currentPosition = current
            currentBearing  = bearing
            progress        = fraction

            // Notify UI (on Main)
            withContext(Dispatchers.Main) {
                onPositionChanged?.invoke(current, bearing, fraction)
            }

            // Push location to backend every ~10 steps (~1.25 s at default speed)
            if (step % 10 == 0) {
                pushLocationToApi(current)
            }

            delay(stepDelay)
        }

        // 1. Snap truck to final position visually
        withContext(Dispatchers.Main) {
            currentPosition = to
            progress        = 1f
            isAnimating     = false
            onPositionChanged?.invoke(to, currentBearing, 1f)
        }

        updateNotification("Delivery complete!")

        // 2. Hit the API (always runs, even if UI is gone)
        if (checkpointId != -1) {
            confirmCheckpointInService(checkpointId, driverIdForConfirm)
        }

        // 3. Tell UI to navigate (only runs if UI is still attached)
        withContext(Dispatchers.Main) {
            onAnimationComplete?.invoke()
        }

        delay(2_000)
        stopSelf()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private suspend fun pushLocationToApi(position: GeoPoint) = withContext(Dispatchers.IO) {
        try {
            val request = DriverLocationRequestDto(
                driverId  = driverId,
                latitude  = position.latitude,
                longitude = position.longitude,
                updatedAt = LocalDateTime.now().toString()
            )
            RetrofitClient.instance.updateDriverLocation(request)
        } catch (e: Exception) {
            Log.e(TAG, "Location push failed: ${e.message}")
        }
    }

    private fun interpolate(a: GeoPoint, b: GeoPoint, t: Double) = GeoPoint(
        a.latitude  + (b.latitude  - a.latitude)  * t,
        a.longitude + (b.longitude - a.longitude) * t
    )

    private fun bearingBetween(from: GeoPoint, to: GeoPoint): Double {
        val lat1 = Math.toRadians(from.latitude)
        val lat2 = Math.toRadians(to.latitude)
        val dLon = Math.toRadians(to.longitude - from.longitude)
        val y    = sin(dLon) * cos(lat2)
        val x    = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        return (Math.toDegrees(atan2(y, x)) + 360) % 360
    }

    // ── Notification helpers ──────────────────────────────────────────────────

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Truck Delivery",
                NotificationManager.IMPORTANCE_LOW          // silent; no heads-up
            ).apply { description = "Live truck delivery tracking" }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }
    private suspend fun confirmCheckpointInService(checkpointId: Int, driverId: Int) {
        withContext(Dispatchers.IO) {
            try {
                RetrofitClient.instance.confirmCheckpointReached(checkpointId, driverId)
            } catch (e: Exception) {
                Log.e(TAG, "confirmCheckpoint failed: ${e.message}")
            }
        }
    }

    private fun buildNotification(text: String): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("CargoConnect – Delivery in progress")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_truck_marker)       // use any icon you have
            .setOngoing(true)                               // can't be swiped away
            .setSilent(true)
            .build()

    private fun updateNotification(text: String) {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, buildNotification(text))
    }

    companion object {
        private const val TAG            = "TruckAnimationService"
        private const val CHANNEL_ID     = "truck_delivery_channel"
        private const val NOTIFICATION_ID = 1001
    }
}