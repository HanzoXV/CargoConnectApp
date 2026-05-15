package com.example.cargoconnectapp.ui.models.map

import com.example.cargoconnectapp.R
import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.location.Geocoder
import android.os.Build
import android.util.Log
import android.view.MotionEvent
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
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.*
import org.osmdroid.bonuspack.routing.OSRMRoadManager
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
import com.example.cargoconnectapp.ui.models.viewmodels.CustomerViewModel
import com.example.cargoconnectapp.ui.models.viewmodels.DriverViewModel
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

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
    customerViewModel: CustomerViewModel? = null,
    driverId: Int = -1,
    trackDriver: Boolean = false
    // ─────────────────────────────────────────────────────────────────────────
) {
    val context = LocalContext.current
    org.osmdroid.config.Configuration.getInstance().userAgentValue = context.packageName

    val redDotIcon = remember(context) {
        ContextCompat.getDrawable(context, R.drawable.ic_red_dot)
    }

    val mapView = rememberMapViewWithLifecycle()

    // Only used in non-tracking mode
    var capturedUserLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var hasInitiallyCentered   by remember { mutableStateOf(false) }

    // Persistent truck marker reference for tracking mode
    val truckMarker = remember { mutableStateOf<Marker?>(null) }
    // Whether an animation is currently in flight (prevents stacking)
    val animating = remember { mutableStateOf(false) }

    var localInitialized by remember { mutableStateOf(false) }
    val initialized = driverViewModel?.hasMapBeenInitialized ?: localInitialized
    val currentCenter = driverViewModel?.lastMapCenter
    val currentZoom = driverViewModel?.lastMapZoom ?: 15.0


    DisposableEffect(mapView) {
        val listener = object : MapListener {
            override fun onScroll(event: ScrollEvent): Boolean {
                if (mapView.isAttachedToWindow && !animating.value) {
                    driverViewModel?.lastMapCenter = mapView.mapCenter as GeoPoint
                }
                return false
            }
            override fun onZoom(event: ZoomEvent): Boolean {
                if (mapView.isAttachedToWindow && !animating.value) {
                    driverViewModel?.lastMapZoom = mapView.zoomLevelDouble
                }
                return false
            }
        }
        mapView.addMapListener(listener)
        onDispose { mapView.removeMapListener(listener) }
    }
    //customerViewModel
    if (trackDriver) {
        // Auto-center map on first valid location
        LaunchedEffect(customerViewModel?.driverLocation) {
            val newLocation = customerViewModel?.driverLocation ?: return@LaunchedEffect

            val existingMarker = mapView.overlays
                .filterIsInstance<Marker>()
                .find { it.id == "driver_red_dot" }

            if (existingMarker != null) {
                existingMarker.position = newLocation
                mapView.overlays.remove(existingMarker)
                mapView.overlays.add(existingMarker)
            } else {
                val driverMarker = Marker(mapView).apply {
                    id = "driver_red_dot"
                    position = newLocation
                    icon = redDotIcon
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                }
                mapView.overlays.add(driverMarker)
            }

            mapView.controller.setZoom(16.0)
            mapView.controller.animateTo(newLocation)
            mapView.invalidate()
        }
    }

    LaunchedEffect(points.size, radiusInMeters, points.hashCode(), truckAtIndex) {

        if (points.isEmpty()) {
            mapView.overlays.removeAll { it is Polyline || it is Marker || it is Polygon }
            mapView.invalidate()
            return@LaunchedEffect
        }
        mapView.overlays.removeAll { overlay ->
            overlay is Polyline ||
                    (overlay is Marker &&
                            overlay.id != "truck_marker" &&
                            overlay.id != "user_location_overlay" &&
                            overlay.id != "driver_red_dot") ||
                    overlay is Polygon
        }

        if (points.isNotEmpty()) {
            if (isTrackingMode) {
                if (points.size >= 2) {
                    if (mapView.isAttachedToWindow) {
                        drawFullRoute(context, mapView, points)
                    }

                    if (truckAtIndex >= 0 && truckAtIndex < points.size) {
                        val pos = points[truckAtIndex]
                        val bearing = bearingBetween(pos, points.getOrNull(truckAtIndex + 1) ?: pos)

                        if (!animating.value) {
                            placeTruckAt(context, mapView, truckMarker, pos, bearing)
                        }
                    }
                }
                // ── Tracking mode: draw checkpoint pins only; no user-location route
                points.forEachIndexed { index, geoPoint ->
                    val marker = Marker(mapView).apply {
                        position = geoPoint
                        icon = ContextCompat.getDrawable(context, R.drawable.ic_red_pin)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = when (index) {
                            0 -> "Start"
                            points.lastIndex -> "End"
                            else -> "Stop $index"
                        }
                    }
                    mapView.overlays.add(marker)
                    truckMarker.value?.let { truck ->
                        mapView.overlays.remove(truck)
                        mapView.overlays.add(truck) // move to top
                    }// adds above route
                }

                // Zoom to fit entire route
                if (!initialized && currentCenter == null) {
                    zoomToFitPoints(mapView, points)
                    if (driverViewModel != null) {
                        driverViewModel.hasMapBeenInitialized = true
                    } else {
                        localInitialized = true
                    }

                    mapView.post {
                        if (mapView.isAttachedToWindow) {
                            driverViewModel?.lastMapCenter = mapView.mapCenter as GeoPoint
                            driverViewModel?.lastMapZoom = mapView.zoomLevelDouble
                        }
                    }
                } else if (currentCenter != null) {
                    mapView.controller.setCenter(currentCenter)
                    mapView.controller.setZoom(currentZoom)
                }
                truckMarker.value?.let { truck ->
                    mapView.overlays.remove(truck)
                    mapView.overlays.add(truck) // move to top
                }

                // If we have a valid index and we aren't currently animating, show the truck
                if (truckAtIndex >= 0 && truckAtIndex < points.size && !animating.value) {
                    val pos = points[truckAtIndex]
                    // Calculate bearing toward the NEXT stop so the truck faces the right way
                    val nextPos = points.getOrNull(truckAtIndex + 1) ?: pos
                    val bearing = bearingBetween(pos, nextPos)

                    // Small delay to ensure polyline overlays are ready so truck stays on top
                    delay(100)
                    placeTruckAt(context, mapView, truckMarker, pos, bearing)
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
    LaunchedEffect(points, truckAtIndex) {
        if (isTrackingMode && points.isNotEmpty()) {
            //truckAtIndex If animating, we could pass the current interpolated point.
            // For now, let's use the stop the truck is currently at:
            val currentTruckPos = if (truckAtIndex >= 0) points[truckAtIndex] else null

            currentTruckPos?.let { pos ->
                updateDistancesLocally(pos, points, onDistancesCalculated)
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
        if (animating.value) return@LaunchedEffect
        if (!initialized) return@LaunchedEffect

        animating.value = true

        val from = truckAnimationEvent.from
        val to = truckAnimationEvent.to
        val duration = truckAnimationEvent.durationMs
        val steps = 60
        val delay = duration / steps

        // Capture points NOW before they can be cleared by confirmCheckpoint
        val capturedPoints = points.toList()

        val roadPoints: List<GeoPoint> = withContext(Dispatchers.IO) {
            try {
                val rm = OSRMRoadManager(context, context.packageName)
                val road = rm.getRoad(arrayListOf(from, to))
                road.mRouteHigh.ifEmpty { listOf(from, to) }
            } catch (e: Exception) {
                listOf(from, to)
            }
        }

        mapView.controller.setZoom(16.0)
        mapView.controller.setCenter(from)

        val totalPoints = roadPoints.size
        for (step in 0..steps) {
            val fraction = step.toFloat() / steps
            val pathIndex = (fraction * (totalPoints - 1)).toInt().coerceIn(0, totalPoints - 2)
            val segFrac = fraction * (totalPoints - 1) - pathIndex
            val current = interpolate(roadPoints[pathIndex], roadPoints[pathIndex + 1], segFrac.toDouble())
            val nextPt = if (pathIndex + 1 < totalPoints) roadPoints[pathIndex + 1] else to
            val currentBearing = bearingBetween(current, nextPt)

            withContext(Dispatchers.Main) {
                placeTruckAt(context, mapView, truckMarker, current, currentBearing)

                if (step % 10 == 0) {
                    updateDistancesLocally(current, points, onDistancesCalculated)
                    driverViewModel?.updateDriverLocation(driverId, current.latitude, current.longitude)
                }
                mapView.controller.setZoom(16.0)
                mapView.controller.setCenter(current)
            }
            delay(delay)
        }

        // Use capturedPoints here — safe even if activeRoute was cleared
        withContext(Dispatchers.Main) {
            val finalBearing = bearingBetween(
                roadPoints.getOrElse(roadPoints.size - 2) { from }, to
            )
            placeTruckAt(context, mapView, truckMarker, to, finalBearing)
            mapView.mapOrientation = 0f
            delay(800)

            driverViewModel?.lastMapCenter = to
            driverViewModel?.lastMapZoom = 16.0

            // Only zoom if we still have points — use captured snapshot
            /*if (capturedPoints.size >= 2) {
                zoomToFitPoints(mapView, capturedPoints)
            }*/
        }

        animating.value = false

        // Fire confirm AFTER all map operations are done
        withContext(Dispatchers.Main) {
            driverViewModel?.isTruckAnimating = false
            val action = driverViewModel?.pendingConfirmAction
            driverViewModel?.pendingConfirmAction = null  // clear BEFORE invoking
            action?.invoke()  // this may clear activeRoute/points — safe now
        }
    }

    // ── AndroidView factory ───────────────────────────────────────────────────
    AndroidView(
        factory = {
            mapView.apply {
                overlays.add(MapEventsOverlay(object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                        onMapClick(p); return true
                    }

                    override fun longPressHelper(p: GeoPoint): Boolean {
                        onMapLongClick(p); return true
                    }
                }))

                if (!isTrackingMode) {
                    val locationOverlay =
                        MyLocationNewOverlay(GpsMyLocationProvider(context), this).apply {
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

                setOnTouchListener { v, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN,
                        MotionEvent.ACTION_MOVE -> v.parent.requestDisallowInterceptTouchEvent(true)

                        MotionEvent.ACTION_UP -> v.parent.requestDisallowInterceptTouchEvent(false)
                    }
                    v.performClick()
                    false
                }
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .pointerInteropFilter { false },
        update = { view ->
            // This runs whenever points or ViewModel state changes.
            // If initialized and not currently animating, snap to the saved state.
            if (initialized && currentCenter != null && !animating.value) {
                view.controller.setCenter(currentCenter)
                view.controller.setZoom(currentZoom)
            }
        },
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

        mapView.overlays.remove(existingMarker)
        mapView.overlays.add(existingMarker)
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
    if (mapView == null || points.size < 2) return

    // 1. Create Polyline IMMEDIATELY on Main thread to avoid Repository NPE
    val polyline = Polyline(mapView).apply {
        infoWindow = null // Disable InfoWindow to skip the internal getRepository() call
        outlinePaint.color = "#6236B5".toColorInt()
        outlinePaint.strokeWidth = 12f
        outlinePaint.isAntiAlias = true
        outlinePaint.strokeJoin = android.graphics.Paint.Join.ROUND
        outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
    }

    val roadManager: RoadManager = OSRMRoadManager(context, context.packageName)

    // 2. Use a specific CoroutineScope that won't leak
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val road = roadManager.getRoad(ArrayList(points))

            withContext(Dispatchers.Main) {
                // Verify MapView is still valid/attached before updating
                if (road != null && road.mRouteHigh.isNotEmpty() && mapView.isAttachedToWindow) {
                    polyline.setPoints(road.mRouteHigh)
                    mapView.overlays.add(0, polyline)
                    mapView.invalidate()
                }
            }
        } catch (e: Exception) {
            Log.e("MAP_DRAW", "Failed to draw route", e)
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
fun calculateDistancesFromTruck(
    context: Context,
    truckLocation: GeoPoint,
    points: List<GeoPoint>,
    onDistancesCalculated: (List<Double>) -> Unit
) {
    val roadManager: RoadManager = OSRMRoadManager(context, context.packageName)
    CoroutineScope(Dispatchers.IO).launch {
        val distances = points.map { stop ->
            val waypoints = arrayListOf(truckLocation, stop)
            val road = roadManager.getRoad(waypoints)
            // road.mLength is in KM for OSRM
            road.mLength
        }
        withContext(Dispatchers.Main) { onDistancesCalculated(distances) }
    }
}
fun calculateHaversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0 // Earth's radius in kilometers
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}
fun updateDistancesLocally(
    truckLocation: GeoPoint,
    points: List<GeoPoint>,
    onDistancesCalculated: (List<Double>) -> Unit
) {
    val distances = points.map { stop ->
        calculateHaversine(
            truckLocation.latitude,
            truckLocation.longitude,
            stop.latitude,
            stop.longitude
        )
    }
    onDistancesCalculated(distances)
}