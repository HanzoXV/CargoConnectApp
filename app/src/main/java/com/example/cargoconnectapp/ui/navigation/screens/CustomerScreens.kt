package com.example.cargoconnectapp.ui.navigation.screens

import android.Manifest
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.example.cargoconnectapp.ui.models.datamodels.*
import com.example.cargoconnectapp.ui.models.destinations.CustomerDashboard
import com.example.cargoconnectapp.ui.models.destinations.CustomerDestination
import com.example.cargoconnectapp.ui.models.viewmodels.CustomerViewModel
import java.time.LocalDate
import java.time.YearMonth
import com.example.cargoconnectapp.ui.navigation.*
import com.example.cargoconnectapp.ui.themes.*
import com.example.cargoconnectapp.ui.models.map.OSMContainer
import com.example.cargoconnectapp.ui.models.map.getAddressFromGeoPoint
import com.example.cargoconnectapp.data.RetrofitClient
import kotlinx.coroutines.delay
import org.osmdroid.util.GeoPoint
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
fun formatDate(isoString: String?): String {
    if (isoString == null) return "N/A"
    return try {
        val zdt = ZonedDateTime.parse(isoString)
        zdt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
    } catch (e: Exception) {
        try {
            val ld = LocalDate.parse(isoString.substringBefore("T"))
            ld.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        } catch (e2: Exception) {
            isoString
        }
    }
}
fun ChatThreadDto.toChatContact() = ChatContact(
    name = otherName,
    lastMessage = lastMessage,
    time = lastMessageTime?.substringBefore("T") ?: "",
    hasUnread = unreadCount > 0
)

// ─────────────────────────────────────────────
// CUSTOMER DASHBOARD SCREEN
// ─────────────────────────────────────────────

@Composable
fun CustomerDashboardScreen(
    customerViewModel: CustomerViewModel,
    bookings: List<CustomerBooking> = emptyList(),
    onDetailsClick: (CustomerBooking) -> Unit = {},
    onContactDriverClick: (CustomerBooking) -> Unit = {},
    onTrackClick: (CustomerBooking) -> Unit = {},
    onCancelClick: (CustomerBooking) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    currentTab: CustomerDestination = CustomerDashboard()
) {
    val backgroundGradient = Brush.verticalGradient(listOf(backgroundColor, Color.White))
    val total = customerViewModel.confirmedCount + customerViewModel.inTransitCount + customerViewModel.deliveredCount

    Scaffold(
        bottomBar = {
            CustomerBottomBar(
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
                    Text("Customer Dashboard", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = textThemeColor)
                    Text("Ready to ship today?", fontSize = 14.sp, color = textThemeColor.copy(alpha = 0.6f))
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

            // Summary Card
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Total Bookings", fontSize = 13.sp, color = textThemeColor.copy(alpha = 0.6f))
                        Text(customerViewModel.totalBookings.toString(), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = textThemeColor)
                    }
                    Surface(
                        modifier = Modifier.size(60.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = actionTealColor.copy(alpha = 0.1f)
                    ) {
                        Icon(Icons.Default.Inventory2, null, modifier = Modifier.padding(16.dp), tint = actionTealColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Active Shipments", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textThemeColor)
                /*Button(
                    onClick = onNewBookingClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = actionTealColor),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Book New", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }*/
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (bookings.isEmpty()) {
                Text("No active bookings found.", color = Color.Gray, modifier = Modifier.padding(vertical = 12.dp))
            } else {
                bookings.forEach { booking ->
                    CustomerBookingCardOverhauled(
                        booking = booking,
                        onDetails = { onDetailsClick(booking) },
                        onContact = { onContactDriverClick(booking) },
                        onTrack = { onTrackClick(booking) },
                        onCancel = { onCancelClick(booking) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Statistics Card
            GlassCard {
                Text("Shipment Overview", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textThemeColor)
                Spacer(modifier = Modifier.height(16.dp))
                BookingStatRowOverhauled("In-Transit", customerViewModel.inTransitCount, total, statusOrangeColor)
                Spacer(modifier = Modifier.height(12.dp))
                BookingStatRowOverhauled("Assigned", customerViewModel.confirmedCount, total, statusBlueColor)
                Spacer(modifier = Modifier.height(12.dp))
                BookingStatRowOverhauled("Completed", customerViewModel.deliveredCount, total, Color(0xFF4CAF50))
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ─────────────────────────────────────────────
// NEW SHIPMENT FLOW (TAB 2)
// ─────────────────────────────────────────────

@Composable
fun NewShipmentPackagesScreen(
    addedPackages: List<Package> = emptyList(),
    recentPackages: List<RecentPackageDto> = emptyList(),
    onAddNewPackage: () -> Unit = {},
    onRemovePackage: (Package) -> Unit = {},
    onQuickAdd: (RecentPackageDto) -> Unit = {},
    //onBack: () -> Unit = {},
    onNext: () -> Unit = {},
    onCancel: () -> Unit = {},   // clears list + goes back
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    currentTab: CustomerDestination = CustomerDashboard()
) {
    Scaffold(
        bottomBar = { CustomerBottomBar(currentTab, onHomeClick, onOrdersClick, onHistoryClick, onChatClick, onProfileClick) },
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 24.dp)) {
            Spacer(modifier = Modifier.height(32.dp))
            Text("New Shipment", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Text("Items to deliver", fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Packages (${addedPackages.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                IconButton(onClick = onAddNewPackage, modifier = Modifier.background(actionTealColor, CircleShape).size(32.dp)) {
                    Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(addedPackages) { pkg ->
                    // Updated card with KG and Quantity label
                    GlassCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(12.dp), color = statusRedColor.copy(alpha = 0.1f)) {
                                Icon(Icons.Default.Inventory2, null, modifier = Modifier.padding(12.dp), tint = statusRedColor)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(pkg.name, fontWeight = FontWeight.Bold)
                                Text("${pkg.weight} KG • Quantity: ${pkg.quantity}", fontSize = 12.sp, color = Color.Gray)
                            }
                            TextButton(onClick = { onRemovePackage(pkg) }) {
                                Text("Remove", color = statusRedColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }

                // Quick Add Section
                if (recentPackages.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Quick Add",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = textThemeColor.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(recentPackages) { recent ->
                        Surface(
                            onClick = { onQuickAdd(recent) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = actionTealColor.copy(alpha = 0.05f),
                            border = BorderStroke(1.dp, actionTealColor.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AddCircleOutline,
                                    null,
                                    tint = actionTealColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(recent.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text(
                                        "${recent.weight} KG • Quantity: ${recent.quantity} • ${recent.length}x${recent.width}x${recent.height} m",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                                Text(
                                    "+ Add",
                                    color = actionTealColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onCancel,  // clears packages + goes back
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = onNext,
                    enabled = addedPackages.isNotEmpty(),
                    modifier = Modifier.weight(1.5f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = actionTealColor)
                ) {
                    Text("Proceed", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun NewShipmentPackageDetailsScreen(
    customerViewModel: CustomerViewModel,
    onBack: () -> Unit = {},
    onNext: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    currentTab: CustomerDestination = CustomerDashboard()
) {
    Scaffold(
        bottomBar = { CustomerBottomBar(currentTab, onHomeClick, onOrdersClick, onHistoryClick, onChatClick, onProfileClick) },
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(24.dp))
            IconButton(onClick = onBack, modifier = Modifier.background(Color.White, CircleShape)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Item Details", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            
            Spacer(modifier = Modifier.height(24.dp))
            GlassCard {
                AppTextField(value = customerViewModel.packageName, onValueChange = { customerViewModel.packageName = it }, placeholder = "Item Name")
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f)) { AppTextField(value = customerViewModel.packageWeight, onValueChange = { customerViewModel.packageWeight = it }, placeholder = "Weight (kg)") }
                    Box(modifier = Modifier.weight(1f)) { AppTextField(value = customerViewModel.packageQuantity, onValueChange = { customerViewModel.packageQuantity = it }, placeholder = "Quantity") }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Dimensions (meters)", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) { AppTextField(value = customerViewModel.packageLength, onValueChange = { customerViewModel.packageLength = it }, placeholder = "L") }
                    Box(modifier = Modifier.weight(1f)) { AppTextField(value = customerViewModel.packageWidth, onValueChange = { customerViewModel.packageWidth = it }, placeholder = "W") }
                    Box(modifier = Modifier.weight(1f)) { AppTextField(value = customerViewModel.packageHeight, onValueChange = { customerViewModel.packageHeight = it }, placeholder = "H") }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            GlassCard {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FormCheckbox(label = "Fragile", checked = customerViewModel.isFragile, onCheckedChange = { customerViewModel.isFragile = it })
                        FormCheckbox(label = "Liquid", checked = customerViewModel.isLiquid, onCheckedChange = { customerViewModel.isLiquid = it })
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FormCheckbox(label = "Flammable", checked = customerViewModel.isFlammable, onCheckedChange = { customerViewModel.isFlammable = it })
                        FormCheckbox(label = "Keep Upright", checked = customerViewModel.isKeepUpright, onCheckedChange = { customerViewModel.isKeepUpright = it })
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = { customerViewModel.addPackageLocally(); onNext() }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
                Text("Add Package", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun FormCheckbox(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onCheckedChange(!checked) }) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange, colors = CheckboxDefaults.colors(checkedColor = actionTealColor))
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@RequiresPermission(
    allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION]
)
@Composable
fun NewShipmentRouteScreen(
    customerViewModel: CustomerViewModel,
    onBookNow: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    currentTab: CustomerDestination = CustomerDashboard()
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val context = LocalContext.current

    Scaffold(
        bottomBar = { CustomerBottomBar(currentTab, onHomeClick, onOrdersClick, onHistoryClick, onChatClick, onProfileClick) },
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // Map Section
            Box(modifier = Modifier.weight(0.5f).fillMaxWidth()) {
                val routePoints = mutableListOf<GeoPoint>()
                customerViewModel.pickupGeoPoint?.let { routePoints.add(it) }
                customerViewModel.destinationGeoPoint?.let { routePoints.add(it) }
                
                OSMContainer(
                    points = routePoints,
                    circleCenter = customerViewModel.pickupGeoPoint,
                    radiusInMeters = customerViewModel.radius,
                    onMapClick = { geoPoint ->
                        if (customerViewModel.locationSelectionMode == "Pickup") {
                            customerViewModel.pickupGeoPoint = geoPoint
                            customerViewModel.pickupPoint = getAddressFromGeoPoint(context, geoPoint)
                        } else {
                            customerViewModel.destinationGeoPoint = geoPoint
                            customerViewModel.destination = getAddressFromGeoPoint(context, geoPoint)
                        }
                    }
                )
                
                Surface(
                    modifier = Modifier.align(Alignment.TopCenter).padding(16.dp).fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp)),
                    color = Color.White.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "Selecting: ${customerViewModel.locationSelectionMode} Location",
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = actionTealColor
                    )
                }
            }

            // Controls Section (Radius Slider & Form)
            Column(modifier = Modifier.weight(0.5f).fillMaxWidth()) {
                // Radius Slider
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                    Text(
                        "Search Radius: ${String.format("%.1f", customerViewModel.radius / 1000)} KM", 
                        fontSize = 14.sp, 
                        fontWeight = FontWeight.Bold,
                        color = textThemeColor
                    )
                    Slider(
                        value = customerViewModel.radius.toFloat(),
                        onValueChange = { customerViewModel.radius = it.toDouble() },
                        valueRange = 100f..10000f,
                        colors = SliderDefaults.colors(thumbColor = actionTealColor, activeTrackColor = actionTealColor)
                    )
                }

                // Location Fields
                Column(modifier = Modifier.padding(horizontal = 24.dp).weight(1f).verticalScroll(rememberScrollState())) {
                    Text("Route & Schedule", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LocationSelectionField(
                        value = customerViewModel.pickupPoint, 
                        placeholder = "Pickup Address", 
                        icon = Icons.Default.PinDrop,
                        isSelected = customerViewModel.locationSelectionMode == "Pickup",
                        onSelect = { customerViewModel.locationSelectionMode = "Pickup" }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LocationSelectionField(
                        value = customerViewModel.destination, 
                        placeholder = "Delivery Address", 
                        icon = Icons.Default.Flag,
                        isSelected = customerViewModel.locationSelectionMode == "Destination",
                        onSelect = { customerViewModel.locationSelectionMode = "Destination" }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Preferred Date", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    CalendarView(currentMonth = currentMonth, selectedDate = customerViewModel.selectedLocalDate, onDateSelected = { customerViewModel.selectedLocalDate = it }, onMonthChanged = { currentMonth = it })
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    GlassCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(checked = customerViewModel.isStrictSchedule, onCheckedChange = { customerViewModel.isStrictSchedule = it })
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Strict Schedule Only", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Shipment Type", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassCard {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).clickable { customerViewModel.shipmentType = "Full" }) {
                                RadioButton(selected = customerViewModel.shipmentType == "Full", onClick = { customerViewModel.shipmentType = "Full" })
                                Text("Full", fontSize = 14.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).clickable { customerViewModel.shipmentType = "Shared" }) {
                                RadioButton(selected = customerViewModel.shipmentType == "Shared", onClick = { customerViewModel.shipmentType = "Shared" })
                                Text("Shared", fontSize = 14.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 16.dp) {
                    Button(
                        onClick = onBookNow,
                        modifier = Modifier.padding(16.dp).fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = customerViewModel.pickupPoint.isNotBlank() && customerViewModel.destination.isNotBlank() && customerViewModel.selectedLocalDate != null
                    ) {
                        Text("Confirm Route", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@RequiresPermission(
    allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION]
)
@Composable
fun ResearchRouteScreen(
    customerViewModel: CustomerViewModel,
    onBookNow: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    currentTab: CustomerDestination = CustomerDashboard()
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val context = LocalContext.current

    Scaffold(
        bottomBar = { CustomerBottomBar(currentTab, onHomeClick, onOrdersClick, onHistoryClick, onChatClick, onProfileClick) },
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            Box(modifier = Modifier.weight(0.5f).fillMaxWidth()) {
                val routePoints = mutableListOf<GeoPoint>()
                customerViewModel.pickupGeoPoint?.let { routePoints.add(it) }
                customerViewModel.destinationGeoPoint?.let { routePoints.add(it) }

                OSMContainer(
                    points = routePoints,
                    circleCenter = customerViewModel.pickupGeoPoint,
                    radiusInMeters = customerViewModel.radius,
                    onMapClick = { geoPoint ->
                        if (customerViewModel.locationSelectionMode == "Pickup") {
                            customerViewModel.pickupGeoPoint = geoPoint
                            customerViewModel.pickupPoint = getAddressFromGeoPoint(context, geoPoint)
                        } else {
                            customerViewModel.destinationGeoPoint = geoPoint
                            customerViewModel.destination = getAddressFromGeoPoint(context, geoPoint)
                        }
                    }
                )

                Surface(
                    modifier = Modifier.align(Alignment.TopCenter).padding(16.dp).fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp)),
                    color = Color.White.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "Selecting: ${customerViewModel.locationSelectionMode} Location",
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = actionTealColor
                    )
                }
            }

            // Controls Section (Radius Slider & Form)
            Column(modifier = Modifier.weight(0.5f).fillMaxWidth()) {
                // Radius Slider
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                    Text(
                        "Search Radius: ${String.format("%.1f", customerViewModel.radius / 1000)} KM",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textThemeColor
                    )
                    Slider(
                        value = customerViewModel.radius.toFloat(),
                        onValueChange = { customerViewModel.radius = it.toDouble() },
                        valueRange = 100f..10000f,
                        colors = SliderDefaults.colors(thumbColor = actionTealColor, activeTrackColor = actionTealColor)
                    )
                }

                // Location Fields
                Column(modifier = Modifier.padding(horizontal = 24.dp).weight(1f).verticalScroll(rememberScrollState())) {
                    Text("Route & Schedule", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(12.dp))

                    LocationSelectionField(
                        value = customerViewModel.pickupPoint,
                        placeholder = "Pickup Address",
                        icon = Icons.Default.PinDrop,
                        isSelected = customerViewModel.locationSelectionMode == "Pickup",
                        onSelect = { customerViewModel.locationSelectionMode = "Pickup" }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LocationSelectionField(
                        value = customerViewModel.destination,
                        placeholder = "Delivery Address",
                        icon = Icons.Default.Flag,
                        isSelected = customerViewModel.locationSelectionMode == "Destination",
                        onSelect = { customerViewModel.locationSelectionMode = "Destination" }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Preferred Date", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    CalendarView(currentMonth = currentMonth, selectedDate = customerViewModel.selectedLocalDate, onDateSelected = { customerViewModel.selectedLocalDate = it }, onMonthChanged = { currentMonth = it })

                    Spacer(modifier = Modifier.height(16.dp))
                    GlassCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(checked = customerViewModel.isStrictSchedule, onCheckedChange = { customerViewModel.isStrictSchedule = it })
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Strict Schedule Only", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Shipment Type", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassCard {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).clickable { customerViewModel.shipmentType = "Full" }) {
                                RadioButton(selected = customerViewModel.shipmentType == "Full", onClick = { customerViewModel.shipmentType = "Full" })
                                Text("Full", fontSize = 14.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).clickable { customerViewModel.shipmentType = "Shared" }) {
                                RadioButton(selected = customerViewModel.shipmentType == "Shared", onClick = { customerViewModel.shipmentType = "Shared" })
                                Text("Shared", fontSize = 14.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 16.dp) {
                    Button(
                        onClick = onBookNow,
                        modifier = Modifier.padding(16.dp).fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = customerViewModel.pickupPoint.isNotBlank() && customerViewModel.destination.isNotBlank() && customerViewModel.selectedLocalDate != null
                    ) {
                        Text("Confirm & Search", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


@Composable
fun LocationSelectionField(
    value: String,
    placeholder: String,
    icon: ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) actionTealColor.copy(alpha = 0.05f) else Color.White,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) actionTealColor else Color.Gray.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if (isSelected) actionTealColor else Color.Gray)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(placeholder, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text(if (value.isBlank()) "Tap to select on map" else value, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun NewShipmentRecipientScreen(
    customerViewModel: CustomerViewModel,
    onBack: () -> Unit = {},
    onNext: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    currentTab: CustomerDestination = CustomerDashboard()
) {
    Scaffold(
        bottomBar = { CustomerBottomBar(currentTab, onHomeClick, onOrdersClick, onHistoryClick, onChatClick, onProfileClick) },
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 24.dp).verticalScroll(rememberScrollState()).imePadding()) {
            Spacer(modifier = Modifier.height(24.dp))
            IconButton(onClick = onBack, modifier = Modifier.background(Color.White, CircleShape)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Recipient Details", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Text("Who will receive the cargo?", fontSize = 14.sp, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(28.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                AppTextField(value = customerViewModel.recipientFirstName, onValueChange = { customerViewModel.recipientFirstName = it }, placeholder = "First Name")
                AppTextField(value = customerViewModel.recipientLastName, onValueChange = { customerViewModel.recipientLastName = it }, placeholder = "Last Name")
                AppTextField(value = customerViewModel.recipientCnic, onValueChange = { customerViewModel.recipientCnic = it }, placeholder = "CNIC Number")
                AppTextField(value = customerViewModel.recipientContact, onValueChange = { customerViewModel.recipientContact = it }, placeholder = "Contact Number")

                Surface(modifier = Modifier.fillMaxWidth().height(120.dp).shadow(4.dp, RoundedCornerShape(12.dp)), color = Color.White, shape = RoundedCornerShape(12.dp)) {
                    TextField(
                        value = customerViewModel.recipientInstructions,
                        onValueChange = { customerViewModel.recipientInstructions = it },
                        placeholder = { Text("Special Instructions (Optional)", fontSize = 13.sp) },
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
                Text("Continue", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ─────────────────────────────────────────────
// TRACKING & MATCHING
// ─────────────────────────────────────────────

@Composable
fun FindingTruckScreen(
    task: String = "Cargo Delivery",
    tripCost: String = "Calculating...",
    onTimeout: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    currentTab: CustomerDestination = CustomerDashboard()
) {
    LaunchedEffect(Unit) {
        delay(3000)
        onTimeout()
    }

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.7f).background(Color(0xFFCFE8E5)), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Map, null, modifier = Modifier.size(64.dp), tint = actionTealColor.copy(alpha = 0.3f))
                Text("Searching for trucks...", color = actionTealColor)
            }
        }
        Surface(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().shadow(16.dp, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)), shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp), color = Color.White) {
            Column(modifier = Modifier.padding(28.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp, color = actionTealColor)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Finding Drivers", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Matching your request with available nearby trucks.", color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(24.dp))
                GlassCard(modifier = Modifier.background(backgroundColor.copy(alpha = 0.2f), RoundedCornerShape(24.dp))) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = actionTealColor.copy(alpha = 0.1f)) {
                            Icon(Icons.Default.LocalShipping, null, modifier = Modifier.padding(12.dp), tint = actionTealColor)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(task, fontWeight = FontWeight.Bold)
                            Text(tripCost, color = actionTealColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            CustomerBottomBar(currentTab, onHomeClick, onOrdersClick, onHistoryClick, onChatClick, onProfileClick)
        }
    }
}

@Composable
fun DriverFoundScreen(
    availableTrucks: List<AvailabilityDto> = emptyList(),
    onSendRequest: (AvailabilityDto) -> Unit = {},
    onShowDetails: (AvailabilityDto) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    currentTab: CustomerDestination = CustomerDashboard(),
    successMessage: String? = null,
    errorMessage: String? = null,
    onClearMessages: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(successMessage, errorMessage) {
        if (successMessage != null) {
            snackbarHostState.showSnackbar(successMessage)
            onClearMessages()
        }
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
            onClearMessages()
        }
    }

    Scaffold(
        bottomBar = { CustomerBottomBar(currentTab, onHomeClick, onOrdersClick, onHistoryClick, onChatClick, onProfileClick) },
        containerColor = backgroundColor,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 24.dp)) {
            Spacer(modifier = Modifier.height(32.dp))
            Text("Available Trucks", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Text("Select a driver to proceed", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(24.dp))
            
            if (availableTrucks.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No trucks found for this route.", color = Color.Gray, fontWeight = FontWeight.Medium)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                    items(availableTrucks) { truck ->
                        TruckSelectionCard(
                            truck = truck,
                            onSendRequest = { onSendRequest(truck) },
                            onShowDetails = { onShowDetails(truck) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TruckSelectionCard(
    truck: AvailabilityDto,
    onSendRequest: () -> Unit,
    onShowDetails: () -> Unit
) {
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(60.dp), shape = CircleShape, color = actionTealColor.copy(alpha = 0.1f)) {
                Icon(Icons.Default.Person, null, modifier = Modifier.padding(16.dp), tint = actionTealColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(truck.driverName ?: "Unknown Driver", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
                    if (truck.rating != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(truck.rating.toString() + " " + "(${truck.totalReviews.toString()})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(5.dp))
                    }
                    PackagePreferenceTag(truck.prefType, Color.Blue)
                }
                Text(truck.truckModel ?: "Unknown Vehicle", fontSize = 14.sp, color = Color.Gray)
                Text("Fare: Rs ${truck.price}", color = actionTealColor, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if(truck.containsLiquid) PackagePreferenceTag("Liquid", Color.Cyan)
            if(truck.containsFlammable) PackagePreferenceTag("Flammable", Color.Yellow)
            if(truck.containsKeepUpright) PackagePreferenceTag("KeepUpright", Color.Green)
            if(truck.containsFragile) PackagePreferenceTag("Fragile", Color.Red)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onSendRequest, modifier = Modifier.weight(1f).height(40.dp), shape = RoundedCornerShape(12.dp)) {
                Text("Send Request", fontSize = 12.sp)
            }
            Button(onClick = onShowDetails, modifier = Modifier.weight(1f).height(40.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = actionTealColor)) {
                Text("Show Details", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
@Composable
fun PackagePreferenceTag(text: String, color: Color) {
    Row(
        modifier = Modifier
            // 1. Clip the shape first so the background follows the curve
            .clip(RoundedCornerShape(12.dp))
            // 2. Set transparency using .copy(alpha = 0.5f)
            .background(color.copy(alpha = 0.2f))
            .wrapContentSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = text,
            // 3. Keep text fully opaque or slightly dimmed for better contrast
            color = Color.Black,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@androidx.annotation.RequiresPermission(
    allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION])
@Composable
fun DriverDetailsScreen(
    truck: AvailabilityDto,
    routePoints: List<GeoPoint> = emptyList(),
    onSendRequest: () -> Unit = {},
    onBack: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    currentTab: CustomerDestination = CustomerDashboard(),
    successMessage: String? = null,
    errorMessage: String? = null,
    onClearMessages: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(successMessage, errorMessage) {
        if (successMessage != null) {
            snackbarHostState.showSnackbar(successMessage)
            onClearMessages()
        }
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
            onClearMessages()
        }
    }

    fun formatDate(isoString: String?): String {
        if (isoString == null) return "N/A"
        return try {
            val zdt = ZonedDateTime.parse(isoString)
            zdt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        } catch (e: Exception) {
            try {
                // Fallback for plain ISO local date
                val ld = LocalDate.parse(isoString.substringBefore("T"))
                ld.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
            } catch (e2: Exception) {
                isoString // Return as-is if parsing fails
            }
        }
    }

    Scaffold(
        bottomBar = { CustomerBottomBar(currentTab, onHomeClick, onOrdersClick, onHistoryClick, onChatClick, onProfileClick) },
        containerColor = backgroundColor,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // Map Section (Route)
            Box(modifier = Modifier.weight(0.4f).fillMaxWidth())
            {
                OSMContainer(points = routePoints)
                IconButton(
                    onClick = onBack, 
                    modifier = Modifier.padding(16.dp).background(Color.White, CircleShape).align(Alignment.TopStart)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                }
            }

            // Driver Info Section
            Column(modifier = Modifier.weight(0.6f).fillMaxWidth().padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Driver Details", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                        if (truck.rating != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(truck.rating.toString(), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("(${truck.totalReviews ?: 0} Reviews)", color = Color.Gray, fontSize = 14.sp)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                GlassCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(64.dp), shape = CircleShape, color = Color.White) {
                            Icon(Icons.Default.Person, null, modifier = Modifier.padding(16.dp), tint = actionTealColor)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(truck.driverName ?: "N/A", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text(truck.truckModel ?: "N/A", color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Truck Information", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                GlassCard {
                    DetailItem(Icons.Default.LocalShipping, "Model", truck.truckModel ?: "N/A")
                    DetailItem(Icons.Default.Badge, "Vehicle Registration No", truck.licenseNo ?: "N/A")
                    DetailItem(Icons.Default.Scale, "Total Capacity", if (truck.totalCapacity != null) "${truck.totalCapacity} kg" else "N/A")
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Schedule", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                GlassCard {
                    DetailItem(Icons.Default.DepartureBoard, "Departure", formatDate(truck.departureDate ?: truck.requestedDate))
                    DetailItem(Icons.Default.EventAvailable, "Arrival Date", formatDate(truck.arrivalDate))
                }

                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onSendRequest,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = actionTealColor)
                ) {
                    Text("Send Delivery Request", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────
// TAB 3 & 4 & 5
// ─────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
@androidx.annotation.RequiresPermission(
    allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION])
@Composable
fun TrackDriverScreen(
    driverRoute: List<GeoPoint>,
    customerViewModel: CustomerViewModel,
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    currentTab: CustomerDestination = CustomerDashboard()
) {
    Scaffold(
        bottomBar = { CustomerBottomBar(currentTab, onHomeClick, onOrdersClick, onHistoryClick, onChatClick, onProfileClick) },
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            Box(modifier = Modifier.weight(0.5f).fillMaxWidth()) {
                OSMContainer(
                    points = driverRoute,
                    radiusInMeters = 0.0,
                    onMapClick = { },
                    trackDriver = true,
                    customerViewModel = customerViewModel
                )
            }
        }
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun YourBookingsScreen(
    userID: Int,
    customerViewModel: CustomerViewModel,
    activeBookings: List<CustomerBooking> = emptyList(),
    completedBookings: List<CustomerBooking> = emptyList(),
    assignedBookings: List<CustomerBooking> = emptyList(),
    pendingBookings: List<ShipmentDto> = emptyList(),
    onDetailsClick: (CustomerBooking) -> Unit = {},
    onContactDriverClick: (CustomerBooking) -> Unit = {},
    onTrackClick: (CustomerBooking) -> Unit = {},
    onCancelClick: (CustomerBooking) -> Unit = {},
    onReviewClick: (CustomerBooking) -> Unit = {},
    onFindTruckClick: (ShipmentDto) -> Unit = {},
    onDelete: (ShipmentDto) -> Unit = {},
    onShipmentDetailsClick: (ShipmentDto) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    currentTab: CustomerDestination = CustomerDashboard()
) {
    val canReviewMap = remember { mutableStateMapOf<Int, Boolean>() }
    var selectedShipmentForModal by remember { mutableStateOf<ShipmentDto?>(null) }

    Scaffold(bottomBar = { CustomerBottomBar(currentTab, onHomeClick, onOrdersClick, onHistoryClick, onChatClick, onProfileClick) }, containerColor = backgroundColor) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 24.dp)) {
            Spacer(modifier = Modifier.height(32.dp))
            Text("Your Shipments", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(24.dp))
            InteractiveTabRow(
                tabs = listOf("All", "Assigned", "In-Transit", "Completed", "Pending"),
                selectedTab = customerViewModel.customerBookingsSubTab,
                onTabSelected = { customerViewModel.customerBookingsSubTab = it }
            )
            Spacer(modifier = Modifier.height(24.dp))
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 32.dp)) {
                when (customerViewModel.customerBookingsSubTab) {
                    "All" -> {
                        items(assignedBookings) { booking ->
                            CustomerBookingCardOverhauled(booking,
                                onDetails = { onDetailsClick(booking) },
                                onContact = { onContactDriverClick(booking) },
                                onTrack = { onTrackClick(booking) },
                                onCancel = { onCancelClick(booking) }
                            )
                        }
                        items(activeBookings) { booking ->
                            CustomerBookingCardOverhauled(booking,
                                onDetails = { onDetailsClick(booking) },
                                onContact = { onContactDriverClick(booking) },
                                onTrack = { onTrackClick(booking) },
                                onCancel = { onCancelClick(booking) }
                            )
                        }
                        items(completedBookings) { booking ->
                            LaunchedEffect(booking.id) {
                                customerViewModel.checkCanReview(
                                    reviewerUserId = userID,
                                    bookingId = booking.id,
                                    onCanReview = { canReviewMap[booking.id] = true },
                                    onCannotReview = { canReviewMap[booking.id] = false }
                                )
                            }
                            CompletedBookingCardOverhauled(booking,
                                onDetails = { onDetailsClick(booking) },
                                onReview = { onReviewClick(booking) },
                                canReview = canReviewMap[booking.id] ?: true
                            )
                        }
                    }
                    "In-Transit" -> {
                        items(activeBookings) {
                            booking ->
                            CustomerBookingCardOverhauled(booking, onDetails = { onDetailsClick(booking) }, onContact = { onContactDriverClick(booking) }, onTrack = { onTrackClick(booking) }, onCancel = { onCancelClick(booking) })
                        }
                    }
                    "Pending" -> { 
                        items(pendingBookings) { shipment -> 
                            PendingShipmentCard(
                                shipment = shipment, 
                                onFindTruck = { onFindTruckClick(shipment) },
                                onDetails = { selectedShipmentForModal = shipment },
                                onDelete = {onDelete(shipment)}
                            ) 
                        } 
                    }
                    "Completed" -> { items(completedBookings) { booking ->
                        LaunchedEffect(booking.id) {
                            customerViewModel.checkCanReview(
                                reviewerUserId = userID,
                                bookingId = booking.id,
                                onCanReview = { canReviewMap[booking.id] = true },
                                onCannotReview = { canReviewMap[booking.id] = false }
                            )
                        }
                        CompletedBookingCardOverhauled(
                            booking,
                            onDetails = { onDetailsClick(booking) },
                            onReview = { onReviewClick(booking) },
                            canReview = canReviewMap[booking.id]
                                ?: true
                        )
                    }
                    }
                    "Assigned" -> { items(assignedBookings) { booking -> CustomerBookingCardOverhauled(booking, onDetails = { onDetailsClick(booking) }, onContact = { onContactDriverClick(booking) }, onTrack = { onTrackClick(booking) }, onCancel = { onCancelClick(booking) }) } }
                }
            }
        }
    }

    // Details Modal
    if (selectedShipmentForModal != null) {
        PendingShipmentDetailsModal(
            shipment = selectedShipmentForModal!!,
            onClose = { selectedShipmentForModal = null }
        )
    }
}

@Composable
fun PendingShipmentCard(shipment: ShipmentDto, onFindTruck: () -> Unit, onDetails: () -> Unit, onDelete: () -> Unit) {
    GlassCard(modifier = Modifier.clickable { onDetails() }) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(10.dp), color = actionTealColor.copy(alpha = 0.1f)) {
                    Icon(Icons.Default.Pending, null, modifier = Modifier.padding(10.dp), tint = actionTealColor)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Shipment #${shipment.shipmentId}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("${shipment.totalWeight} kg", fontSize = 13.sp, color = actionTealColor, fontWeight = FontWeight.Bold)
                }
            }
            Surface(shape = RoundedCornerShape(8.dp), color = statusBlueColor.copy(alpha = 0.1f)) {
                Text(shipment.status.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = statusBlueColor, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Route", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text("${shipment.pickupAddress} to ${shipment.deliveryAddress}", fontSize = 14.sp, color = textThemeColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onDetails,
                modifier = Modifier.weight(1f).height(40.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, textThemeColor.copy(alpha = 0.1f))
            ) {
                Text("Details", fontSize = 12.sp, color = textThemeColor)
            }
            Button(
                onClick = onDelete,
                modifier = Modifier.weight(1f).height(40.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Icon(Icons.Default.Delete, null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Delete", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onFindTruck,
                modifier = Modifier.weight(1f).height(40.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = actionTealColor)
            ) {
                Icon(Icons.Default.Search, null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Find Truck", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PendingShipmentDetailsModal(
    shipment: ShipmentDto,
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = {
            TextButton(onClick = onClose) {
                Text("Close", color = actionTealColor, fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Text("Shipment Details", fontWeight = FontWeight.ExtraBold, color = textThemeColor)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailItem(Icons.Default.PinDrop, "Pickup Location", shipment.pickupAddress)
                DetailItem(Icons.Default.Flag, "Delivery Location", shipment.deliveryAddress)
                
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Recipient", fontSize = 10.sp, color = Color.Gray)
                        Text("${shipment.recipientFname} ${shipment.recipientLname ?: ""}", fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Contact", fontSize = 10.sp, color = Color.Gray)
                        Text(shipment.recipientContact ?: "N/A", fontWeight = FontWeight.Bold)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Total Weight", fontSize = 10.sp, color = Color.Gray)
                        Text("${shipment.totalWeight} kg", fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Radius", fontSize = 10.sp, color = Color.Gray)
                        Text("${shipment.shipmentRadius ?: 0.0} m", fontWeight = FontWeight.Bold)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Quantity", fontSize = 10.sp, color = Color.Gray)
                        Text("${shipment.packages?.size ?: 0} units", fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Type", fontSize = 10.sp, color = Color.Gray)
                        Text(shipment.shipmentType ?: "N/A", fontWeight = FontWeight.Bold)
                    }
                }

                DetailItem(Icons.Default.CalendarToday, "Booking Date", formatDate(shipment.bookingDate))
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

@Composable
fun CustomerActivityScreen(
    chats: List<ChatContact> = emptyList(),
    notifications: List<NotificationDto> = emptyList(),
    onChatClick: (ChatContact) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatNavClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    currentTab: CustomerDestination = CustomerDashboard()
) {
    var selectedTab by remember { mutableStateOf("Chats") }
    Scaffold(bottomBar = { CustomerBottomBar(currentTab, onHomeClick, onOrdersClick, onHistoryClick, onChatNavClick, onProfileClick) }, containerColor = backgroundColor) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 24.dp)) {
            Spacer(modifier = Modifier.height(32.dp))
            Text("Activity", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(24.dp))
            InteractiveTabRow(tabs = listOf("Chats", "Notifications"), selectedTab = selectedTab, onTabSelected = { selectedTab = it })
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (selectedTab == "Chats") items(chats) { EnhancedChatListItem(it, onClick = { onChatClick(it) }) }
                else items(notifications) { EnhancedNotificationCard(it) }
            }
        }
    }
}

@Composable
fun CustomerProfileScreen(
    fullName: String = "John Doe",
    profileImageUrl: String? = null,
    rating: Double = 4.9,
    numberOfReviews: Int = 0,
    onYourInformationClick: () -> Unit = {},
    onRequestHistoryClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onReviewsClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    currentTab: CustomerDestination = CustomerDashboard()
) {
    Scaffold(bottomBar = { CustomerBottomBar(currentTab, onHomeClick, onOrdersClick, onHistoryClick, onChatClick, onProfileClick) }, containerColor = backgroundColor) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.fillMaxWidth().height(260.dp).drawBehind { drawCircle(Brush.radialGradient(listOf(actionTealColor.copy(alpha = 0.15f), Color.Transparent)), radius = size.width, center = Offset(size.width/2, 0f)) }, contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(modifier = Modifier.size(110.dp).shadow(12.dp, CircleShape), shape = CircleShape, color = Color.White) { 
                        val fullImageUrl = if (profileImageUrl?.startsWith("api/uploads/") == true) {
                            RetrofitClient.BASE_URL + profileImageUrl
                        } else {
                            profileImageUrl
                        }
                        if (fullImageUrl != null) {
                            AsyncImage(
                                model = fullImageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Person, null, modifier = Modifier.padding(24.dp), tint = actionTealColor) 
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(fullName, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            null,
                            tint = statusOrangeColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            " $rating Customer Rating ($numberOfReviews)",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                //ProfileMenuCard("Shipment History", Icons.Default.History, onBookingDetailsClick)
                ProfileMenuCard("Request History", Icons.Default.Assignment, onRequestHistoryClick)
                ProfileMenuCard("Reviews", Icons.Default.Assignment, onReviewsClick)
                ProfileMenuCard("Personal Details", Icons.Default.Badge, onYourInformationClick)
                Button(onClick = onLogoutClick, modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 12.dp), colors = ButtonDefaults.buttonColors(containerColor = statusRedColor.copy(alpha = 0.1f), contentColor = statusRedColor), shape = RoundedCornerShape(16.dp)) { Text("Logout", fontWeight = FontWeight.Bold) }
            }
        }
    }
}
@Composable
fun CustomerInformationScreen(
    fullName: String = "",
    phone: String = "",
    email: String = "",
    address: String = "",
    city: String = "",
    cnic: String = "",
    onBack: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    currentTab: CustomerDestination = CustomerDashboard()
) {
    Scaffold(bottomBar = { CustomerBottomBar(currentTab, onHomeClick, onOrdersClick, onHistoryClick, onChatClick, onProfileClick) }, containerColor = backgroundColor) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(Color.White, CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Personal Details", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)

            Spacer(modifier = Modifier.height(24.dp))
            GlassCard {
                DetailRow("Full Name", fullName)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color.Gray.copy(alpha = 0.1f)
                )
                DetailRow("CNIC", cnic)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color.Gray.copy(alpha = 0.1f)
                )
                DetailRow("Phone", phone)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color.Gray.copy(alpha = 0.1f)
                )
                DetailRow("Email", email)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color.Gray.copy(alpha = 0.1f)
                )
                DetailRow("Address", address)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color.Gray.copy(alpha = 0.1f)
                )
                DetailRow("City", city)
            }

            Spacer(modifier = Modifier.height(32.dp))
            /*Button(
                onClick = onEditClick,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = actionTealColor)
            ) {
                Text("Edit Profile", fontWeight = FontWeight.Bold)
            }*/
        }
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RequestHistoryScreen(
    requests: List<CustomerRequestDto>,
    isLoading: Boolean,
    onBack: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    currentTab: CustomerDestination = CustomerDashboard()
) {
    Scaffold(
        bottomBar = { CustomerBottomBar(currentTab, onHomeClick, onOrdersClick, onHistoryClick, onChatClick, onProfileClick) },
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 24.dp)) {
            Spacer(modifier = Modifier.height(24.dp))
            IconButton(onClick = onBack, modifier = Modifier.background(Color.White, CircleShape)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Request History", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Text("Status of your sent requests", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = actionTealColor)
                }
            } else if (requests.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No requests found.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(requests) { request ->
                        CustomerRequestCard(request)
                    }
                }
            }
        }
    }
}
@Composable
fun CustomerReviewsScreen(
    reviews: List<ReviewDto>,
    averageRating: Double,
    totalReviews: Int,
    isLoading: Boolean,
    onBack: () -> Unit,
    currentTab: CustomerDestination,
    onHomeClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onChatClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Scaffold(
        bottomBar = { CustomerBottomBar(currentTab, onHomeClick, onOrdersClick, onHistoryClick, onChatClick, onProfileClick) },
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = textThemeColor
                    )
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CustomerRequestCard(request: CustomerRequestDto) {
    GlassCard {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Request #${request.RequestId}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(formatDate(request.DateRequested), fontSize = 12.sp, color = Color.Gray)
            }
            RequestStatusBadge(request.Status)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp), tint = actionTealColor)
            Spacer(modifier = Modifier.width(8.dp))
            Text(request.DriverName, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.PinDrop, null, modifier = Modifier.size(14.dp).padding(top = 2.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text(request.PickupLocation ?: "N/A", fontSize = 13.sp, color = textThemeColor.copy(alpha = 0.8f))
            }
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Flag, null, modifier = Modifier.size(14.dp).padding(top = 2.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text(request.DropoffLocation ?: "N/A", fontSize = 13.sp, color = textThemeColor.copy(alpha = 0.8f))
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        Text("Fare: Rs ${request.Fare}", color = actionTealColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

@Composable
fun RequestStatusBadge(status: String) {
    val color = when (status.lowercase()) {
        "pending" -> statusBlueColor
        "accepted", "approved" -> Color(0xFF4CAF50)
        "rejected", "canceled" -> statusRedColor
        else -> Color.Gray
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = status.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun CancelBookingScreen(
    onBack: () -> Unit = {},
    onConfirmCancel: (String) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    currentTab: CustomerDestination = CustomerDashboard()
) {
    var reason by remember { mutableStateOf("") }
    Scaffold(bottomBar = { CustomerBottomBar(currentTab, onHomeClick, onOrdersClick, onHistoryClick, onChatClick, onProfileClick) }, containerColor = backgroundColor) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(24.dp))
            IconButton(onClick = onBack, modifier = Modifier.background(Color.White, CircleShape)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Cancel Booking", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(24.dp))
            GlassCard { TextField(value = reason, onValueChange = { reason = it }, placeholder = { Text("Cancellation Reason...") }, modifier = Modifier.fillMaxWidth().height(120.dp), colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)) }
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = { onConfirmCancel(reason) }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = statusRedColor)) { Text("Confirm Cancellation") }
        }
    }
}

@Composable
fun ReviewScreen(
    customerViewModel: CustomerViewModel,
    onBack: () -> Unit = {},
    onSubmit: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    currentTab: CustomerDestination = CustomerDashboard()
) {
    val context = LocalContext.current
    val data = customerViewModel.canReviewData

    Scaffold(
        bottomBar = { CustomerBottomBar(currentTab, onHomeClick, onOrdersClick, onHistoryClick, onChatClick, onProfileClick) },
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
            Text("Rate your driver", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Text(data?.targetName ?: "Driver", fontSize = 18.sp, color = actionTealColor, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 5 Star Rating Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(5) { index ->
                    val starIndex = index + 1
                    Icon(
                        imageVector = if (starIndex <= customerViewModel.reviewRating) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { customerViewModel.reviewRating = starIndex },
                        tint = if (starIndex <= customerViewModel.reviewRating) actionTealColor else Color.Gray.copy(alpha = 0.5f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Comments (Optional)", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth().height(150.dp).shadow(4.dp, RoundedCornerShape(12.dp)),
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            ) {
                TextField(
                    value = customerViewModel.reviewComments,
                    onValueChange = { customerViewModel.reviewComments = it },
                    placeholder = { Text("How was your experience?", fontSize = 14.sp) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onSubmit,
                enabled = customerViewModel.reviewRating > 0 && !customerViewModel.isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = actionTealColor)
            ) {
                if (customerViewModel.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Submit Review", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun BookingShipmentsDetailScreen(
    packages: List<PackageDto> = emptyList(),
    pickupAddress: String = "Karachi",
    deliveryAddress: String = "Lahore",
    totalWeight: String = "500kg",
    pickupDate: String = "Sep 12",
    recpName: String = "N/A",
    recpContact: String = "N/A",
    onBack: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    currentTab: CustomerDestination = CustomerDashboard()
) {
    Scaffold(bottomBar = { CustomerBottomBar(currentTab, onHomeClick, onOrdersClick, onHistoryClick, onChatClick, onProfileClick) }, containerColor = backgroundColor) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(24.dp))
            IconButton(onClick = onBack, modifier = Modifier.background(Color.White, CircleShape)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Shipment Details", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            
            Spacer(modifier = Modifier.height(24.dp))
            GlassCard {
                DetailItem(Icons.Default.PinDrop, "From", pickupAddress)
                DetailItem(Icons.Default.Flag, "To", deliveryAddress)
                DetailItem(Icons.Default.Scale, "Weight", totalWeight)
                DetailItem(Icons.Default.CalendarToday, "Date", pickupDate)
                DetailItem(Icons.Default.CalendarToday, "Recipient Name", recpName)
                DetailItem(Icons.Default.CalendarToday, "Recipient Contact", recpContact)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Packages (${packages.size})", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
            packages.forEach { pkg ->
                GlassCard(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(pkg.name, fontWeight = FontWeight.Bold)
                    Text("Qty: ${pkg.quantity} • ${pkg.length}x${pkg.width}x${pkg.height} m", fontSize = 12.sp, color = Color.Gray)
                    Text("Weight: ${pkg.weight} kg", fontSize = 12.sp, color = actionTealColor, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// REUSABLE COMPONENTS
// ─────────────────────────────────────────────

@Composable
fun PackageCardOverhauled(pkg: Package, actionLabel: String, actionColor: Color, onAction: () -> Unit) {
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(12.dp), color = actionColor.copy(alpha = 0.1f)) {
                Icon(Icons.Default.Inventory2, null, modifier = Modifier.padding(12.dp), tint = actionColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(pkg.name, fontWeight = FontWeight.Bold)
                Text("${pkg.weight} • ${pkg.quantity} units", fontSize = 12.sp, color = Color.Gray)
            }
            TextButton(onClick = onAction) {
                Text(actionLabel, color = actionColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun CustomerBookingCardOverhauled(
    booking: CustomerBooking,
    onDetails: () -> Unit,
    onContact: () -> Unit,
    onTrack: () -> Unit,
    onCancel: () -> Unit
) {


    GlassCard(modifier = Modifier.clickable { onDetails() }) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(10.dp), color = actionTealColor.copy(alpha = 0.1f)) {
                    Icon(Icons.Default.Inventory2, null, modifier = Modifier.padding(10.dp), tint = actionTealColor)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(booking.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = booking.amount, fontSize = 13.sp, color = actionTealColor, fontWeight = FontWeight.Bold)
                }
            }
            BookingStatusBadgeOverhauled(status = booking.status)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Route", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(booking.route, fontSize = 14.sp, color = textThemeColor, maxLines = 1, overflow = TextOverflow.Ellipsis)

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons row 1
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onDetails,
                modifier = Modifier.weight(1f).height(40.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, textThemeColor.copy(alpha = 0.1f))
            ) {
                Text("Details", fontSize = 12.sp, color = textThemeColor)
            }

            if (booking.status == BookingStatus.InTransit) {
                Button(
                    onClick = onTrack,
                    modifier = Modifier.weight(1.5f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = actionTealColor)
                ) {
                    Icon(Icons.Default.MyLocation, null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Track Live", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else if (booking.status == BookingStatus.Assigned) {
                Button(
                    onClick = onCancel,
                    modifier = Modifier.weight(1.5f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = statusRedColor.copy(alpha = 0.1f),
                        contentColor = statusRedColor
                    )
                ) {
                    Text("Cancel", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Contact Driver button — only for Assigned and InTransit
        if (booking.status == BookingStatus.Assigned || booking.status == BookingStatus.InTransit) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onContact,
                modifier = Modifier.fillMaxWidth().height(40.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, actionTealColor.copy(alpha = 0.4f))
            ) {
                Icon(Icons.Default.Chat, null, modifier = Modifier.size(14.dp), tint = actionTealColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Contact Driver", fontSize = 12.sp, color = actionTealColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CompletedBookingCardOverhauled(booking: CustomerBooking, onDetails: () -> Unit, onReview: () -> Unit, canReview: Boolean) {
    GlassCard(modifier = Modifier.clickable { onDetails() }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = Color.Gray.copy(alpha = 0.1f)) { Icon(Icons.Default.CheckCircle, null, modifier = Modifier.padding(10.dp), tint = Color.Gray) }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(booking.title, fontWeight = FontWeight.Bold)
                Text(booking.route, fontSize = 12.sp, color = Color.Gray, maxLines = 1)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onDetails,
                modifier = Modifier.weight(0.8f).heightIn(min = 40.dp),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) { Text("Details", fontSize = 12.sp) }
            Button(
                onClick = onReview,
                enabled = canReview,
                modifier = Modifier.weight(1f).heightIn(min = 40.dp),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    // Set the background color when the button is disabled
                    disabledContainerColor = Color.LightGray.copy(alpha = 0.5f),
                    // Set the text/icon color when the button is disabled
                    disabledContentColor = Color.Gray
                ))
            {
                Text(
                    if (canReview) "Review" else "Already Reviewed",
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
fun BookingStatRowOverhauled(label: String, count: Int, total: Int, color: Color) {
    val percent = if (total > 0) count. LeonardOf(total.toFloat()) else 0f
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = textThemeColor.copy(alpha = 0.7f))
            Text("$count", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textThemeColor)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(color.copy(alpha = 0.1f))) {
            Box(modifier = Modifier.fillMaxWidth(percent).fillMaxHeight().clip(CircleShape).background(color))
        }
    }
}

private fun Int.LeonardOf(total: Float): Float {
    return if (total > 0) this / total else 0f
}

@Composable
fun BookingStatusBadgeOverhauled(status: BookingStatus) {
    val (label, color) = when (status) {
        BookingStatus.InTransit -> "In-Transit" to statusOrangeColor
        BookingStatus.Assigned -> "Assigned" to statusBlueColor
        BookingStatus.Completed -> "Completed" to Color(0xFF4CAF50)
        BookingStatus.Canceled -> "Canceled" to statusRedColor
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = label.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Preview
@Composable
fun displaycancelscreen () {
    val truck = AvailabilityDto(
        1,
        pickupLat = 0.0,
        pickupLong = 0.0,
        destLat = 0.0,
        destLong = 0.0,
        requestedDate = "",
        isStrict = false,
        containsLiquid = true,
        containsFragile = true,
        containsKeepUpright = true,
        containsFlammable = true
    )
    TruckSelectionCard(
        truck = truck,
        onSendRequest = { },
        onShowDetails = {  }
    )
}

