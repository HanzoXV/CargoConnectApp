package com.example.cargoconnectapp.ui.models.map

import com.example.cargoconnectapp.R
import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.location.Geocoder
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.*
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Locale
import androidx.core.graphics.toColorInt
import com.example.cargoconnectapp.ui.models.viewmodels.DriverViewModel
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

// ─────────────────────────────────────────────────────────────────────────────
// Data class to trigger a truck animation from the dashboard
// ─────────────────────────────────────────────────────────────────────────────

data class TruckAnimationEvent(
    val from: GeoPoint,
    val to: GeoPoint,
    val durationMs: Long = 15_000L,
    val id: Long = System.currentTimeMillis() // unique per press so LaunchedEffect re-triggers
)

// ─────────────────────────────────────────────────────────────────────────────
// Lifecycle-aware MapView
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            setMultiTouchControls(true)
            setHorizontalMapRepetitionEnabled(false)
            setVerticalMapRepetitionEnabled(false)
            minZoomLevel = 4.0
            maxZoomLevel = 20.0
        }
    }
    val lifecycleObserver = remember {
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE  -> mapView.onPause()
                else -> {}
            }
        }
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        lifecycle.addObserver(lifecycleObserver)
        onDispose { lifecycle.removeObserver(lifecycleObserver) }
    }
    return mapView
}

// ─────────────────────────────────────────────────────────────────────────────
// OSMContainer
// ─────────────────────────────────────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.O)
@RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
@Composable
fun OSMContainer(
    points: List<GeoPoint>,
    routeFromUser: Boolean = false,
    radiusInMeters: Double? = null,
    circleCenter: GeoPoint? = null,
    onDistancesCalculated: (List<Double>) -> Unit = {},
    onMapClick: (GeoPoint) -> Unit = {},
    onMapLongClick: (GeoPoint) -> Unit = {},
    isNavigation: Boolean = false,

    // ── NEW tracking-mode params ──────────────────────────────────────────────
    isTrackingMode: Boolean = false,
    // The index (into `points`) where the truck currently sits after all
    // "reached" presses so far. -1 = not started yet (truck hidden).
    truckAtIndex: Int = -1,
    // Set this to a new TruckAnimationEvent every time "Reached" is pressed.
    truckAnimationEvent: TruckAnimationEvent? = null,
    driverViewModel: DriverViewModel? = null,
    driverId: Int = -1
    // ─────────────────────────────────────────────────────────────────────────
) {
    val context = LocalContext.current
    org.osmdroid.config.Configuration.getInstance().userAgentValue = context.packageName

    val mapView = rememberMapViewWithLifecycle()

    // Only used in non-tracking mode
    var capturedUserLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var hasInitiallyCentered   by remember { mutableStateOf(false) }

    // Persistent truck marker reference for tracking mode
    val truckMarker = remember { mutableStateOf<Marker?>(null) }
    // Whether an animation is currently in flight (prevents stacking)
    val animating = remember { mutableStateOf(false) }

    // ── Effect 1: Redraw route, checkpoint markers, optional circle ───────────
    LaunchedEffect(points, capturedUserLocation, radiusInMeters, circleCenter, isTrackingMode) {

        // Remove previous route / checkpoint markers / circles (keep truck marker)
        mapView.overlays.removeAll { overlay ->
            overlay is Polyline ||
                    (overlay is Marker && overlay.id != "truck_marker" && overlay.id != "user_location_overlay") ||
                    overlay is Polygon
        }

        if (points.isNotEmpty()) {
            if (isTrackingMode) {
                // ── Tracking mode: draw checkpoint pins only; no user-location route
                points.forEachIndexed { index, geoPoint ->
                    val marker = Marker(mapView).apply {
                        position  = geoPoint
                        icon      = ContextCompat.getDrawable(context, R.drawable.ic_red_pin)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title     = when (index) {
                            0               -> "Start"
                            points.lastIndex -> "End"
                            else            -> "Stop $index"
                        }
                    }
                    mapView.overlays.add(marker)
                }

                // Draw the full checkpoint-to-checkpoint route
                if (points.size >= 2) {
                    drawFullRoute(context, mapView, points)
                }

                // Zoom to fit entire route
                zoomToFitPoints(mapView, points)

                // Place truck at resting position if no animation event yet
                if (truckAtIndex >= 0 && truckAtIndex < points.size && !animating.value) {
                    val pos = points[truckAtIndex]
                    placeTruckAt(context, mapView, truckMarker, pos, bearingBetween(pos, points.getOrNull(truckAtIndex + 1) ?: pos))
                }

            } else {
                // ── Normal / navigation mode (unchanged behaviour) ─────────────
                val userLoc = capturedUserLocation
                points.forEachIndexed { index, geoPoint ->
                    val marker = Marker(mapView).apply {
                        position  = geoPoint
                        icon      = ContextCompat.getDrawable(context, R.drawable.ic_red_pin)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title     = when (index) {
                            0               -> "Start"
                            points.lastIndex -> "End"
                            else            -> "Stop $index"
                        }
                    }
                    mapView.overlays.add(marker)
                }

                if (isNavigation && userLoc != null) {
                    val pointsList = points.toMutableList().also { it.add(0, userLoc) }
                    drawFullRoute(context, mapView, pointsList)
                } else {
                    val waypoints = if (routeFromUser && userLoc != null) listOf(userLoc) + points else points
                    if (waypoints.size >= 2) drawFullRoute(context, mapView, waypoints)
                }
            }
        }

        // Circle overlay (both modes)
        if (circleCenter != null && radiusInMeters != null) {
            val circle  = Polygon.pointsAsCircle(circleCenter, radiusInMeters)
            val polygon = Polygon(mapView)
            polygon.points = circle
            polygon.fillPaint.color    = 0x330000FF.toInt()
            polygon.outlinePaint.color = 0xFF0000FF.toInt()
            polygon.outlinePaint.strokeWidth = 2f
            mapView.overlays.add(polygon)
        }

        mapView.invalidate()
    }

    // ── Effect 2: Distances (non-tracking only) ───────────────────────────────
    LaunchedEffect(capturedUserLocation, points) {
        if (!isTrackingMode) {
            capturedUserLocation?.let { loc ->
                if (points.isNotEmpty()) {
                    calculateDistancesFromUser(context, loc, points, onDistancesCalculated)
                }
            }
        }
    }

    // ── Effect 3: First-center on user location (non-tracking only) ───────────
    LaunchedEffect(capturedUserLocation) {
        if (!isTrackingMode && capturedUserLocation != null && !hasInitiallyCentered) {
            mapView.controller.setZoom(17.0)
            mapView.controller.animateTo(capturedUserLocation)
            hasInitiallyCentered = true
        }
    }

    // ── Effect 4: Truck animation (tracking mode only) ────────────────────────
    LaunchedEffect(truckAnimationEvent) {
        if (!isTrackingMode || truckAnimationEvent == null) return@LaunchedEffect
        if (animating.value) return@LaunchedEffect          // skip if already running

        animating.value = true

        val from     = truckAnimationEvent.from
        val to       = truckAnimationEvent.to
        val duration = truckAnimationEvent.durationMs
        val steps    = 120                                   // animation frame count
        val delay    = duration / steps

        // Fetch the real road path so the truck follows the road
        val roadPoints: List<GeoPoint> = withContext(Dispatchers.IO) {
            try {
                val rm  = OSRMRoadManager(context, context.packageName)
                val road = rm.getRoad(arrayListOf(from, to))
                road.mRouteHigh.ifEmpty { listOf(from, to) }
            } catch (e: Exception) {
                listOf(from, to)
            }
        }

        // Zoom + rotate the map toward the destination (navigation feel)
        //val bearing = bearingBetween(from, to)
        //mapView.mapOrientation = -bearing.toFloat()         // rotate map so "up" is forward
        mapView.controller.setZoom(16.0)
        mapView.controller.animateTo(from)

        // Animate truck along road path
        val totalPoints = roadPoints.size
        for (step in 0..steps) {
            val fraction  = step.toFloat() / steps
            val pathIndex = (fraction * (totalPoints - 1)).toInt().coerceIn(0, totalPoints - 2)
            val segFrac   = fraction * (totalPoints - 1) - pathIndex
            val current   = interpolate(roadPoints[pathIndex], roadPoints[pathIndex + 1], segFrac.toDouble())
            val nextPt    = if (pathIndex + 1 < totalPoints) roadPoints[pathIndex + 1] else to
            val currentBearing = bearingBetween(current, nextPt)

            withContext(Dispatchers.Main) {
                placeTruckAt(context, mapView, truckMarker, current, currentBearing)

                if (step % 30 == 0) {
                    driverViewModel?.updateDriverLocation(driverId, current.latitude, current.longitude)
                }

                mapView.controller.animateTo(current)

                //mapView.mapOrientation = -currentBearing.toFloat()
            }
            delay(delay)
        }

        // Snap to final position, reset map rotation
        withContext(Dispatchers.Main) {
            val finalBearing = bearingBetween(
                roadPoints.getOrElse(roadPoints.size - 2) { from },
                to
            )
            placeTruckAt(context, mapView, truckMarker, to, finalBearing)

            mapView.mapOrientation = 0f                     // restore north-up after animation
            mapView.controller.setZoom(15.0)
            zoomToFitPoints(mapView, points)
        }

        animating.value = false
    }

    // ── AndroidView factory ───────────────────────────────────────────────────
    AndroidView(
        factory = {
            mapView.apply {
                overlays.add(MapEventsOverlay(object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean { onMapClick(p); return true }
                    override fun longPressHelper(p: GeoPoint): Boolean { onMapLongClick(p); return true }
                }))

                if (!isTrackingMode) {
                    val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this).apply {
                        enableMyLocation()
                        runOnFirstFix {
                            val location = myLocation
                            capturedUserLocation = location
                            (context as? android.app.Activity)?.runOnUiThread {
                                capturedUserLocation = location
                            }
                        }
                    }
                    overlays.add(locationOverlay)
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

/** Places (or moves) the persistent truck marker at [position] rotated by [bearing] degrees. */
private fun placeTruckAt(
    context: Context,
    mapView: MapView,
    markerState: androidx.compose.runtime.MutableState<Marker?>,
    position: GeoPoint,
    bearing: Double
) {
    val existingMarker = markerState.value

    // Build a rotated bitmap from the truck drawable
    val drawable = ContextCompat.getDrawable(context, R.drawable.ic_truck_marker)!!
    val baseBitmap = drawable.toBitmap(72, 72)
    val rotatedBitmap = rotateBitmap(baseBitmap, bearing.toFloat())
    val icon = android.graphics.drawable.BitmapDrawable(context.resources, rotatedBitmap)

    if (existingMarker != null) {
        existingMarker.position = position
        existingMarker.icon     = icon
    } else {
        val marker = Marker(mapView).apply {
            id        = "truck_marker"
            this.position  = position
            this.icon      = icon
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            title     = "Truck"
        }
        mapView.overlays.add(marker)
        markerState.value = marker
    }
    mapView.invalidate()
}

/** Rotates a Bitmap by [degrees] around its center. */
private fun rotateBitmap(source: Bitmap, degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
}

/** Linear interpolation between two GeoPoints. */
private fun interpolate(a: GeoPoint, b: GeoPoint, t: Double): GeoPoint {
    val lat = a.latitude  + (b.latitude  - a.latitude)  * t
    val lon = a.longitude + (b.longitude - a.longitude) * t
    return GeoPoint(lat, lon)
}

/** Returns the compass bearing (0–360°) from [from] to [to]. */
private fun bearingBetween(from: GeoPoint, to: GeoPoint): Double {
    val lat1 = Math.toRadians(from.latitude)
    val lat2 = Math.toRadians(to.latitude)
    val dLon = Math.toRadians(to.longitude - from.longitude)
    val y    = sin(dLon) * cos(lat2)
    val x    = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
    return (Math.toDegrees(atan2(y, x)) + 360) % 360
}

/** Zooms the map to fit all given points with padding. */
private fun zoomToFitPoints(mapView: MapView, points: List<GeoPoint>) {
    if (points.size < 2) {
        mapView.controller.setZoom(14.0)
        points.firstOrNull()?.let { mapView.controller.setCenter(it) }
        return
    }
    val box = BoundingBox.fromGeoPoints(points)
    mapView.post {
        mapView.zoomToBoundingBox(box, true, 120)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Unchanged helpers from original file
// ─────────────────────────────────────────────────────────────────────────────

fun drawFullRoute(context: Context, mapView: MapView, points: List<GeoPoint>) {
    val roadManager: RoadManager = OSRMRoadManager(context, context.packageName)
    CoroutineScope(Dispatchers.IO).launch {
        val waypoints = ArrayList(points)
        val road      = roadManager.getRoad(waypoints)
        withContext(Dispatchers.Main) {
            val roadOverlay = RoadManager.buildRoadOverlay(road)
            roadOverlay.outlinePaint.color       = "#6236B5".toColorInt()
            roadOverlay.outlinePaint.strokeWidth  = 12f
            mapView.overlays.add(roadOverlay)
            mapView.invalidate()
        }
    }
}

fun calculateDistancesFromUser(
    context: Context,
    userLocation: GeoPoint,
    points: List<GeoPoint>,
    onDistancesCalculated: (List<Double>) -> Unit
) {
    val roadManager: RoadManager = OSRMRoadManager(context, context.packageName)
    CoroutineScope(Dispatchers.IO).launch {
        val distances = points.map { stop ->
            val waypoints = arrayListOf(userLocation, stop)
            val road = roadManager.getRoad(waypoints)
            if (road.mLength > 5000) road.mLength / 1000.0 else road.mLength
        }
        withContext(Dispatchers.Main) { onDistancesCalculated(distances) }
    }
}

fun getAddressFromGeoPoint(context: Context, geoPoint: GeoPoint): String {
    val geocoder = Geocoder(context, Locale.getDefault())
    return try {
        val addresses = geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)
        if (!addresses.isNullOrEmpty()) {
            val addr    = addresses[0]
            val subArea = addr.subLocality ?: addr.thoroughfare
            val mainCity = addr.locality   ?: addr.subAdminArea
            when {
                !subArea.isNullOrBlank() && !mainCity.isNullOrBlank() && subArea != mainCity -> "$subArea, $mainCity"
                !mainCity.isNullOrBlank() -> mainCity
                !subArea.isNullOrBlank()  -> subArea
                else -> {
                    val fallback = addr.getAddressLine(0)?.split(",")?.get(0) ?: ""
                    if (fallback.contains("+")) addr.adminArea ?: "Point on Map" else fallback
                }
            }
        } else "Location Not Found"
    } catch (e: Exception) { "Service Unavailable" }
}