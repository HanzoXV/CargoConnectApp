package com.example.cargoconnectapp.ui.models.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cargoconnectapp.data.RetrofitClient
import com.example.cargoconnectapp.ui.models.datamodels.*
import com.example.cargoconnectapp.ui.navigation.screens.ShipmentRequest
import com.example.cargoconnectapp.ui.models.helpermodels.Dimensions
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import java.time.LocalDate

class DriverViewModel : ViewModel() {

    // ── State ─────────────────────────────────────────────────────────────────

    var activeRoute by mutableStateOf<RouteData?>(null)
        private set
    var nextRoute by mutableStateOf<RouteData?>(null)
        private set
    var pastRoutes by mutableStateOf<List<RouteData>>(emptyList())
        private set

    var currentCapacity by mutableIntStateOf(0)
        private set
    var totalCapacity by mutableIntStateOf(0)
        private set

    var totalDimensions by mutableStateOf(Dimensions())
        private set
    var currentDimensions by mutableStateOf(Dimensions())
        private set

    var totalStops by mutableIntStateOf(0)
        private set
    var completedStops by mutableIntStateOf(0)
        private set

    val distances = mutableStateListOf<Int>()

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    var driverRequests by mutableStateOf<DriverRequestsResponse?>(null)
        private set

    var selectedRequest by mutableStateOf<ShipmentRequest?>(null)

    // ── Booking State ──
    var pendingBookings by mutableStateOf<List<BookingData>>(emptyList())
        private set
    var inTransitBookings by mutableStateOf<List<BookingData>>(emptyList())
        private set
    var completedBookings by mutableStateOf<List<BookingData>>(emptyList())
        private set
    var canceledBookings by mutableStateOf<List<BookingData>>(emptyList())
        private set

    var selectedBooking by mutableStateOf<BookingData?>(null)

    // ── Checkpoint Reached State ──
    var checkpointShipments by mutableStateOf<CheckpointShipmentsResponse?>(null)
        private set

    // ── Route Planning State (Temporary) ──────────────────────────────────────
    
    val plannedPoints = mutableStateListOf<GeoPoint>()
    var plannedDepartureDate by mutableStateOf<LocalDate?>(null)
    var plannedArrivalDate by mutableStateOf<LocalDate?>(null)
    
    // New Route Attributes
    var plannedIsFragile by mutableStateOf(false)
    var plannedIsLiquid by mutableStateOf(false)
    var plannedIsFlammable by mutableStateOf(false)
    var plannedKeepUpright by mutableStateOf(false)
    var plannedShipmentType by mutableStateOf("Shared") // "Full" or "Shared"

    // Stores a route template if we are activating an existing one
    var selectedTemplateRoute by mutableStateOf<RouteData?>(null)

    fun clearPlannedRoute() {
        plannedPoints.clear()
        plannedDepartureDate = null
        plannedArrivalDate = null
        selectedTemplateRoute = null
        plannedIsFragile = false
        plannedIsLiquid = false
        plannedIsFlammable = false
        plannedKeepUpright = false
        plannedShipmentType = "Shared"
    }

    fun clearAllData() {
        activeRoute = null
        nextRoute = null
        pastRoutes = emptyList()
        currentCapacity = 0
        totalCapacity = 0
        totalDimensions = Dimensions()
        currentDimensions = Dimensions()
        totalStops = 0
        completedStops = 0
        distances.clear()
        driverRequests = null
        clearPlannedRoute()
        errorMessage = null
        selectedRequest = null
        pendingBookings = emptyList()
        inTransitBookings = emptyList()
        completedBookings = emptyList()
        canceledBookings = emptyList()
        selectedBooking = null
        checkpointShipments = null
    }

    // ── Fetch functions ───────────────────────────────────────────────────────

    fun fetchActiveRoute(driverId: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.instance.getActiveRoute(driverId)
                if (response.isSuccessful) {
                    activeRoute = response.body()
                    totalStops = activeRoute?.checkpoints?.size ?: 0
                    completedStops = activeRoute?.checkpoints?.count { it.reached ?: false } ?: 0
                } else errorMessage = response.errorBody()?.string()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchNextRoute(driverId: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.instance.getNextRoute(driverId)
                if (response.isSuccessful) nextRoute = response.body()
                else errorMessage = response.errorBody()?.string()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchAllRoutes(driverId: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.instance.getRoutes(driverId)
                if (response.isSuccessful) {
                    val all = response.body() ?: emptyList()
                    // Filter and take 4 most recent
                    pastRoutes = all.filter {
                        it.isActive != true && it.isNextRoute != true
                    }.sortedByDescending { it.routeId }.take(4)
                } else errorMessage = response.errorBody()?.string()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun saveRoute(request: CreateRouteRequest, driverId: Int, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.instance.saveRoute(request)
                if (response.isSuccessful) {
                    clearPlannedRoute()
                    fetchActiveRoute(driverId)
                    fetchNextRoute(driverId)
                    fetchAllRoutes(driverId)
                    onComplete()
                } else errorMessage = response.errorBody()?.string()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun activateRoute(routeId: Int, driverId: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.instance.activateRoute(routeId)
                if (response.isSuccessful) fetchActiveRoute(driverId)
                else errorMessage = response.errorBody()?.string()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun scheduleNextRoute(routeId: Int, driverId: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.instance.scheduleNextRoute(routeId)
                if (response.isSuccessful) {
                    fetchNextRoute(driverId)
                } else errorMessage = response.errorBody()?.string()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteRoute(routeId: Int, driverId: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.instance.deleteRoute(routeId)
                if (response.isSuccessful) fetchAllRoutes(driverId)
                else errorMessage = response.errorBody()?.string()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchDriverRequests(driverId: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.instance.getDriverRequests(driverId)
                if (response.isSuccessful) {
                    driverRequests = response.body()
                } else {
                    errorMessage = response.errorBody()?.string()
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchAllBookings(driverId: Int) {
        viewModelScope.launch {
            isLoading = true
            Log.d("DriverViewModel", "Fetching all bookings for driver: $driverId")
            try {
                val p = async { RetrofitClient.instance.getConfirmedBookings(driverId) }
                val i = async { RetrofitClient.instance.getInTransitBookings(driverId) }
                val co = async { RetrofitClient.instance.getCompletedBookings(driverId) }
                val ca = async { RetrofitClient.instance.getCanceledBookings(driverId) }

                val resP = p.await()
                val resI = i.await()
                val resCo = co.await()
                val resCa = ca.await()

                if (resP.isSuccessful) pendingBookings = resP.body() ?: emptyList() else Log.e("DriverViewModel", "Pending failed: ${resP.errorBody()?.string()}")
                if (resI.isSuccessful) inTransitBookings = resI.body() ?: emptyList() else Log.e("DriverViewModel", "InTransit failed: ${resI.errorBody()?.string()}")
                if (resCo.isSuccessful) completedBookings = resCo.body() ?: emptyList() else Log.e("DriverViewModel", "Completed failed: ${resCo.errorBody()?.string()}")
                if (resCa.isSuccessful) canceledBookings = resCa.body() ?: emptyList() else Log.e("DriverViewModel", "Canceled failed: ${resCa.errorBody()?.string()}")

                Log.d("DriverViewModel", "Bookings fetched. Pending: ${pendingBookings.size}, InTransit: ${inTransitBookings.size}")

            } catch (e: Exception) {
                Log.e("DriverViewModel", "Error fetching bookings", e)
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun startTrip(checkpointId: Int, driverId: Int, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.instance.startTrip(checkpointId, driverId)
                if (response.isSuccessful) {
                    fetchActiveRoute(driverId)
                    onComplete()
                } else {
                    errorMessage = response.errorBody()?.string()
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchShipmentsAtCheckpoint(checkpointId: Int, driverId: Int, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.instance.getShipmentsAtCheckpoint(checkpointId, driverId)
                if (response.isSuccessful) {
                    checkpointShipments = response.body()
                    onComplete()
                } else {
                    errorMessage = response.errorBody()?.string()
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun confirmCheckpoint(checkpointId: Int, driverId: Int, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.instance.confirmCheckpointReached(checkpointId, driverId)
                if (response.isSuccessful) {
                    fetchActiveRoute(driverId)
                    checkpointShipments = null
                    onComplete()
                } else {
                    errorMessage = response.errorBody()?.string()
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    // ── Local setters ─────────────────────────────────────────────────────────

    fun setCapacity(current: Int, total: Int) {
        currentCapacity = current
        totalCapacity = total
    }

    fun setTotalDimensions(length: Int, width: Int, height: Int) {
        totalDimensions = Dimensions(length, width, height)
    }

    fun setCurrentDimensions(length: Int, width: Int, height: Int) {
        currentDimensions = Dimensions(length, width, height)
    }

    fun setDistances(values: List<Int>) {
        distances.clear()
        distances.addAll(values)
    }

    fun clearError() { errorMessage = null }

}
