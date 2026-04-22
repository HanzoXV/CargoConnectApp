package com.example.cargoconnectapp.ui.navigation.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cargoconnectapp.ui.navigation.*
import com.example.cargoconnectapp.ui.themes.*
import java.time.LocalDate
import java.time.YearMonth


data class ShipmentRequest(
    val title: String,
    val route: String,
    val weight: String,
    val pickup: String,
    val amount: String,
    val isShared: Boolean = false
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

data class NotificationItem(
    val message: String,
    val time: String,
    val hasUnread: Boolean
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


// ─────────────────────────────────────────────
// DRIVER DASHBOARD SCREEN
// ─────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DriverDashboardScreen(
    routeStops: List<Triple<String, String, String>> = listOf(
        Triple("Peshawar", "Haji Camp Truck Adda", "~0KM"),
        Triple("Rawalpindi", "Kohistan Truck Stand", "~170KM"),
        Triple("Multan", "Vehari Road Truck Stop", "~330KM"),
        Triple("Karachi", "Mauripur Truck Stand", "~700KM")
    ),
    reachedStops: Set<Int> = setOf(1, 2, 3),
    currentCapacityKg: Int = 1200,
    totalCapacityKg: Int = 3000,
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onDelayClick: (Int) -> Unit = {},
    onReachedClick: (Int) -> Unit = {}
) {
    val backgroundGradient = remember {
        Brush.verticalGradient(listOf(Color(0xFFE0F2F1), backgroundColor))
    }

    var selectedTab by remember { mutableStateOf("Trip") }
    var currentCapacity by remember { mutableStateOf(currentCapacityKg.toFloat()) }
    var vehicleLength by remember { mutableStateOf(30f) }
    var vehicleWidth by remember { mutableStateOf(30f) }
    var vehicleHeight by remember { mutableStateOf(30f) }

    Scaffold(
        bottomBar = {
            DashboardBottomBar(
                onHomeClick = onHomeClick,
                onOrdersClick = onOrdersClick,
                onHistoryClick = onHistoryClick,
                onChatClick = onChatClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(innerPadding)
                .padding(horizontal = 28.dp)
                .padding(top = 48.dp)
                .verticalScroll(rememberScrollState())
        ) {
            AppHeading("Dashboard")
            AddSpace(16)

            // Tab Switcher
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TabButton(text = "Trip Info", isSelected = selectedTab == "Trip", onClick = { selectedTab = "Trip" })
                TabButton(text = "Truck Info", isSelected = selectedTab == "Truck", onClick = { selectedTab = "Truck" })
            }

            AddSpace(20)

            if (selectedTab == "Trip") {
                // ── TRIP INFO TAB ──
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Route Info", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textThemeColor)
                        AddSpace(12)
                        routeStops.forEachIndexed { index, (city, address, distance) ->
                            val isReached = index in reachedStops
                            RouteItem(
                                city = city,
                                address = address,
                                distance = distance,
                                isReached = isReached,
                                onDelayClick = { onDelayClick(index) },
                                onReachedClick = { onReachedClick(index) }
                            )
                            if (index < routeStops.lastIndex) {
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
                            }
                        }
                    }
                }

                AddSpace(16)

                // Map placeholder
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFCFE8E5),
                    shadowElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Map View", color = Color.Gray, fontSize = 14.sp)
                    }
                }

            } else {
                // ── TRUCK INFO TAB ──

                // Truck Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Replace with actual truck image resource
                    Surface(
                        modifier = Modifier.size(200.dp, 140.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFF9C4)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🚛", fontSize = 64.sp)
                        }
                    }
                }

                AddSpace(8)

                // Capacity Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Current Capacity", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = textThemeColor)
                        AddSpace(12)

                        val capacityPercent = currentCapacity / totalCapacityKg
                        LinearProgressIndicator(
                            progress = { capacityPercent },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = actionTealColor,
                            trackColor = Color.LightGray.copy(alpha = 0.3f)
                        )
                        Text(
                            text = "${(capacityPercent * 100).toInt()}%",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                        )

                        AddSpace(16)

                        CounterRow(
                            label = "Current Capacity",
                            value = "${currentCapacity.toInt()} KGs",
                            onDecrease = { if (currentCapacity > 0) currentCapacity -= 100 },
                            onIncrease = { if (currentCapacity < totalCapacityKg) currentCapacity += 100 }
                        )
                        AddSpace(8)
                        InfoRow(label = "Total Capacity", value = "$totalCapacityKg KGs")
                    }
                }

                AddSpace(16)

                // Dimensions
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Adjust Dimensions", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = textThemeColor)
                        AddSpace(12)
                        CounterRow(
                            label = "Length",
                            value = "${vehicleLength.toInt()}cm",
                            onDecrease = { if (vehicleLength > 0) vehicleLength -= 5 },
                            onIncrease = { vehicleLength += 5 }
                        )
                        AddSpace(8)
                        CounterRow(
                            label = "Width",
                            value = "${vehicleWidth.toInt()}cm",
                            onDecrease = { if (vehicleWidth > 0) vehicleWidth -= 5 },
                            onIncrease = { vehicleWidth += 5 }
                        )
                        AddSpace(8)
                        CounterRow(
                            label = "Height",
                            value = "${vehicleHeight.toInt()}cm",
                            onDecrease = { if (vehicleHeight > 0) vehicleHeight -= 5 },
                            onIncrease = { vehicleHeight += 5 }
                        )
                    }
                }
            }

            AddSpace(24)
        }
    }
}


// ─────────────────────────────────────────────
// DASHBOARD - SHIPMENTS SUB-VIEW
// ─────────────────────────────────────────────

@Composable
fun DashboardShipmentsScreen(
    shipments: List<ShipmentDetail> = listOf(
        ShipmentDetail(
            title = "Electronics Household",
            totalWeight = "~580 KGs",
            recipientName = "Kamran Ali",
            totalFare = "25570.0 PKR",
            packages = listOf(
                ShipmentPackage("Refrigerator", 1, "Red", "PESHAWA#122025"),
                ShipmentPackage("Sofa", 3, "Red", "PESHAWA#122026")
            )
        ),
        ShipmentDetail(
            title = "Sofa Set",
            totalWeight = "~270 KGs",
            recipientName = "Kamran Ali",
            totalFare = "14570.0 PKR",
            packages = listOf(
                ShipmentPackage("Refrigerator", 1, "Red", "PESHAWA#122025")
            )
        )
    ),
    onBack: () -> Unit = {},
    onConfirmDelivery: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val backgroundGradient = remember {
        Brush.verticalGradient(listOf(Color(0xFFE0F2F1), backgroundColor))
    }

    Scaffold(
        bottomBar = {
            DashboardBottomBar(
                onHomeClick = onHomeClick,
                onOrdersClick = onOrdersClick,
                onHistoryClick = onHistoryClick,
                onChatClick = onChatClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(innerPadding)
                .padding(horizontal = 28.dp)
                .padding(top = 48.dp)
                .verticalScroll(rememberScrollState())
        ) {
            AppHeading("Dashboard")
            AddSpace(16)

            // Back row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onBack() }
                    .padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = textThemeColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Shipments", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = textThemeColor)
            }

            shipments.forEach { shipment ->
                ShipmentDetailCard(shipment = shipment)
                AddSpace(12)
            }

            AddSpace(16)

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                AppButton(
                    text = "Confirm Delivery",
                    onClick = onConfirmDelivery,
                    width = 220.dp
                )
            }

            AddSpace(24)
        }
    }
}

@Composable
fun ShipmentDetailCard(shipment: ShipmentDetail) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(shipment.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textThemeColor)
            Text("Total Weight: ${shipment.totalWeight}", fontSize = 12.sp, color = Color.Gray)
            Text("Recipient's Name: ${shipment.recipientName}", fontSize = 12.sp, color = Color.Gray)
            AddSpace(10)
            shipment.packages.forEachIndexed { i, pkg ->
                Text("Package ${i + 1}: ${pkg.name}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textThemeColor)
                Text("Quantity: ${pkg.quantity}", fontSize = 12.sp, color = Color.Gray)
                Text("Tag Color: ${pkg.tagColor}", fontSize = 12.sp, color = Color.Gray)
                Text("Tag ID: ${pkg.tagId}", fontSize = 12.sp, color = Color.Gray)
                if (i < shipment.packages.lastIndex) AddSpace(8)
            }
            AddSpace(10)
            Text("Total Fare: ${shipment.totalFare}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textThemeColor)
        }
    }
}


// ─────────────────────────────────────────────
// SHIPMENT REQUESTS SCREEN
// ─────────────────────────────────────────────

@Composable
fun ShipmentRequestsScreen(
    requests: List<ShipmentRequest> = listOf(
        ShipmentRequest("Electronics Household (Private)", "Karachi to Peshawar", "~580 KGs", "Pickup at 02/09/2025...", "Amount: PKR 8,500,..."),
        ShipmentRequest("Cement Bags (Shared)", "Multan to Peshawar", "~70 KGs", "Pickup at 02/09/2025", "Amount: PKR 65,000,...", true),
        ShipmentRequest("Sofa Set (Shared)", "Karachi to Peshawar", "~600 KGs", "Pickup at 02/09/2025", "Amount: PKR 85,000,...", true),
        ShipmentRequest("Glassware (Shared)", "Multan to Peshawar", "~5 KGs", "Pickup at 02/09/2025", "Amount: PKR 115,000,...", true),
    ),
    onDetailsClick: (ShipmentRequest) -> Unit = {},
    onAccept: (ShipmentRequest) -> Unit = {},
    onDecline: (ShipmentRequest) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val backgroundGradient = remember {
        Brush.verticalGradient(listOf(Color(0xFFE0F2F1), backgroundColor))
    }

    Scaffold(
        bottomBar = {
            DashboardBottomBar(
                onHomeClick = onHomeClick,
                onOrdersClick = onOrdersClick,
                onHistoryClick = onHistoryClick,
                onChatClick = onChatClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(innerPadding)
                .padding(horizontal = 28.dp)
                .padding(top = 48.dp)
                .verticalScroll(rememberScrollState())
        ) {
            AppHeading("Shipment Requests")
            AddSpace(12)

            Text(
                text = "${requests.size} Requests Found",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.End)
            )
            AddSpace(12)

            requests.forEach { request ->
                ShipmentRequestCard(
                    request = request,
                    onDetailsClick = { onDetailsClick(request) },
                    onAccept = { onAccept(request) },
                    onDecline = { onDecline(request) }
                )
                AddSpace(12)
            }

            AddSpace(24)
        }
    }
}

@Composable
fun ShipmentRequestCard(
    request: ShipmentRequest,
    onDetailsClick: () -> Unit,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(request.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textThemeColor)
                Text(request.route, fontSize = 12.sp, color = Color.Gray)
                Text(request.weight, fontSize = 12.sp, color = Color.Gray)
                Text(request.pickup, fontSize = 12.sp, color = Color.Gray)
                Text(request.amount, fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SmallActionButton(
                    text = "Details",
                    containerColor = Color(0xFF333333),
                    enabled = true,
                    onClick = onDetailsClick
                )
                SmallActionButton(
                    text = "Accept",
                    containerColor = Color(0xFF4CAF50),
                    enabled = true,
                    onClick = onAccept
                )
                SmallActionButton(
                    text = "Decline",
                    containerColor = Color(0xFFFF1744),
                    enabled = true,
                    onClick = onDecline
                )
            }
        }
    }
}


// ─────────────────────────────────────────────
// SHIPMENT REQUEST DETAIL SCREEN
// ─────────────────────────────────────────────

@Composable
fun ShipmentRequestDetailScreen(
    request: ShipmentRequest = ShipmentRequest(
        "Electronics Household (Private)",
        "Karachi to Peshawar",
        "~580 KGs",
        "02, September, 2025",
        "14570.0 PKR"
    ),
    packages: List<ShipmentPackage> = listOf(
        ShipmentPackage("Refrigerator", 1, "Red", "PESHAWA#122025", "15cm x 15cm x 4cm", "~280 KGs", "Keep Upright"),
        ShipmentPackage("Sofa", 3, "Red", "PESHAWA#122026", "15cm x 15cm x 4cm", "~300 KGs")
    ),
    pickupAddress: String = "Shabir Chowk, Karachi",
    deliveryAddress: String = "Main Stand, Peshawar",
    onBack: () -> Unit = {},
    onAccept: () -> Unit = {},
    onDecline: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val backgroundGradient = remember {
        Brush.verticalGradient(listOf(Color(0xFFE0F2F1), backgroundColor))
    }

    Scaffold(
        bottomBar = {
            DashboardBottomBar(
                onHomeClick = onHomeClick,
                onOrdersClick = onOrdersClick,
                onHistoryClick = onHistoryClick,
                onChatClick = onChatClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(innerPadding)
                .padding(horizontal = 28.dp)
                .padding(top = 48.dp)
                .verticalScroll(rememberScrollState())
        ) {
            AppHeading("Shipment Requests")
            AddSpace(16)

            // Back row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onBack() }
                    .padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = textThemeColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Request Details", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = textThemeColor)
            }

            // Main card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(request.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textThemeColor)
                            AddSpace(4)
                            Text("Pickup Address: $pickupAddress", fontSize = 12.sp, color = Color.Gray)
                            Text("Delivery Address: $deliveryAddress", fontSize = 12.sp, color = Color.Gray)
                            Text("Total Weight: ${request.weight}", fontSize = 12.sp, color = Color.Gray)
                            Text("Pickup Date: ${request.pickup}", fontSize = 12.sp, color = Color.Gray)
                        }
                        // Truck icon placeholder
                        Surface(
                            modifier = Modifier.size(60.dp, 40.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFF9C4)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🚛", fontSize = 24.sp)
                            }
                        }
                    }

                    AddSpace(16)

                    packages.forEachIndexed { i, pkg ->
                        Text(
                            "Package ${i + 1}: ${pkg.name}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = textThemeColor
                        )
                        Text("Quantity: ${pkg.quantity}", fontSize = 12.sp, color = Color.Gray)
                        if (pkg.dimensions != null) {
                            Text("Dimensions", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                            Text("  Length: ${pkg.dimensions.split("x")[0].trim()}", fontSize = 12.sp, color = Color.Gray)
                            Text("  Width: ${pkg.dimensions.split("x").getOrNull(1)?.trim() ?: ""}", fontSize = 12.sp, color = Color.Gray)
                            Text("  Height: ${pkg.dimensions.split("x").getOrNull(2)?.trim() ?: ""}", fontSize = 12.sp, color = Color.Gray)
                        }
                        if (pkg.weight != null) Text("Weight: ${pkg.weight}", fontSize = 12.sp, color = Color.Gray)
                        if (pkg.note != null) Text(pkg.note, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                        if (i < packages.lastIndex) AddSpace(10)
                    }

                    AddSpace(12)
                    Text("Total Fare: ${request.amount}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textThemeColor)
                }
            }

            AddSpace(24)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Decline", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Accept", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            AddSpace(24)
        }
    }
}


// ─────────────────────────────────────────────
// CUSTOMERS SCREEN
// ─────────────────────────────────────────────

@Composable
fun CustomersScreen(
    activeDeliveries: List<CustomerDelivery> = listOf(
        CustomerDelivery("Faheem", "#TR1064", "House Electronics", "Vihari Truck Adda", "Multan"),
        CustomerDelivery("Sarfaraz", "#TR1065", "House Electronics", "Vihari Truck Adda", "Multan")
    ),
    pendingPickups: List<CustomerDelivery> = listOf(
        CustomerDelivery("Faheem", "#TR1064", "House Electronics", "Vihari Truck Adda", "Multan")
    ),
    upcomingPickups: List<CustomerDelivery> = listOf(
        CustomerDelivery("Faheem", "#TR1064", "House Electronics", "Vihari Truck Adda", "Multan", "In 4 DAYS"),
        CustomerDelivery("Sarfaraz", "#TR1065", "Cement Bags", "Mauripur Truck Stand", "Karachi", "In 6 DAYS")
    ),
    onDetailsClick: (CustomerDelivery) -> Unit = {},
    onConfirmClick: (CustomerDelivery) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val backgroundGradient = remember {
        Brush.verticalGradient(listOf(Color(0xFFE0F2F1), backgroundColor))
    }

    Scaffold(
        bottomBar = {
            DashboardBottomBar(
                onHomeClick = onHomeClick,
                onOrdersClick = onOrdersClick,
                onHistoryClick = onHistoryClick,
                onChatClick = onChatClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(innerPadding)
                .padding(horizontal = 28.dp)
                .padding(top = 48.dp)
                .verticalScroll(rememberScrollState())
        ) {
            AppHeading("Customers")
            AddSpace(20)

            // Active Deliveries
            AppSectionHeader("Active Deliveries")
            activeDeliveries.forEach { delivery ->
                CustomerDeliveryCard(
                    delivery = delivery,
                    showConfirm = false,
                    onDetailsClick = { onDetailsClick(delivery) }
                )
                AddSpace(8)
            }

            AddSpace(16)

            // Pending Pickups
            AppSectionHeader("Pending Pickups")
            pendingPickups.forEach { delivery ->
                CustomerDeliveryCard(
                    delivery = delivery,
                    showConfirm = true,
                    onDetailsClick = { onDetailsClick(delivery) },
                    onConfirmClick = { onConfirmClick(delivery) }
                )
                AddSpace(8)
            }

            AddSpace(16)

            // Upcoming Pickups
            AppSectionHeader("Upcoming Pickups")
            upcomingPickups.forEach { delivery ->
                CustomerDeliveryCard(
                    delivery = delivery,
                    showConfirm = false,
                    onDetailsClick = { onDetailsClick(delivery) }
                )
                AddSpace(8)
            }

            AddSpace(24)
        }
    }
}

@Composable
fun CustomerDeliveryCard(
    delivery: CustomerDelivery,
    showConfirm: Boolean,
    onDetailsClick: () -> Unit,
    onConfirmClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${delivery.customerName} ${delivery.trackingId}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = textThemeColor
                )
                Text(
                    text = "Shipment Description: ${delivery.description}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Checkpoint: ${delivery.checkpoint}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = delivery.city,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                if (delivery.daysUntil != null) {
                    Text(
                        text = delivery.daysUntil,
                        fontSize = 12.sp,
                        color = actionTealColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (showConfirm) {
                    SmallActionButton(
                        text = "Confirm",
                        containerColor = Color(0xFF4CAF50),
                        enabled = true,
                        onClick = onConfirmClick
                    )
                }
                SmallActionButton(
                    text = "Details",
                    containerColor = Color(0xFF333333),
                    enabled = true,
                    onClick = onDetailsClick
                )
            }
        }
    }
}


// ─────────────────────────────────────────────
// CUSTOMER SHIPMENTS DETAIL (Customers > Shipments)
// ─────────────────────────────────────────────

@Composable
fun CustomerShipmentsDetailScreen(
    shipments: List<ShipmentDetail> = listOf(
        ShipmentDetail(
            title = "Electronics Household",
            totalWeight = "~580 KGs",
            recipientName = "Kamran Ali",
            totalFare = "25570.0 PKR",
            packages = listOf(
                ShipmentPackage("Refrigerator", 1, "Red", "PESHAWA#122025"),
                ShipmentPackage("Sofa", 3, "Red", "PESHAWA#122026")
            )
        ),
        ShipmentDetail(
            title = "Sofa Set",
            totalWeight = "~270 KGs",
            recipientName = "Kamran Ali",
            totalFare = "14570.0 PKR",
            packages = listOf(
                ShipmentPackage("Refrigerator", 1, "Red", "PESHAWA#122025")
            )
        )
    ),
    onBack: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val backgroundGradient = remember {
        Brush.verticalGradient(listOf(Color(0xFFE0F2F1), backgroundColor))
    }

    Scaffold(
        bottomBar = {
            DashboardBottomBar(
                onHomeClick = onHomeClick,
                onOrdersClick = onOrdersClick,
                onHistoryClick = onHistoryClick,
                onChatClick = onChatClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(innerPadding)
                .padding(horizontal = 28.dp)
                .padding(top = 48.dp)
                .verticalScroll(rememberScrollState())
        ) {
            AppHeading("Customers")
            AddSpace(16)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onBack() }
                    .padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = textThemeColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Shipments", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = textThemeColor)
            }

            shipments.forEach { shipment ->
                ShipmentDetailCard(shipment = shipment)
                AddSpace(12)
            }

            AddSpace(24)
        }
    }
}


// ─────────────────────────────────────────────
// ACTIVITY SCREEN (Chat + Notifications)
// ─────────────────────────────────────────────

@Composable
fun ActivityScreen(
    chats: List<ChatContact> = listOf(
        ChatContact("Kamran", "Hello.", "1h", true),
        ChatContact("Waleed", "Received", "1h", true),
        ChatContact("Customer Support", "", "1h", false, isSupport = true)
    ),
    notifications: List<NotificationItem> = listOf(
        NotificationItem("You have received a review from Ahmed on (Karachi to Lahore)", "1h", true),
        NotificationItem("Waleed sent you a message", "3h", true)
    ),
    onChatClick: (ChatContact) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatNavClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val backgroundGradient = remember {
        Brush.verticalGradient(listOf(Color(0xFFE0F2F1), backgroundColor))
    }

    var selectedTab by remember { mutableStateOf("Chats") }

    Scaffold(
        bottomBar = {
            DashboardBottomBar(
                onHomeClick = onHomeClick,
                onOrdersClick = onOrdersClick,
                onHistoryClick = onHistoryClick,
                onChatClick = onChatNavClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(innerPadding)
                .padding(horizontal = 28.dp)
                .padding(top = 48.dp)
        ) {
            AppHeading("Activity")
            AddSpace(16)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TabButton(text = "Chats", isSelected = selectedTab == "Chats", onClick = { selectedTab = "Chats" })
                TabButton(text = "Notifications", isSelected = selectedTab == "Notifications", onClick = { selectedTab = "Notifications" })
            }

            AddSpace(16)

            if (selectedTab == "Chats") {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    chats.forEach { chat ->
                        ChatListItem(chat = chat, onClick = { onChatClick(chat) })
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    }
                }
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    notifications.forEach { notification ->
                        NotificationCard(notification = notification)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatListItem(chat: ChatContact, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Unread dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(if (chat.hasUnread) Color.Red else Color.Transparent, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))

        // Avatar
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (chat.isSupport) Color(0xFF1B5E20) else Color(0xFFBDBDBD)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(chat.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = textThemeColor)
            if (chat.lastMessage.isNotEmpty()) {
                Text(chat.lastMessage, fontSize = 13.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        Text(chat.time, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun NotificationCard(notification: NotificationItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .offset(y = 4.dp)
                    .background(if (notification.hasUnread) Color.Red else Color.Transparent, CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(notification.message, fontSize = 13.sp, color = textThemeColor)
                AddSpace(4)
                Text("Click to see the details", fontSize = 11.sp, color = Color.Gray)
            }
            Text(notification.time, fontSize = 12.sp, color = Color.Gray)
        }
    }
}


// ─────────────────────────────────────────────
// PROFILE SCREEN
// ─────────────────────────────────────────────

@Composable
fun ProfileScreen(
    profileImageUrl: String? = null,
    rating: Float = 4.0f,
    isSwitchedToCustomer: Boolean = false,
    onYourInformationClick: () -> Unit = {},
    onActivateRouteClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onSwitchRoleToggle: (Boolean) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val backgroundGradient = remember {
        Brush.verticalGradient(listOf(Color(0xFFE0F2F1), backgroundColor))
    }

    var switchState by remember { mutableStateOf(isSwitchedToCustomer) }

    Scaffold(
        bottomBar = {
            DashboardBottomBar(
                onHomeClick = onHomeClick,
                onOrdersClick = onOrdersClick,
                onHistoryClick = onHistoryClick,
                onChatClick = onChatClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(innerPadding)
                .padding(horizontal = 28.dp)
                .padding(top = 48.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppHeading("Your Profile", modifier = Modifier.fillMaxWidth())
            AddSpace(32)

            // Profile Avatar
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE1D5F0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(80.dp),
                    tint = Color(0xFF5C3D8F)
                )
            }

            AddSpace(20)

            // Star Rating
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                val fullStars = rating.toInt()
                val hasHalf = rating - fullStars >= 0.5f
                val totalStars = 5
                repeat(totalStars) { i ->
                    val starColor = when {
                        i < fullStars -> Color(0xFFFFB300)
                        i == fullStars && hasHalf -> Color(0xFFFFB300).copy(alpha = 0.5f)
                        else -> Color(0xFFE0E0E0)
                    }
                    Text("★", fontSize = 28.sp, color = starColor)
                }
            }

            AddSpace(32)

            // Info and Route menu items
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column {
                    ProfileMenuItem(text = "Your information", onClick = onYourInformationClick)
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
                    ProfileMenuItem(text = "Activate Route", onClick = onActivateRouteClick)
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            AddSpace(32)

            HorizontalDivider(color = Color.LightGray)
            AddSpace(16)

            // Logout + Switch row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onLogoutClick() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFFF1744), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("—", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Logout", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textThemeColor)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Switch to\nCustomer", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(end = 8.dp))
                    Switch(
                        checked = switchState,
                        onCheckedChange = {
                            switchState = it
                            onSwitchRoleToggle(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF333333),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.Gray
                        )
                    )
                }
            }

            AddSpace(24)
        }
    }
}


// ─────────────────────────────────────────────
// DELIVERY DELAY SCREEN
// ─────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DeliveryDelayScreen(
    onBack: () -> Unit = {},
    onConfirmDelay: (LocalDate?, String, Int, Boolean) -> Unit = { _, _, _, _ -> },
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val backgroundGradient = remember {
        Brush.verticalGradient(listOf(Color(0xFFE0F2F1), backgroundColor))
    }

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var reason by remember { mutableStateOf("") }
    var selectedHour by remember { mutableStateOf(7) }
    var isAm by remember { mutableStateOf(true) }

    Scaffold(
        bottomBar = {
            DashboardBottomBar(
                onHomeClick = onHomeClick,
                onOrdersClick = onOrdersClick,
                onHistoryClick = onHistoryClick,
                onChatClick = onChatClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(innerPadding)
                .padding(horizontal = 28.dp)
                .padding(top = 48.dp)
                .verticalScroll(rememberScrollState())
        ) {
            AppHeading("Delivery Delay")
            AddSpace(20)

            // Time picker card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onBack() }.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textThemeColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Expected Delivery Time", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textThemeColor)
                    }

                    AddSpace(20)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Hour display
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF3E5F5)
                        ) {
                            Text(
                                text = "%02d".format(selectedHour),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6A0DAD),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        Text(" : ", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = textThemeColor)

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF3E5F5)
                        ) {
                            Text(
                                text = "00",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6A0DAD),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // AM/PM
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isAm) Color(0xFFF3E5F5) else Color.LightGray.copy(alpha = 0.3f),
                                modifier = Modifier.clickable { isAm = true }
                            ) {
                                Text("AM", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    fontWeight = FontWeight.Bold, color = if (isAm) Color(0xFF6A0DAD) else Color.Gray)
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (!isAm) Color(0xFFF3E5F5) else Color.LightGray.copy(alpha = 0.3f),
                                modifier = Modifier.clickable { isAm = false }
                            ) {
                                Text("PM", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    fontWeight = FontWeight.Bold, color = if (!isAm) Color(0xFF6A0DAD) else Color.Gray)
                            }
                        }
                    }
                }
            }

            AddSpace(20)

            // Date picker
            AppSectionHeader("Expected Delivery Date")
            AddSpace(12)

            CalendarView(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it },
                onMonthChanged = { currentMonth = it }
            )

            AddSpace(20)

            // Reason input
            Surface(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.LightGray.copy(alpha = 0.3f)
            ) {
                TextField(
                    value = reason,
                    onValueChange = { reason = it },
                    placeholder = { Text("Reason", color = Color.Gray) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxSize()
                )
            }

            AddSpace(24)

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                AppButton(
                    text = "Confirm delay",
                    onClick = { onConfirmDelay(selectedDate, reason, selectedHour, isAm) },
                    width = 220.dp
                )
            }

            AddSpace(24)
        }
    }
}


// ─────────────────────────────────────────────
// CHAT SCREEN
// ─────────────────────────────────────────────

data class ChatMessage(
    val content: String,
    val isSentByMe: Boolean,
    val timestamp: String? = null
)

@Composable
fun ChatScreen(
    contactName: String = "Kamran",
    contactStatus: String = "Active 11m ago",
    contactImageUrl: String? = null,
    messages: List<ChatMessage> = listOf(
        ChatMessage("I will be there in 2 hours", isSentByMe = true, timestamp = "Oct 07, 2025, 9:41 AM"),
        ChatMessage("ok", isSentByMe = false),
        ChatMessage("drop at the location", isSentByMe = false),
        ChatMessage("i will send someone to unload", isSentByMe = false)
    ),
    onBack: () -> Unit = {},
    onCallClick: () -> Unit = {},
    onSendMessage: (String) -> Unit = {}
) {
    val backgroundGradient = remember {
        Brush.verticalGradient(listOf(Color(0xFFE0F2F1), backgroundColor))
    }

    var messageText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .imePadding()
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundGradient)
                .padding(horizontal = 16.dp)
                .padding(top = 48.dp, bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textThemeColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    ProfileIcon(imageUrl = contactImageUrl, onClick = {})
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(contactName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textThemeColor)
                        Text(contactStatus, fontSize = 12.sp, color = Color.Gray)
                    }
                }
                IconButton(onClick = onCallClick) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = "Call", tint = textThemeColor)
                }
            }
        }

        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

        // Messages
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            messages.forEach { message ->
                if (message.timestamp != null) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(message.timestamp, fontSize = 11.sp, color = Color.Gray)
                    }
                    AddSpace(4)
                }
                ChatBubble(message = message)
            }
        }

        // Input bar
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Message...", color = Color.Gray, fontSize = 14.sp) },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )
            IconButton(onClick = { }) {
                Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice", tint = Color.Gray)
            }
            IconButton(onClick = { }) {
                Icon(imageVector = Icons.Default.SentimentSatisfied, contentDescription = "Emoji", tint = Color.Gray)
            }
            IconButton(onClick = {
                if (messageText.isNotBlank()) {
                    onSendMessage(messageText)
                    messageText = ""
                }
            }) {
                Icon(imageVector = Icons.Default.Image, contentDescription = "Attach", tint = Color.Gray)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun PreviewDriverDashboardScreen() {
    DriverDashboardScreen()
}

@Preview(showBackground = true)
@Composable
fun PreviewDashboardShipmentsScreen() {
    DashboardShipmentsScreen()
}

@Preview(showBackground = true)
@Composable
fun PreviewShipmentRequestsScreen() {
    ShipmentRequestsScreen()
}

@Preview(showBackground = true)
@Composable
fun PreviewShipmentRequestDetailScreen() {
    ShipmentRequestDetailScreen()
}

@Preview(showBackground = true)
@Composable
fun PreviewCustomersScreen() {
    CustomersScreen()
}

@Preview(showBackground = true)
@Composable
fun PreviewCustomerShipmentsDetailScreen() {
    CustomerShipmentsDetailScreen()
}

@Preview(showBackground = true)
@Composable
fun PreviewActivityScreen() {
    ActivityScreen()
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileScreen() {
    ProfileScreen()
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun PreviewDeliveryDelayScreen() {
    DeliveryDelayScreen()
}

@Preview(showBackground = true)
@Composable
fun PreviewChatScreen() {
    ChatScreen()
}