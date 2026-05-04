package com.example.cargoconnectapp.ui.navigation.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.cargoconnectapp.ui.models.destinations.DriverDashboard
import com.example.cargoconnectapp.ui.models.destinations.DriverDestination
import com.example.cargoconnectapp.ui.models.viewmodels.DriverViewModel
import com.example.cargoconnectapp.ui.navigation.*
import com.example.cargoconnectapp.ui.themes.*
import com.example.cargoconnectapp.ui.models.map.OSMContainer
import com.example.cargoconnectapp.ui.models.map.getAddressFromGeoPoint
import org.osmdroid.util.GeoPoint
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.unit.Dp
import com.example.cargoconnectapp.ui.models.datamodels.BookingData
import com.example.cargoconnectapp.ui.models.datamodels.PackageDto
import com.example.cargoconnectapp.ui.models.datamodels.CheckpointShipmentsResponse
import com.example.cargoconnectapp.ui.models.datamodels.CheckpointShipmentData
import com.example.cargoconnectapp.ui.models.datamodels.CreateRouteRequest
import com.example.cargoconnectapp.ui.models.datamodels.NotificationDto
import com.example.cargoconnectapp.ui.models.datamodels.ReviewDto
import com.example.cargoconnectapp.ui.models.datamodels.RouteCheckpointRequest
import com.example.cargoconnectapp.ui.models.datamodels.RouteData
import com.example.cargoconnectapp.ui.models.helpermodels.PackageColorPool
import com.example.cargoconnectapp.ui.models.viewmodels.UserSessionViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

// ─────────────────────────────────────────────
// MODELS & DATA
// ─────────────────────────────────────────────

data class ShipmentRequest(
    val requestId: Int,
    val title: String,
    val route: String,
    val weight: String,
    val pickup: String,
    val amount: String,
    val isShared: Boolean = false,
    val totalWeight: Double = 0.0,
    val packageCount: Int = 0,
    val pickupAddress: String = "",
    val deliveryAddress: String = "",
    val packages: List<PackageDto> = emptyList()
)

data class CustomerDelivery(
    val customerName: String,
    val trackingId: String,
    val description: String,
    val checkpoint: String,
    val city: String,
    val daysUntil: String? = null
)

data class ChatContact(
    val name: String,
    val lastMessage: String,
    val time: String,
    val hasUnread: Boolean,
    val isSupport: Boolean = false
)

data class ShipmentPackage(
    val name: String,
    val quantity: Int,
    val tagColor: String,
    val tagId: String,
    val dimensions: String? = null,
    val weight: String? = null,
    val note: String? = null
)

data class ShipmentDetail(
    val title: String,
    val totalWeight: String,
    val recipientName: String,
    val totalFare: String,
    val packages: List<ShipmentPackage>
)

data class ChatMessage(
    val content: String,
    val isSentByMe: Boolean,
    val timestamp: String? = null
)

// ─────────────────────────────────────────────
// REUSABLE COMPONENTS (Enhanced)
// ─────────────────────────────────────────────

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.95f),
    ) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}

@Composable
fun AnimatedProgressBar(
    progress: Float,
    color: Color = actionTealColor,
    label: String = ""
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "progress"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textThemeColor)
            Text("${(progress * 100).toInt()}%", fontSize = 12.sp, color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(color.copy(alpha = 0.7f), color)
                        )
                    )
            )
        }
    }
}

@Composable
fun InteractiveTabRow(
    tabs: List<String>,
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.5f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabs.forEach { tab ->
            val isSelected = selectedTab == tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) actionTealColor else Color.Transparent)
                    .clickable { onTabSelected(tab) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab,
                    color = if (isSelected) Color.White else textThemeColor.copy(alpha = 0.6f),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// DRIVER DASHBOARD SCREEN (Overhauled)
// ─────────────────────────────────────────────

@androidx.annotation.RequiresPermission(
    allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION]
)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DriverDashboardScreen(
    driverId: Int,
    driverViewModel: DriverViewModel,
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onDelayClick: (Int) -> Unit = {},
    onReachedClick: (Int) -> Unit = {},
    currentTab: DriverDestination = DriverDashboard()
) {
    val backgroundGradient = Brush.verticalGradient(
        listOf(backgroundColor, Color.White)
    )

    val activeRoute = driverViewModel.activeRoute
    val checkpoints = activeRoute?.checkpoints?.sortedBy { it.sequenceNo } ?: emptyList()
    val reachedCount = checkpoints.count { it.reached ?: false }
    var selectedTab by remember { mutableStateOf("Trip Info") }
    val calculatedDistances = remember { mutableStateListOf<Double>() }

    LaunchedEffect(selectedTab) {
        if (selectedTab == "Truck Info") {
            driverViewModel.fetchTruckInfo(driverId)
        }
    }

    val tripStarted = activeRoute?.checkpoints?.any { it.reached == true } == true
    val nextCheckpointData = checkpoints.firstOrNull { it.reached != true }
    val nextCheckpointGeo = if (nextCheckpointData?.latitude != null && nextCheckpointData.longitude != null)
        GeoPoint(nextCheckpointData.latitude, nextCheckpointData.longitude) else null


    Scaffold(
        bottomBar = {
            DashboardBottomBar(
                currentTab = currentTab,
                onHomeClick = onHomeClick,
                onOrdersClick = onOrdersClick,
                onHistoryClick = onHistoryClick,
                onChatClick = onChatClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Driver Dashboard", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = textThemeColor)
                    Text("Welcome back, Captain!", fontSize = 14.sp, color = textThemeColor.copy(alpha = 0.6f))
                }
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = actionTealColor.copy(alpha = 0.1f)
                ) {
                    IconButton(onClick = { /* Notifications */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = actionTealColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            InteractiveTabRow(
                tabs = listOf("Trip Info", "Truck Info"),
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                }, label = ""
            ) { targetTab ->
                if (targetTab == "Trip Info") {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Progress Card
                        GlassCard {
                            Text("Current Progress", fontWeight = FontWeight.Bold, color = textThemeColor)
                            Spacer(modifier = Modifier.height(16.dp))
                            val progress = if (checkpoints.isNotEmpty()) reachedCount.toFloat() / checkpoints.size else 0f
                            AnimatedProgressBar(progress = progress, label = "Journey Status")
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "$reachedCount of ${checkpoints.size} stops reached",
                                fontSize = 12.sp,
                                color = textThemeColor.copy(alpha = 0.5f)
                            )
                        }

                        // Map / Stops Card
                        GlassCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Itinerary", fontWeight = FontWeight.Bold, color = textThemeColor)
                                Icon(Icons.Default.Map, contentDescription = null, tint = actionTealColor, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            Box(modifier = Modifier.height(200.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp)))   {
                                val geoPoints = checkpoints.mapNotNull { cp ->
                                    if (cp.latitude != null && cp.longitude != null) GeoPoint(cp.latitude, cp.longitude) else null
                                }
                                if (geoPoints.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxSize().background(Color.Gray.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                                        Text("No active route found", color = Color.Gray)
                                    }
                                } else {
                                    OSMContainer(
                                        points = geoPoints,
                                        onDistancesCalculated = { calculatedDistances.clear(); calculatedDistances.addAll(it) },
                                        onMapClick = {}, onMapLongClick = {},
                                        isNavigation = true
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            val firstUnreachedIndex = checkpoints.indexOfFirst { it.reached != true }

                            checkpoints.forEachIndexed { index, checkpoint ->
                                val dist = calculatedDistances.getOrNull(index)?.let {
                                    if (it < 1.0) "${(it * 1000).toInt()}m" else "%.1f km".format(it)
                                } ?: "--"

                                EnhancedRouteItem(
                                    city = checkpoint.name ?: "Unknown Stop",
                                    distance = dist,
                                    isReached = checkpoint.reached ?: false,
                                    isFirst = index == 0,
                                    isLast = index == checkpoints.lastIndex,
                                    isEnabled = index == firstUnreachedIndex,
                                    onDelay = { onDelayClick(index) },
                                    onReached = { onReachedClick(index) }
                                )
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        val truck = driverViewModel.truckInfo

                        // Visual Truck Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Brush.horizontalGradient(listOf(actionTealColor, actionTealColor.copy(alpha = 0.6f)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🚛", fontSize = 60.sp)
                                if (truck != null) {
                                    Text(truck.vehicle_model, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text(truck.reg_no, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                                }
                            }
                        }

                        // Capacity Card
                        GlassCard {
                            Text("Cargo Load", fontWeight = FontWeight.Bold, color = textThemeColor)
                            Spacer(modifier = Modifier.height(16.dp))

                            val weight = truck?.weight
                            val loadPercent = if (weight != null && weight.max_capacity > 0)
                                (weight.used_capacity / weight.max_capacity).toFloat() else 0f

                            AnimatedProgressBar(progress = loadPercent, color = statusBlueColor, label = "Weight Utilization")

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                InfoMetric(label = "Used", value = "${weight?.used_capacity ?: 0} KG")
                                InfoMetric(label = "Available", value = "${weight?.remaining_capacity ?: 0} KG")
                            }
                        }

                        // Dimensions Card
                        GlassCard {
                            Text("Remaining Space Dimensions", fontWeight = FontWeight.Bold, color = textThemeColor)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Current available linear space in cargo hold", fontSize = 12.sp, color = textThemeColor.copy(alpha = 0.5f))

                            Spacer(modifier = Modifier.height(16.dp))

                            val dim = truck?.dimensions?.remaining_dimensions
                            DimensionDisplayItem("Length", "${dim?.l ?: 0} m")
                            DimensionDisplayItem("Width", "${dim?.w ?: 0} m")
                            DimensionDisplayItem("Height", "${dim?.h ?: 0} m")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
@Composable
fun InfoMetric(label: String, value: String) {
    Column {
        Text(label, fontSize = 12.sp, color = textThemeColor.copy(alpha = 0.6f))
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textThemeColor)
    }
}

@Composable
fun DimensionDisplayItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = textThemeColor.copy(alpha = 0.7f))
        Text(value, fontWeight = FontWeight.Bold, color = actionTealColor)
    }
}

@Composable
fun EnhancedRouteItem(
    city: String,
    distance: String,
    isReached: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    isEnabled: Boolean,
    onDelay: () -> Unit,
    onReached: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (isReached) actionTealColor else Color.Gray.copy(alpha = 0.3f))
                    .border(3.dp, Color.White, CircleShape)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(if (isReached) actionTealColor.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.1f))
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f).padding(bottom = 24.dp)) {
            Text(city, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (isReached) actionTealColor else textThemeColor)
            Text(distance, fontSize = 12.sp, color = Color.Gray)
            
            if (!isReached) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onDelay,
                        enabled = isEnabled,
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = statusRedColor.copy(alpha = 0.1f), 
                            contentColor = statusRedColor,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.05f),
                            disabledContentColor = Color.Gray.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text("Delay", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onReached,
                        enabled = isEnabled,
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = actionTealColor,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.1f),
                            disabledContentColor = Color.Gray.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text(if (isFirst) "Start" else "Reached", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedCounter(label: String, value: String, onValueChange: (Int) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onValueChange(-100) }, modifier = Modifier.background(Color.Gray.copy(alpha = 0.1f), CircleShape).size(32.dp)) {
                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            Text(value, modifier = Modifier.width(80.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = actionTealColor)
            IconButton(onClick = { onValueChange(100) }, modifier = Modifier.background(actionTealColor.copy(alpha = 0.1f), CircleShape).size(32.dp)) {
                Icon(Icons.Default.Add, contentDescription = null, tint = actionTealColor, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun DimensionItem(label: String, value: Int, onValueChange: (Int) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), fontSize = 13.sp, color = textThemeColor.copy(alpha = 0.6f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("$value cm", fontWeight = FontWeight.SemiBold, color = textThemeColor)
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = { onValueChange(value - 5) }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.KeyboardArrowDown, null) }
            IconButton(onClick = { onValueChange(value + 5) }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.KeyboardArrowUp, null) }
        }
    }
}

// ─────────────────────────────────────────────
// SHIPMENT REQUESTS SCREEN (Overhauled)
// ─────────────────────────────────────────────

@Composable
fun ShipmentRequestsScreen(
    requests: List<ShipmentRequest> = emptyList(),
    onDetailsClick: (ShipmentRequest) -> Unit = {},
    onAccept: (ShipmentRequest) -> Unit = {},
    onDecline: (ShipmentRequest) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    currentTab: DriverDestination = DriverDashboard(),
    successMessage: String? = null,
    errorMessage: String? = null,
    onClearMessages: () -> Unit = {}
) {
    if (successMessage != null) {
        AlertDialog(
            onDismissRequest = onClearMessages,
            confirmButton = { TextButton(onClick = onClearMessages) { Text("OK", color = actionTealColor) } },
            title = { Text("Success") },
            text = { Text(successMessage) }
        )
    }

    Scaffold(
        bottomBar = {
            DashboardBottomBar(
                currentTab = currentTab,
                onHomeClick = onHomeClick,
                onOrdersClick = onOrdersClick,
                onHistoryClick = onHistoryClick,
                onChatClick = onChatClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Text("Shipment Requests", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = textThemeColor)
            Text("${requests.size} pending opportunities", fontSize = 14.sp, color = textThemeColor.copy(alpha = 0.6f))
            
            Spacer(modifier = Modifier.height(24.dp))

            if (errorMessage != null) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text(errorMessage, color = statusRedColor, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
                }
            } else if (requests.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray.copy(alpha = 0.3f))
                        Text("No requests right now", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    itemsIndexed(requests) { _, request ->
                        RequestCard(
                            request = request,
                            onDetails = { onDetailsClick(request) },
                            onAccept = { onAccept(request) },
                            onDecline = { onDecline(request) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RequestCard(
    request: ShipmentRequest,
    onDetails: () -> Unit,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = actionTealColor.copy(alpha = 0.1f)
            ) {
                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = actionTealColor, modifier = Modifier.padding(12.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(request.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(request.route, fontSize = 12.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            LabelValue("Weight", request.weight)
            LabelValue("Fare", request.amount)
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onDetails,
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, actionTealColor.copy(alpha = 0.3f))
            ) {
                Text("View Details", color = actionTealColor, fontSize = 13.sp)
            }
            Button(
                onClick = onAccept,
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = actionTealColor)
            ) {
                Text("Accept", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun LabelValue(label: String, value: String) {
    Column {
        Text(label, fontSize = 11.sp, color = Color.Gray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textThemeColor)
    }
}

// ─────────────────────────────────────────────
// SHIPMENT REQUEST DETAIL SCREEN (Overhauled)
// ─────────────────────────────────────────────

@Composable
fun ShipmentRequestDetailScreen(
    request: ShipmentRequest = ShipmentRequest(
        0,
        "Electronics Household (Private)",
        "Karachi to Peshawar",
        "~580 KGs",
        "02, September, 2025",
        "14570.0 PKR"
    ),
    onBack: () -> Unit = {},
    onAccept: () -> Unit = {},
    onDecline: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    currentTab: DriverDestination = DriverDashboard(),
    successMessage: String? = null,
    errorMessage: String? = null,
    onClearMessages: () -> Unit = {}
) {
    if (successMessage != null) {
        AlertDialog(
            onDismissRequest = onClearMessages,
            confirmButton = { TextButton(onClick = onClearMessages) { Text("OK", color = actionTealColor) } },
            title = { Text("Success") },
            text = { Text(successMessage) }
        )
    }

    Scaffold(
        bottomBar = {
            DashboardBottomBar(
                currentTab = currentTab,
                onHomeClick = onHomeClick,
                onOrdersClick = onOrdersClick,
                onHistoryClick = onHistoryClick,
                onChatClick = onChatClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            IconButton(onClick = onBack, modifier = Modifier.background(Color.White, CircleShape)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Request Details", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            
            Spacer(modifier = Modifier.height(12.dp))
            if (errorMessage != null) {
                Text(errorMessage, color = statusRedColor, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            GlassCard {
                Text(request.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = actionTealColor)
                Spacer(modifier = Modifier.height(16.dp))
                
                DetailItem(Icons.Default.PinDrop, "Pickup", request.pickupAddress)
                DetailItem(Icons.Default.Flag, "Delivery", request.deliveryAddress)
                DetailItem(Icons.Default.Scale, "Total Weight", "${request.totalWeight} KG")
                DetailItem(Icons.Default.Inventory, "Total Items", "${request.packageCount} units")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle("Items to carry")
            
            request.packages.forEach { pkg ->
                GlassCard(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(8.dp), color = actionTealColor.copy(alpha = 0.1f)) {
                            Icon(Icons.Default.Inventory2, null, modifier = Modifier.padding(8.dp), tint = actionTealColor)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(pkg.name, fontWeight = FontWeight.Bold)
                            Text("Qty: ${pkg.quantity} • Weight: ${pkg.weight} KG", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Dimensions: ${pkg.length} x ${pkg.width} x ${pkg.height} cm", fontSize = 12.sp, color = Color.Gray)
                    Text("Tag: ${pkg.tagNo}", fontSize = 12.sp, color = Color.Gray)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = statusRedColor.copy(alpha = 0.1f), contentColor = statusRedColor),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Decline", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = actionTealColor),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Accept Request", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun DetailItem(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 10.sp, color = Color.Gray)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// ─────────────────────────────────────────────
// CUSTOMERS SCREEN (Overhauled)
// ─────────────────────────────────────────────

@Composable
fun CustomersScreen(
    pendingBookings: List<BookingData> = emptyList(),
    inTransitBookings: List<BookingData> = emptyList(),
    completedBookings: List<BookingData> = emptyList(),
    canceledBookings: List<BookingData> = emptyList(),
    onDetailsClick: (BookingData) -> Unit = {},
    onContactClick: (BookingData) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    currentTab: DriverDestination = DriverDashboard()
) {
    var selectedTab by remember { mutableStateOf("Pending") }
    val tabs = listOf("Pending", "In-Transit", "Completed", "Canceled")

    Scaffold(
        bottomBar = {
            DashboardBottomBar(
                currentTab = currentTab,
                onHomeClick = onHomeClick,
                onOrdersClick = onOrdersClick,
                onHistoryClick = onHistoryClick,
                onChatClick = onChatClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Text("Deliveries", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = textThemeColor)
            
            Spacer(modifier = Modifier.height(24.dp))

            InteractiveTabRow(
                tabs = tabs,
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            val currentList = when (selectedTab) {
                "Pending" -> pendingBookings
                "In-Transit" -> inTransitBookings
                "Completed" -> completedBookings
                "Canceled" -> canceledBookings
                else -> emptyList()
            }

            val accentColor = when (selectedTab) {
                "Pending" -> statusOrangeColor
                "In-Transit" -> statusBlueColor
                "Completed" -> actionTealColor
                "Canceled" -> statusRedColor
                else -> Color.Gray
            }

            if (currentList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No $selectedTab deliveries found.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    itemsIndexed(currentList) { _, booking ->
                        BookingInteractiveCard(
                            booking = booking,
                            accentColor = accentColor,
                            onDetails = { onDetailsClick(booking) },
                            onContact = if (selectedTab == "Pending" || selectedTab == "In-Transit") {
                                { onContactClick(booking) }
                            } else null
                        )
                    }
                }
            }

        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = textThemeColor.copy(alpha = 0.4f),
        modifier = Modifier.padding(vertical = 12.dp)
    )
}

@Composable
fun BookingInteractiveCard(
    booking: BookingData,
    accentColor: Color,
    onDetails: (BookingData) -> Unit,
    onContact: ((BookingData) -> Unit)? = null  // ← add this
) {
    GlassCard(modifier = Modifier.padding(vertical = 8.dp).clickable { onDetails(booking) }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(4.dp, 40.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Booking #${booking.booking_id}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Shipment #${booking.shipment_id}", fontSize = 12.sp, color = accentColor, fontWeight = FontWeight.SemiBold)
            }
            Surface(color = accentColor.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                Text(booking.status, modifier = Modifier.padding(6.dp), fontSize = 10.sp, color = accentColor, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Amount: ${booking.amount} PKR", fontSize = 13.sp, color = textThemeColor.copy(alpha = 0.7f))
        booking.pickup_date?.let {
            Text("Pickup Date: $it", fontSize = 13.sp, color = textThemeColor.copy(alpha = 0.7f))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onDetails(booking) },
            modifier = Modifier.fillMaxWidth().height(40.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
        ) {
            Text("View Details", fontWeight = FontWeight.Bold)
        }

        // Contact Customer — only for Assigned and In-Transit
        if (onContact != null && (booking.status == "Assigned" || booking.status == "In-Transit")) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { onContact(booking) },
                modifier = Modifier.fillMaxWidth().height(40.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
            ) {
                Icon(Icons.Default.Chat, null, modifier = Modifier.size(14.dp), tint = accentColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Contact Customer", fontSize = 12.sp, color = accentColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DeliveryInteractiveCard(
    delivery: CustomerDelivery,
    accentColor: Color,
    onDetails: (CustomerDelivery) -> Unit,
    onConfirm: ((CustomerDelivery) -> Unit)? = null
) {
    GlassCard(modifier = Modifier.padding(vertical = 8.dp).clickable { onDetails(delivery) }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(4.dp, 40.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(delivery.customerName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(delivery.trackingId, fontSize = 12.sp, color = accentColor, fontWeight = FontWeight.SemiBold)
            }
            if (delivery.daysUntil != null) {
                Surface(color = accentColor.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                    Text(delivery.daysUntil, modifier = Modifier.padding(6.dp), fontSize = 10.sp, color = accentColor, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(delivery.description, fontSize = 13.sp, color = textThemeColor.copy(alpha = 0.7f))
        
        if (onConfirm != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onConfirm(delivery) },
                modifier = Modifier.fillMaxWidth().height(40.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("Confirm Pickup", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─────────────────────────────────────────────
// DRIVER BOOKING DETAIL SCREEN
// ─────────────────────────────────────────────

@Composable
fun DriverBookingDetailScreen(
    booking: BookingData,
    onBack: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    currentTab: DriverDestination = DriverDashboard()
) {
    Scaffold(
        bottomBar = {
            DashboardBottomBar(
                currentTab = currentTab,
                onHomeClick = onHomeClick,
                onOrdersClick = onOrdersClick,
                onHistoryClick = onHistoryClick,
                onChatClick = onChatClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            IconButton(onClick = onBack, modifier = Modifier.background(Color.White, CircleShape)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Booking Details", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            GlassCard {
                Text("Booking #${booking.booking_id}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = actionTealColor)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Status: ${booking.status}", fontWeight = FontWeight.Bold, color = statusBlueColor)
                Text("Amount: ${booking.amount} PKR", fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(16.dp))

                DetailItem(Icons.Default.Person, "Sender", booking.sender_name ?: "N/A")
                DetailItem(Icons.Default.Phone, "Sender Contact", booking.sender_contact ?: "N/A")
                DetailItem(Icons.Default.Person, "Recipient", booking.recipient_name ?: "N/A")
                DetailItem(Icons.Default.Phone, "Recipient Contact", booking.recipient_contact ?: "N/A")
                DetailItem(Icons.Default.PinDrop, "Pickup", booking.pickup_address ?: "N/A")
                DetailItem(Icons.Default.Flag, "Delivery", booking.delivery_address ?: "N/A")
                DetailItem(Icons.Default.Scale, "Total Weight", "${booking.total_weight ?: 0.0} KG")
                DetailItem(Icons.Default.Inventory, "Total Items", "${booking.package_count ?: 0} units")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle("Packages")
            
            booking.packages.forEach { pkg ->
                GlassCard(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(8.dp), color = actionTealColor.copy(alpha = 0.1f)) {
                            Icon(Icons.Default.Inventory2, null, modifier = Modifier.padding(8.dp), tint = actionTealColor)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(pkg.name, fontWeight = FontWeight.Bold)
                            Text("Qty: ${pkg.quantity} • Weight: ${pkg.weight} KG", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Dimensions: ${pkg.length} x ${pkg.width} x ${pkg.height} cm", fontSize = 12.sp, color = Color.Gray)
                    Text("Tag: ${pkg.tagNo}", fontSize = 12.sp, color = Color.Gray)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ─────────────────────────────────────────────
// CHECKPOINT SHIPMENTS SCREEN
// ─────────────────────────────────────────────

@Composable
fun CheckpointShipmentsScreen(
    data: CheckpointShipmentsResponse,
    isLoading: Boolean = false,
    isStartCheckpoint: Boolean = false,  // NEW
    onPickup: (Int) -> Unit = {},
    onDeliver: (Int) -> Unit = {},
    showReviewDialog: Boolean = false,
    onReviewSubmit: (rating: Int, comment: String) -> Unit = { _, _ -> },
    onReviewDismiss: () -> Unit = {},
    onConfirm: () -> Unit = {},
    onBack: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    currentTab: DriverDestination = DriverDashboard()
) {
    // For start checkpoint only show Load tab
    val tabs = if (isStartCheckpoint)
        listOf("Load (${data.to_load.total})")
    else
        listOf("Load (${data.to_load.total})", "Drop (${data.to_drop.total})")

    var selectedSubTab by remember { mutableStateOf(tabs.first()) }

    // For start: only need all pickups done
    // For others: need both pickups and drops done
    val allPickedUp = data.to_load.shipments.all { it.status == "In-Transit" }
    val allDelivered = if (isStartCheckpoint) true
    else data.to_drop.shipments.all { it.status == "Completed" }
    val canConfirm = allPickedUp && allDelivered

    val confirmButtonText = if (isStartCheckpoint) "Confirm Start" else "Confirm Reached"

    Scaffold(
        bottomBar = {
            DashboardBottomBar(
                currentTab = currentTab,
                onHomeClick = onHomeClick,
                onOrdersClick = onOrdersClick,
                onHistoryClick = onHistoryClick,
                onChatClick = onChatClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            IconButton(onClick = onBack, modifier = Modifier.background(Color.White, CircleShape)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(data.checkpoint_name, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Text(
                if (isStartCheckpoint) "Load all packages before starting" else "Checkpoint Shipment Log",
                fontSize = 14.sp, color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            InteractiveTabRow(
                tabs = tabs,
                selectedTab = selectedSubTab,
                onTabSelected = { selectedSubTab = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (selectedSubTab.startsWith("Load")) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                        if (data.to_load.shipments.isEmpty()) {
                            item { Text("No pickups at this stop.", color = Color.Gray, fontSize = 13.sp) }
                        } else {
                            itemsIndexed(data.to_load.shipments) { _, shipment ->
                                ShipmentActionCard(
                                    shipment = shipment,
                                    color = statusOrangeColor,
                                    label = "Pickup From",
                                    actionLabel = if (shipment.status == "In-Transit") "✓ Picked Up" else "Pickup",
                                    actionEnabled = shipment.status == "Assigned",
                                    onAction = { onPickup(shipment.booking_id) }
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                        if (data.to_drop.shipments.isEmpty()) {
                            item { Text("No drop-offs at this stop.", color = Color.Gray, fontSize = 13.sp) }
                        } else {
                            itemsIndexed(data.to_drop.shipments) { _, shipment ->
                                ShipmentActionCard(
                                    shipment = shipment,
                                    color = statusBlueColor,
                                    label = "Deliver To",
                                    actionLabel = if (shipment.status == "Completed") "✓ Delivered" else "Deliver",
                                    actionEnabled = shipment.status == "In-Transit",
                                    onAction = { onDeliver(shipment.booking_id) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!canConfirm) {
                Text(
                    text = when {
                        !allPickedUp -> "Pick up all shipments to ${if (isStartCheckpoint) "start" else "confirm"}"
                        !allDelivered -> "Deliver all shipments to confirm"
                        else -> ""
                    },
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 16.dp))
            } else {
                Button(
                    onClick = onConfirm,
                    enabled = canConfirm,
                    modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = actionTealColor,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                    )
                ) {
                    Text(confirmButtonText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (showReviewDialog) {
            ReviewDialog(
                onSubmit = onReviewSubmit,
                onDismiss = onReviewDismiss
            )
        }
    }
}

@Composable
fun ShipmentActionCard(
    shipment: CheckpointShipmentData,
    color: Color,
    label: String,
    actionLabel: String,
    actionEnabled: Boolean,
    onAction: () -> Unit
) {
    val isDone = !actionEnabled
    GlassCard(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(8.dp),
                color = if (isDone) Color.Gray.copy(alpha = 0.1f) else color.copy(alpha = 0.1f)
            ) {
                Icon(
                    Icons.Default.Inventory2,
                    null,
                    modifier = Modifier.padding(8.dp),
                    tint = if (isDone) Color.Gray else color
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Shipment #${shipment.shipment_id}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("${shipment.total_weight} KG • ${shipment.package_count ?: 0} units", fontSize = 12.sp, color = Color.Gray)
            }
            Button(
                onClick = onAction,
                enabled = actionEnabled,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = color,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.2f),
                    disabledContentColor = Color.Gray
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(actionLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(12.dp))

        Text(label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(shipment.sender_name ?: "N/A", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(shipment.sender_contact ?: "N/A", fontSize = 13.sp, color = actionTealColor)

        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.PinDrop, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                if (label == "Pickup From") (shipment.pickup_address ?: "N/A") else (shipment.delivery_address ?: "N/A"),
                fontSize = 12.sp,
                color = textThemeColor.copy(alpha = 0.7f)
            )
        }

        // Packages
        if (shipment.packages.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Packages", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            shipment.packages.forEach { pkg ->
                val pkgColor = PackageColorPool.getColor(pkg.color)
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = pkgColor.copy(alpha = 0.12f)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(pkgColor))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(pkg.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Qty: ${pkg.quantity} • ${pkg.weight} KG", fontSize = 12.sp, color = Color.Gray)
                        }
                        Surface(shape = RoundedCornerShape(6.dp), color = pkgColor.copy(alpha = 0.2f)) {
                            Text(
                                pkg.tagNo ?: "No Tag",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = pkgColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShipmentDetailedCard(
    shipment: CheckpointShipmentData,
    color: Color,
    label: String
) {
    GlassCard(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.1f)) {
                Icon(Icons.Default.Inventory2, null, modifier = Modifier.padding(8.dp), tint = color)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Shipment #${shipment.shipment_id}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${shipment.total_weight} KG • ${shipment.package_count ?: 0} units", fontSize = 12.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(12.dp))

        Text(label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(shipment.sender_name ?: "N/A", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(shipment.sender_contact ?: "N/A", fontSize = 13.sp, color = actionTealColor)

        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.PinDrop, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                if (label == "Pickup From") (shipment.pickup_address ?: "N/A") else (shipment.delivery_address ?: "N/A"),
                fontSize = 12.sp,
                color = textThemeColor.copy(alpha = 0.7f)
            )
        }

        // Packages list
        if (shipment.packages.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Packages", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            shipment.packages.forEach { pkg ->
                val pkgColor = PackageColorPool.getColor(pkg.color)

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = pkgColor.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Color dot
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(pkgColor)
                        )
                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(pkg.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                "Qty: ${pkg.quantity} • ${pkg.weight} KG",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                "${pkg.length} x ${pkg.width} x ${pkg.height} cm",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        // Tag badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = pkgColor.copy(alpha = 0.2f)
                        ) {
                            Text(
                                pkg.tagNo ?: "No Tag",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = pkgColor
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// CUSTOMER SHIPMENTS DETAIL SCREEN (Overhauled)
// ─────────────────────────────────────────────

@Composable
fun CustomerShipmentsDetailScreen(
    shipments: List<ShipmentDetail> = emptyList(),
    onBack: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    currentTab: DriverDestination = DriverDashboard()
) {
    Scaffold(
        bottomBar = {
            DashboardBottomBar(
                currentTab = currentTab,
                onHomeClick = onHomeClick,
                onOrdersClick = onOrdersClick,
                onHistoryClick = onHistoryClick,
                onChatClick = onChatClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            IconButton(onClick = onBack, modifier = Modifier.background(Color.White, CircleShape)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Shipment Details", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)

            Spacer(modifier = Modifier.height(24.dp))

            if (shipments.isEmpty()) {
                GlassCard {
                    Text("No shipment data available.", color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            } else {
                shipments.forEach { shipment ->
                    GlassCard(modifier = Modifier.padding(bottom = 16.dp)) {
                        Text(shipment.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = actionTealColor)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Weight: ${shipment.totalWeight}", fontSize = 14.sp, color = Color.Gray)
                        Text("Fare: ${shipment.totalFare}", fontSize = 14.sp, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(16.dp))

                        shipment.packages.forEachIndexed { index, pkg ->
                            Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 4.dp)) {
                                Surface(modifier = Modifier.size(24.dp), shape = CircleShape, color = actionTealColor.copy(alpha = 0.1f)) {
                                    Text("${index + 1}", modifier = Modifier.fillMaxSize(), textAlign = TextAlign.Center, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = actionTealColor)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(pkg.name, fontWeight = FontWeight.SemiBold)
                                    Text("Qty: ${pkg.quantity} • Tag: ${pkg.tagId}", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ─────────────────────────────────────────────
// ACTIVITY & PROFILE (Visual Tweaks)
// ─────────────────────────────────────────────

@Composable
fun ActivityScreen(
    chats: List<ChatContact> = emptyList(),
    notifications: List<NotificationDto> = emptyList(),
    onChatClick: (ChatContact) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatNavClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    currentTab: DriverDestination = DriverDashboard()
) {
    var selectedTab by remember { mutableStateOf("Chats") }

    Scaffold(
        bottomBar = {
            DashboardBottomBar(
                currentTab = currentTab,
                onHomeClick = onHomeClick,
                onOrdersClick = onOrdersClick,
                onHistoryClick = onHistoryClick,
                onChatClick = onChatNavClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Text("Activity", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = textThemeColor)

            Spacer(modifier = Modifier.height(24.dp))

            InteractiveTabRow(
                tabs = listOf("Chats", "Notifications"),
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (selectedTab == "Chats") {
                    items(chats.size) { index ->
                        EnhancedChatListItem(chats[index], onClick = { onChatClick(chats[index]) })
                    }
                } else {
                    items(notifications.size) { index ->
                        EnhancedNotificationCard(notifications[index])
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedChatListItem(chat: ChatContact, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Surface(modifier = Modifier.size(56.dp), shape = CircleShape, color = Color.White) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.padding(12.dp), tint = Color.LightGray)
                }
                if (chat.hasUnread) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(statusRedColor).border(2.dp, backgroundColor, CircleShape).align(Alignment.TopEnd))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(chat.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(chat.time, fontSize = 12.sp, color = Color.Gray)
                }
                Text(chat.lastMessage, fontSize = 14.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun EnhancedNotificationCard(notif: NotificationDto) {
    GlassCard(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = actionTealColor.copy(alpha = 0.1f)) {
                Icon(Icons.Default.NotificationsActive, null, modifier = Modifier.padding(8.dp), tint = actionTealColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(notif.message, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(notif.created_at, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
fun ProfileScreen(
    fullName: String = "",
    rating: Float = 4.8f,
    numberOfReviews: Int = 0,
    isSwitchedToCustomer: Boolean = false,
    onYourInformationClick: () -> Unit = {},
    onActivateRouteClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onReviewsClick: () -> Unit = {},
    onSwitchRoleToggle: (Boolean) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    currentTab: DriverDestination = DriverDashboard(),
    session: UserSessionViewModel
) {
    Scaffold(
        bottomBar = {
            DashboardBottomBar(
                currentTab = currentTab,
                onHomeClick = onHomeClick,
                onOrdersClick = onOrdersClick,
                onHistoryClick = onHistoryClick,
                onChatClick = onChatClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Profile
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(listOf(actionTealColor.copy(alpha = 0.2f), Color.Transparent)),
                            radius = size.width,
                            center = Offset(size.width / 2, 0f)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(120.dp)) {
                        Surface(
                            modifier = Modifier.fillMaxSize().shadow(12.dp, CircleShape),
                            shape = CircleShape,
                            color = Color.White
                        ) {
                            if (session.userProfileImageUrl != null) {
                                AsyncImage(model = session.userProfileImageUrl, contentDescription = null, contentScale = ContentScale.Crop)
                            } else {
                                Icon(Icons.Default.Person, null, modifier = Modifier.padding(24.dp), tint = actionTealColor)
                            }
                        }
                        Surface(
                            modifier = Modifier.size(36.dp).align(Alignment.BottomEnd),
                            shape = CircleShape,
                            color = actionTealColor,
                            shadowElevation = 4.dp
                        ) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.padding(8.dp), tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(fullName, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = statusOrangeColor, modifier = Modifier.size(16.dp))
                        Text(" $rating Driver Rating ($numberOfReviews)", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Menu Items
            Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileMenuCard("Personal Information", Icons.Default.Badge, onYourInformationClick)
                ProfileMenuCard("Route Management", Icons.Default.Route, onActivateRouteClick)
                ProfileMenuCard("Settings & Security", Icons.Default.Settings, {})
                ProfileMenuCard("Reviews", Icons.Default.Star, onReviewsClick)

                Spacer(modifier = Modifier.height(24.dp))

                // Role Switch Card
                GlassCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SwapHoriz, null, tint = statusBlueColor)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Switch to Customer Mode", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        Switch(checked = isSwitchedToCustomer, onCheckedChange = { onSwitchRoleToggle(it) })
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onLogoutClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = statusRedColor.copy(alpha = 0.1f), contentColor = statusRedColor),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Logout, null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Logout Session", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
@Composable
fun AllReviewsScreen(
    reviews: List<ReviewDto>,
    averageRating: Double,
    totalReviews: Int,
    isLoading: Boolean,
    onBack: () -> Unit,
    currentTab: DriverDestination,
    onHomeClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onChatClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Scaffold(
        bottomBar = {
            DashboardBottomBar(
                currentTab = currentTab,
                onHomeClick = onHomeClick,
                onOrdersClick = onOrdersClick,
                onHistoryClick = onHistoryClick,
                onChatClick = onChatClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textThemeColor)
                }
                Text(
                    "Ratings & Feedback",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = textThemeColor
                )
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = actionTealColor)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Summary Card (Matches RatingResponse fields)
                    item {
                        GlassCard {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "%.1f".format(averageRating),
                                        fontSize = 42.sp,
                                        fontWeight = FontWeight.Black,
                                        color = actionTealColor
                                    )
                                    Text(
                                        "Average Rating",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    RatingBar(rating = averageRating.toFloat())
                                    Text(
                                        "$totalReviews total reviews",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = textThemeColor
                                    )
                                }
                            }
                        }
                    }

                    if (reviews.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillParentMaxHeight(0.6f).fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No feedback received yet", color = Color.Gray)
                            }
                        }
                    } else {
                        items(reviews) { review ->
                            ReviewItem(review)
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
fun ReviewItem(review: ReviewDto) {
    GlassCard {
        Column(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        // Uses reviewerName from your DTO
                        text = review.reviewerName ?: "Anonymous User",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = textThemeColor
                    )
                    // Uses createdAt from your DTO
                    Text(
                        text = review.createdAt?.split("T")?.firstOrNull() ?: "",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                // Uses rating from your DTO
                RatingBar(rating = review.rating.toFloat(), size = 12.dp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                // Uses comments (plural) from your DTO
                text = review.comments ?: "No written feedback provided.",
                fontSize = 14.sp,
                color = textThemeColor.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun RatingBar(rating: Float, size: Dp = 18.dp) {
    Row {
        repeat(5) { index ->
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(size),
                tint = if (index < rating) statusOrangeColor else Color.LightGray.copy(alpha = 0.5f)
            )
        }
    }
}
@Composable
fun ProfileMenuCard(title: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(10.dp), color = backgroundColor.copy(alpha = 0.4f)) {
                Icon(icon, null, modifier = Modifier.padding(8.dp), tint = textThemeColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}

// ─────────────────────────────────────────────
// (Keeping Other Screens Simple but Styled)
// ─────────────────────────────────────────────

@Composable
fun YourInformationScreen(
    fullName: String = "",
    phone: String = "",
    email: String = "",
    cnic: String = "",
    licenseNo: String = "",
    onBack: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    currentTab: DriverDestination = DriverDashboard()
) {
    Scaffold(
        bottomBar = {
            DashboardBottomBar(currentTab, onHomeClick, onOrdersClick, onHistoryClick, onChatClick, onProfileClick)
        },
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(24.dp))
            IconButton(onClick = onBack, modifier = Modifier.background(Color.White, CircleShape)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Personal Details", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)

            Spacer(modifier = Modifier.height(24.dp))
            GlassCard {
                DetailRow("Full Name", fullName)
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Gray.copy(alpha = 0.1f))
                DetailRow("Phone", phone)
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Gray.copy(alpha = 0.1f))
                DetailRow("Email", email)
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Gray.copy(alpha = 0.1f))
                DetailRow("License", licenseNo)
            }

            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onEditClick, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = actionTealColor)) {
                Text("Edit Profile", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

// ─────────────────────────────────────────────
// CHAT & OTHERS (Basic Overhaul)
// ─────────────────────────────────────────────

@Composable
fun ChatScreen(
    contactName: String = "Kamran",
    contactStatus: String = "Online",
    contactImageUrl: String? = null,
    messages: List<ChatMessage> = emptyList(),
    onBack: () -> Unit = {},
    onCallClick: () -> Unit = {},
    onSendMessage: (String) -> Unit = {}
) {
    var messageText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(backgroundColor).imePadding()) {
        // Chat Header
        Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 4.dp, color = Color.White) {
            Row(modifier = Modifier.padding(top = 48.dp, bottom = 12.dp, start = 16.dp, end = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = actionTealColor.copy(alpha = 0.1f)) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.padding(8.dp), tint = actionTealColor)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(contactName, fontWeight = FontWeight.Bold)
                    Text(contactStatus, fontSize = 11.sp, color = actionTealColor)
                }
                IconButton(onClick = onCallClick) { Icon(Icons.Default.Call, null, tint = actionTealColor) }
            }
        }

        // Messages Area
        Box(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
            LazyColumn(modifier = Modifier.fillMaxSize(), reverseLayout = false, verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 16.dp)) {
                items(messages.size) { index ->
                    val msg = messages[index]
                    ChatBubbleEnhanced(msg)
                }
            }
        }

        // Input Area
        Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Write a message...") },
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(24.dp)),
                    colors = TextFieldDefaults.colors(focusedContainerColor = backgroundColor.copy(alpha = 0.3f), unfocusedContainerColor = backgroundColor.copy(alpha = 0.3f), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = actionTealColor) {
                    IconButton(onClick = { if(messageText.isNotBlank()) { onSendMessage(messageText); messageText = "" } }) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubbleEnhanced(message: ChatMessage) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = if(message.isSentByMe) Alignment.CenterEnd else Alignment.CenterStart) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if(message.isSentByMe) 16.dp else 2.dp,
                bottomEnd = if(message.isSentByMe) 2.dp else 16.dp
            ),
            color = if(message.isSentByMe) actionTealColor else Color.White,
            shadowElevation = 1.dp
        ) {
            Text(message.content, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), color = if(message.isSentByMe) Color.White else textThemeColor, fontSize = 14.sp)
        }
    }
}

// ─────────────────────────────────────────────
// ADDITIONAL SCREENS (Simple Styling)
// ─────────────────────────────────────────────

/*@Composable
fun ActivateRouteScreen(
    activeRoute: String? = "No Active Route",
    activeVia: String? = null,
    nextRoute: String? = "No Next Route",
    onTapToChange: () -> Unit = {},
    onTapToChangeUpcoming: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    currentTab: DriverDestination = DriverDashboard()
) {
    Scaffold(bottomBar = { DashboardBottomBar(currentTab, onHomeClick, onOrdersClick, onHistoryClick, onChatClick, onProfileClick) }, containerColor = backgroundColor) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(32.dp))
            Text("Your Routes", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(24.dp))

            Text("Active Route", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(modifier = Modifier.clickable { onTapToChange() }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PlayCircle, null, tint = actionTealColor)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(activeRoute ?: "None", fontWeight = FontWeight.Bold)
                        if(activeVia != null) Text(activeVia, fontSize = 12.sp, color = Color.Gray)
                    }
                    Text("CHANGE", fontSize = 12.sp, color = actionTealColor, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Upcoming Route", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(modifier = Modifier.clickable { onTapToChangeUpcoming() }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, tint = if (nextRoute == null) Color.Gray else actionTealColor)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(nextRoute ?: "No route scheduled", fontWeight = FontWeight.Medium, color = if (nextRoute == null) Color.Gray else textThemeColor)
                    }
                    Text(if (nextRoute == null) "ADD" else "CHANGE", fontSize = 12.sp, color = actionTealColor, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}*/
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ActivateRouteScreen(
    activeRouteLabel: String? = null,
    activeVia: String? = null,
    nextRouteLabel: String? = null,
    nextVia: String? = null,
    pastRoutes: List<RouteData> = emptyList(),
    isLoading: Boolean = false,
    onCreateNew: () -> Unit = {},
    onMakeActive: (RouteData) -> Unit = {},
    onMakeNext: (RouteData) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    currentTab: DriverDestination = DriverDashboard()
) {
    Scaffold(
        bottomBar = {
            DashboardBottomBar(
                currentTab, onHomeClick, onOrdersClick,
                onHistoryClick, onChatClick, onProfileClick
            )
        },
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                "Route Management",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textThemeColor
            )
            Text(
                "Manage your active and upcoming routes",
                fontSize = 14.sp,
                color = textThemeColor.copy(alpha = 0.55f)
            )
            Spacer(modifier = Modifier.height(24.dp))

            // ── Create New button ─────────────────────────────────────────────
            Button(
                onClick = onCreateNew,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = actionTealColor)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create New Route", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Active Route ──────────────────────────────────────────────────
            SectionLabel("Active Route")
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = actionTealColor.copy(alpha = 0.12f)
                    ) {
                        Icon(
                            Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = actionTealColor,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            activeRouteLabel ?: "No active route",
                            fontWeight = FontWeight.Bold,
                            color = if (activeRouteLabel != null) textThemeColor
                            else textThemeColor.copy(alpha = 0.4f)
                        )
                        if (activeVia != null) {
                            Text(activeVia, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Next Route ────────────────────────────────────────────────────
            SectionLabel("Upcoming Route")
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Gray.copy(alpha = 0.1f)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = if (nextRouteLabel != null) actionTealColor else Color.Gray,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            nextRouteLabel ?: "No route scheduled",
                            fontWeight = FontWeight.Medium,
                            color = if (nextRouteLabel != null) textThemeColor
                            else textThemeColor.copy(alpha = 0.4f)
                        )
                        if (nextVia != null) {
                            Text(nextVia, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Past Routes ───────────────────────────────────────────────────
            SectionLabel("Recent Routes")
            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = actionTealColor)
                }
            } else if (pastRoutes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No past routes yet",
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    pastRoutes.forEach { route ->
                        val startName = route.startPoint
                            ?: route.checkpoints?.firstOrNull()?.name ?: "?"
                        val endName = route.endPoint
                            ?: route.checkpoints?.lastOrNull()?.name ?: "?"
                        val viaName = route.checkpoints?.getOrNull(1)?.name
                        PastRouteCard(
                            label = "$startName → $endName",
                            via = viaName?.let { "via $it" },
                            onMakeActive = { onMakeActive(route) },
                            onMakeNext = { onMakeNext(route) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = textThemeColor.copy(alpha = 0.45f),
        letterSpacing = 1.sp
    )
}

@Composable
private fun PastRouteCard(
    label: String,
    via: String?,
    onMakeActive: () -> Unit,
    onMakeNext: () -> Unit
) {
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(8.dp),
                color = backgroundColor.copy(alpha = 0.6f)
            ) {
                Icon(
                    Icons.Default.Route,
                    contentDescription = null,
                    tint = textThemeColor.copy(alpha = 0.5f),
                    modifier = Modifier.padding(7.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                if (via != null) {
                    Text(via, fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onMakeNext,
                modifier = Modifier.weight(1f).height(40.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, actionTealColor.copy(alpha = 0.4f))
            ) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = actionTealColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Set as Next", fontSize = 12.sp, color = actionTealColor)
            }
            Button(
                onClick = onMakeActive,
                modifier = Modifier.weight(1f).height(40.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = actionTealColor)
            ) {
                Icon(
                    Icons.Default.PlayCircle,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Set Active", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SelectRouteScreen(
    savedRoutes: List<Pair<String, String?>> = emptyList(),
    onRouteSelected: (Int) -> Unit = {},
    onCreateNewRoute: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    currentTab: DriverDestination = DriverDashboard()
) {
    Scaffold(bottomBar = { DashboardBottomBar(currentTab, onHomeClick, onOrdersClick, onHistoryClick, onChatClick, onProfileClick) }, containerColor = backgroundColor) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 24.dp)) {
            Spacer(modifier = Modifier.height(32.dp))
            Text("Select Route", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(24.dp))

            if (savedRoutes.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No routes found. Create one first.", color = Color.Gray, fontWeight = FontWeight.Medium)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    itemsIndexed(savedRoutes) { index, route ->
                        GlassCard(modifier = Modifier.clickable { onRouteSelected(index) }) {
                            Text(route.first, fontWeight = FontWeight.Bold)
                            route.second?.let { Text(it, fontSize = 12.sp, color = Color.Gray) }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onCreateNewRoute, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = actionTealColor)) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create New Route")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun CreateRouteStep1(
    routePoints: SnapshotStateList<GeoPoint>,
    onNext: () -> Unit = {},
    onBack: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    currentTab: DriverDestination = DriverDashboard()
) {
    Scaffold(bottomBar = { DashboardBottomBar(currentTab, onHomeClick, onOrdersClick, onHistoryClick, onChatClick, onProfileClick) }, containerColor = backgroundColor) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Box(modifier = Modifier.weight(1f)) {
                OSMContainer(points = routePoints.toList(), onMapClick = { routePoints.add(it) }, onMapLongClick = {})

                Surface(modifier = Modifier.align(Alignment.TopCenter).padding(16.dp).fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp)), color = Color.White.copy(alpha = 0.9f), shape = RoundedCornerShape(16.dp)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                        Text("Mark Route Points", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("${routePoints.size} Points", fontSize = 12.sp, color = actionTealColor, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 8.dp) {
                Button(onClick = onNext, enabled = routePoints.size >= 2, modifier = Modifier.padding(16.dp).fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = actionTealColor)) {
                    Text("Confirm Route Points")
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CreateRouteStep2(
    minDate: LocalDate? = null,
    onNext: (LocalDateTime, LocalDateTime) -> Unit = { _, _ -> },
    onBack: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    currentTab: DriverDestination = DriverDashboard()
) {
    var departureDate by remember { mutableStateOf<LocalDate?>(null) }
    var arrivalDate by remember { mutableStateOf<LocalDate?>(null) }
    var departureHour by remember { mutableIntStateOf(8) }
    var departureMinute by remember { mutableIntStateOf(0) }
    var arrivalHour by remember { mutableIntStateOf(8) }
    var arrivalMinute by remember { mutableIntStateOf(0) }
    var departureMonth by remember { mutableStateOf(YearMonth.now()) }
    var arrivalMonth by remember { mutableStateOf(YearMonth.now()) }

    Scaffold(bottomBar = { DashboardBottomBar(currentTab, onHomeClick, onOrdersClick, onHistoryClick, onChatClick, onProfileClick) }, containerColor = backgroundColor) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Set Dates & Times", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(24.dp))

            Text("Departure Date", fontWeight = FontWeight.Bold)
            CalendarView(
                currentMonth = departureMonth,
                selectedDate = departureDate,
                minDate = minDate,
                onDateSelected = { departureDate = it },
                onMonthChanged = { departureMonth = it }
            )
            Spacer(modifier = Modifier.height(12.dp))
            TimePickerRow(
                label = "Departure Time",
                hour = departureHour,
                minute = departureMinute,
                onHourChange = { departureHour = it },
                onMinuteChange = { departureMinute = it }
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("Arrival Date", fontWeight = FontWeight.Bold)
            CalendarView(
                currentMonth = arrivalMonth,
                selectedDate = arrivalDate,
                minDate = departureDate ?: minDate,
                onDateSelected = { arrivalDate = it },
                onMonthChanged = { arrivalMonth = it }
            )
            Spacer(modifier = Modifier.height(12.dp))
            TimePickerRow(
                label = "Arrival Time",
                hour = arrivalHour,
                minute = arrivalMinute,
                onHourChange = { arrivalHour = it },
                onMinuteChange = { arrivalMinute = it }
            )

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    val dep = LocalDateTime.of(departureDate!!, java.time.LocalTime.of(departureHour, departureMinute))
                    val arr = LocalDateTime.of(arrivalDate!!, java.time.LocalTime.of(arrivalHour, arrivalMinute))
                    onNext(dep, arr)
                },
                enabled = departureDate != null && arrivalDate != null,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Continue")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CreateRouteStep3(
    viewModel: DriverViewModel, // Integrated ViewModel
    onSave: (Double) -> Unit = {},
    onBack: () -> Unit = {},
    isLoading: Boolean = false,
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    currentTab: DriverDestination = DriverDashboard()
) {
    var fare by remember { mutableFloatStateOf(1000f) }
    val context = LocalContext.current
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

    LaunchedEffect(Unit) {
        if (viewModel.previewedPoints.isEmpty()) {
            // Construct a temporary request to get the initial calculated times
            val tempRequest = CreateRouteRequest(
                driverId = 0, // Id doesn't matter for preview math
                departureDate = viewModel.plannedDepartureDate?.format(formatter) ?: "",
                arrivalDate = viewModel.plannedArrivalDate?.format(formatter) ?: "",
                baseFare = fare.toDouble(),
                shipmentType = viewModel.plannedShipmentType,
                points = viewModel.plannedPoints.mapIndexed { index, gp ->
                    RouteCheckpointRequest(
                        name = getAddressFromGeoPoint(context, gp) ?: "Stop ${index + 1}",
                        latitude = gp.latitude,
                        longitude = gp.longitude,
                        sequenceNo = index + 1
                    )
                }
            )
            viewModel.fetchRoutePreview(tempRequest)
        }
    }
    Scaffold(
        bottomBar = { DashboardBottomBar(currentTab, onHomeClick, onOrdersClick, onHistoryClick, onChatClick, onProfileClick) },
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Finalize Journey", fontSize = 24.sp, fontWeight = FontWeight.Bold)

            // --- Fare Slider ---
            Text("PKR ${fare.toInt()}", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = actionTealColor)
            Slider(value = fare, onValueChange = { fare = it }, valueRange = 100f..50000f)

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // --- NEW: Review Schedule Section ---
            Text("Review Schedule", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(8.dp))

            viewModel.previewedPoints.forEachIndexed { index, point ->
                CheckpointTimeItem(
                    point = point,
                    onTimeClick = {
                        showDateTimePicker(context, point.estimatedArrival) { newDateTime ->
                            val updatedList = viewModel.previewedPoints.toMutableList()
                            updatedList[index] = point.copy(estimatedArrival = newDateTime)
                            viewModel.updatePreviewedPoints(updatedList)
                        }
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // --- Shipment Type (Radio Buttons) ---
            Text("Booking Type", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                RadioButton(
                    selected = viewModel.plannedShipmentType == "Shared",
                    onClick = { viewModel.plannedShipmentType = "Shared" }
                )
                Text("Shared")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(
                    selected = viewModel.plannedShipmentType == "Full",
                    onClick = { viewModel.plannedShipmentType = "Full" }
                )
                Text("Full Load")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Preferences Toggles ---
            PreferenceToggle("Accept Fragile Packages", viewModel.plannedIsFragile) { viewModel.plannedIsFragile = it }
            PreferenceToggle("Accept Liquid Packages", viewModel.plannedIsLiquid) { viewModel.plannedIsLiquid = it }
            PreferenceToggle("Accept Flammable Packages", viewModel.plannedIsFlammable) { viewModel.plannedIsFlammable = it }
            PreferenceToggle("Keep Upright", viewModel.plannedKeepUpright) { viewModel.plannedKeepUpright = it }

            Spacer(modifier = Modifier.weight(1f))

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator(color = actionTealColor)
            } else {
                Button(
                    onClick = { onSave(fare.toDouble()) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(if (viewModel.templateRouteForUpdate != null) "Confirm & Update Route"
                    else "Confirm & Create Route")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PreferenceToggle(label: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = actionTealColor)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DelayReportScreen(
    viewModel: DriverViewModel,
    checkpointId: Int,
    checkpointName: String,
    onConfirm: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var days by remember { mutableIntStateOf(0) }
    var hours by remember { mutableIntStateOf(0) }
    var reason by remember { mutableStateOf("") }

    LaunchedEffect(checkpointId) {
        viewModel.fetchEstimatedArrival(checkpointId)
    }

    val rawArrival = viewModel.estimatedArrivalForDelay
    val displayArrival = remember(rawArrival) {
        rawArrival?.let {
            try {
                val clean = it.trim('"') // API may return quoted string
                val datePart = clean.substringBefore("T")
                val timePart = clean.substringAfter("T").substringBeforeLast(":")
                "$datePart at $timePart"
            } catch (e: Exception) { it }
        } ?: "Loading..."
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textThemeColor)
                }
                Text(
                    "Report Delay",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textThemeColor
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Checkpoint Info Card
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = actionTealColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Checkpoint", fontSize = 12.sp, color = textThemeColor.copy(alpha = 0.5f))
                        Text(checkpointName, fontWeight = FontWeight.Bold, color = textThemeColor)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = actionTealColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Current Estimated Arrival", fontSize = 12.sp, color = textThemeColor.copy(alpha = 0.5f))
                        Text(displayArrival, fontWeight = FontWeight.Bold, color = textThemeColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Delay Picker Card
            GlassCard {
                Text(
                    "How much delay?",
                    fontWeight = FontWeight.Bold,
                    color = textThemeColor,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "This will push all future checkpoints forward.",
                    fontSize = 12.sp,
                    color = textThemeColor.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DelayCounter(
                        label = "Days",
                        value = days,
                        onIncrement = { if (days < 30) days++ },
                        onDecrement = { if (days > 0) days-- }
                    )
                    DelayCounter(
                        label = "Hours",
                        value = hours,
                        onIncrement = { if (hours < 23) hours++ },
                        onDecrement = { if (hours > 0) hours-- }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Summary
                val totalHours = days * 24 + hours
                val summaryColor = if (totalHours > 0) statusRedColor else textThemeColor.copy(alpha = 0.4f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(summaryColor.copy(alpha = 0.08f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (totalHours > 0) "Total delay: $totalHours hour${if (totalHours != 1) "s" else ""}"
                        else "No delay selected",
                        fontWeight = FontWeight.Bold,
                        color = summaryColor,
                        fontSize = 15.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("Reason for delay") },
                placeholder = { Text("e.g. Road blocked, weather conditions...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = actionTealColor,
                    focusedLabelColor = actionTealColor,
                    cursorColor = actionTealColor
                )
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (viewModel.isLoading) {
                CircularProgressIndicator(color = actionTealColor)
            } else {
                Button(
                    onClick = {
                        if ((days > 0 || hours > 0) && reason.isNotBlank()) {
                            viewModel.reportDelay(checkpointId, days, hours, reason) {
                                onConfirm()
                            }
                        }
                    },
                    enabled = (days > 0 || hours > 0) && reason.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = statusRedColor,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.2f),
                        disabledContentColor = Color.Gray.copy(alpha = 0.5f)
                    )
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirm Delay", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun DelayCounter(
    label: String,
    value: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 13.sp, color = textThemeColor.copy(alpha = 0.6f), fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onDecrement,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(textThemeColor.copy(alpha = 0.08f))
            ) {
                Icon(Icons.Default.Remove, contentDescription = null, tint = textThemeColor, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                value.toString().padStart(2, '0'),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (value > 0) statusRedColor else textThemeColor.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(
                onClick = onIncrement,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(actionTealColor.copy(alpha = 0.1f))
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = actionTealColor, modifier = Modifier.size(16.dp))
            }
        }
    }
}
@Composable
fun TimePickerRow(
    label: String,
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit
) {
    GlassCard {
        Text(label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hour picker
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { onHourChange((hour + 1) % 24) }) {
                    Icon(Icons.Default.KeyboardArrowUp, null, tint = actionTealColor)
                }
                Text(
                    String.format("%02d", hour),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = actionTealColor
                )
                IconButton(onClick = { onHourChange((hour - 1 + 24) % 24) }) {
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = actionTealColor)
                }
            }

            Text(":", fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))

            // Minute picker
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { onMinuteChange((minute + 15) % 60) }) {
                    Icon(Icons.Default.KeyboardArrowUp, null, tint = actionTealColor)
                }
                Text(
                    String.format("%02d", minute),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = actionTealColor
                )
                IconButton(onClick = { onMinuteChange((minute - 15 + 60) % 60) }) {
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = actionTealColor)
                }
            }
        }
    }
}
@Composable
fun ReviewDialog(
    onSubmit: (rating: Int, comment: String) -> Unit,
    onDismiss: () -> Unit
) {
    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Rate the Customer", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = textThemeColor)
                Text("How was this delivery experience?", fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center)
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                // Star rating row
                Row(horizontalArrangement = Arrangement.Center) {
                    repeat(5) { index ->
                        IconButton(onClick = { rating = index + 1 }) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (index < rating) statusOrangeColor else Color.LightGray,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Leave a comment (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = actionTealColor,
                        focusedLabelColor = actionTealColor
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (rating > 0) onSubmit(rating, comment) },
                enabled = rating > 0,
                colors = ButtonDefaults.buttonColors(containerColor = actionTealColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit Review", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Skip", color = Color.Gray)
            }
        }
    )
}
