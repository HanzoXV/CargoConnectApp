package com.example.cargoconnectapp.ui.models.map

import com.example.cargoconnectapp.R
import android.Manifest
import android.content.Context
import android.location.Geocoder
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Locale
import androidx.core.graphics.toColorInt
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Overlay

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
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
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

@RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
@Composable
fun OSMContainer(
    points: List<GeoPoint>,
    routeFromUser: Boolean = false,
    radiusInMeters: Double? = null,
    circleCenter: GeoPoint? = null,
    onDistancesCalculated: (List<Double>) -> Unit = {},
    onMapClick: (GeoPoint) -> Unit = {},
    onMapLongClick: (GeoPoint) -> Unit = {}
) {
    val context = LocalContext.current
    org.osmdroid.config.Configuration.getInstance().userAgentValue = context.packageName
    val mapView = rememberMapViewWithLifecycle()
    var capturedUserLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var hasInitiallyCentered by remember { mutableStateOf(false) }

    // Effect 1: Redraw Route & Markers & Circle
    LaunchedEffect(points, capturedUserLocation == null, radiusInMeters, circleCenter) {
        val userLoc = capturedUserLocation
        
        mapView.overlays.removeAll { overlay ->
            overlay is org.osmdroid.views.overlay.Polyline ||
                    (overlay is org.osmdroid.views.overlay.Marker && overlay.id != "user_location_overlay") ||
                    overlay is Polygon
        }

        if (points.isNotEmpty()) {
            points.forEachIndexed { index, geoPoint ->
                val marker = org.osmdroid.views.overlay.Marker(mapView).apply {
                    position = geoPoint
                    icon = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.ic_red_pin)
                    setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM)
                    title = if (index == 0) "Start" else if (index == points.size -1) "End" else "Stop $index"
                }
                mapView.overlays.add(marker)
            }

            val waypoints = if (routeFromUser && userLoc != null) arrayListOf(userLoc) + points else points
            if (waypoints.size >= 2) {
                drawFullRoute(context, mapView, waypoints as List<GeoPoint>)
            }
        }

        // Draw Circle if center and radius provided
        if (circleCenter != null && radiusInMeters != null) {
            val circle = Polygon.pointsAsCircle(circleCenter, radiusInMeters)
            val polygon = Polygon(mapView)
            polygon.points = circle
            polygon.fillPaint.color = 0x330000FF.toInt() // Transparent Blue
            polygon.outlinePaint.color = 0xFF0000FF.toInt() // Blue
            polygon.outlinePaint.strokeWidth = 2f
            mapView.overlays.add(polygon)
        }

        mapView.invalidate()
    }

    // Effect 2: Distances update
    LaunchedEffect(capturedUserLocation, points) {
        capturedUserLocation?.let { loc ->
            if (points.isNotEmpty()) {
                calculateDistancesFromUser(context, loc, points, onDistancesCalculated)
            }
        }
    }

    // Effect 3: Camera Center - Centering on User Location
    LaunchedEffect(capturedUserLocation) {
        if (capturedUserLocation != null && !hasInitiallyCentered) {
            mapView.controller.setZoom(17.0)
            mapView.controller.animateTo(capturedUserLocation)
            hasInitiallyCentered = true
        }
    }

    AndroidView(
        factory = {
            mapView.apply {
                overlays.add(MapEventsOverlay(object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean { onMapClick(p); return true }
                    override fun longPressHelper(p: GeoPoint): Boolean { onMapLongClick(p); return true }
                }))
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
        },
        modifier = Modifier.fillMaxSize()
    )
}

fun drawFullRoute(
    context: Context,
    mapView: MapView,
    points: List<GeoPoint>
) {
    val roadManager: RoadManager = OSRMRoadManager(context, context.packageName)

    CoroutineScope(Dispatchers.IO).launch {
        val waypoints = ArrayList(points)
        val road = roadManager.getRoad(waypoints)

        withContext(Dispatchers.Main) {
            val roadOverlay = RoadManager.buildRoadOverlay(road)
            roadOverlay.outlinePaint.color = "#6236B5".toColorInt()
            roadOverlay.outlinePaint.strokeWidth = 12f
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

        withContext(Dispatchers.Main) {
            onDistancesCalculated(distances)
        }
    }
}

fun getAddressFromGeoPoint(context: Context, geoPoint: GeoPoint): String {
    val geocoder = Geocoder(context, Locale.getDefault())
    return try {
        val addresses = geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)
        if (!addresses.isNullOrEmpty()) {
            val addr = addresses[0]
            val subArea = addr.subLocality ?: addr.thoroughfare
            val mainCity = addr.locality ?: addr.subAdminArea

            when {
                !subArea.isNullOrBlank() && !mainCity.isNullOrBlank() && subArea != mainCity -> "$subArea, $mainCity"
                !mainCity.isNullOrBlank() -> mainCity
                !subArea.isNullOrBlank() -> subArea
                else -> {
                    val fallback = addr.getAddressLine(0)?.split(",")?.get(0) ?: ""
                    if (fallback.contains("+")) addr.adminArea ?: "Point on Map" else fallback
                }
            }
        } else "Location Not Found"
    } catch (e: Exception) { "Service Unavailable" }
}
