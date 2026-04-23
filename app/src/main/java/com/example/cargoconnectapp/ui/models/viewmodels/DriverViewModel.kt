package com.example.cargoconnectapp.ui.models.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cargoconnectapp.data.RetrofitClient
import com.example.cargoconnectapp.ui.models.datamodels.CreateRouteRequest
import com.example.cargoconnectapp.ui.models.datamodels.RouteData
import com.example.cargoconnectapp.ui.models.helpermodels.Dimensions
import kotlinx.coroutines.launch

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
                    pastRoutes = all.filter {
                        it.isActive != true && it.isNextRoute != true
                    }
                } else errorMessage = response.errorBody()?.string()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun saveRoute(request: CreateRouteRequest, driverId: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.instance.saveRoute(request)
                if (response.isSuccessful) {
                    fetchActiveRoute(driverId)
                    fetchNextRoute(driverId)
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