package com.example.cargoconnectapp.ui.navigation.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.tooling.preview.Preview
import java.time.LocalDate
import java.time.YearMonth
import com.example.cargoconnectapp.ui.navigation.*
import com.example.cargoconnectapp.ui.themes.*


// ─────────────────────────────────────────────
// SHARED DATA MODELS (Customer-side)
// ─────────────────────────────────────────────

data class CustomerBooking(
    val title: String,
    val route: String,
    val amount: String,
    val status: BookingStatus
)

enum class BookingStatus { InTransit, Confirmed, Completed, Reviewed }

data class AvailableTruck(
    val shipmentTitle: String,
    val route: String,
    val amount: String,
    val badge: TruckBadge
)

enum class TruckBadge { Shared, Full }

data class Package(
    val name: String,
    val dimensions: String,
    val weight: String,
    val quantity: Int,
    val fragile: Boolean = false
)

data class DriverProfile(
    val name: String,
    val email: String,
    val location: String,
    val memberSince: String,
    val isTopRated: Boolean,
    val rating: Float,
    val reviewerName: String,
    val reviewDate: String,
    val reviewText: String,
    val vehicleModel: String,
    val vehicleType: String,
    val licensePlate: String,
    val color: String,
    val year: String,
    val capacity: String,
    val registrationNo: String
)


// ─────────────────────────────────────────────
// CUSTOMER DASHBOARD SCREEN
// ─────────────────────────────────────────────

@Composable
fun CustomerDashboardScreen(
    totalBookings: Int = 2,
    bookingGrowth: String = "+12% from last month",
    bookings: List<CustomerBooking> = listOf(
        CustomerBooking("Electronics", "Karachi to Multan", "PKR 8,500", BookingStatus.InTransit),
        CustomerBooking("Chemical", "Karachi to Multan", "PKR 8,500", BookingStatus.Confirmed)
    ),
    confirmedCount: Int = 2,
    inTransitCount: Int = 1,
    deliveredCount: Int = 0,
    onNewBookingClick: () -> Unit = {},
    onDetailsClick: (CustomerBooking) -> Unit = {},
    onContactDriverClick: (CustomerBooking) -> Unit = {},
    onTrackClick: (CustomerBooking) -> Unit = {},
    onCancelClick: (CustomerBooking) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val backgroundGradient = remember {
        Brush.verticalGradient(listOf(Color(0xFFE0F2F1), backgroundColor))
    }
    val total = confirmedCount + inTransitCount + deliveredCount

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
            AddSpace(20)

            // Total Bookings card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text("Total Bookings", fontSize = 13.sp, color = Color.Gray)
                        AddSpace(4)
                        Text(
                            text = totalBookings.toString(),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = textThemeColor
                        )
                        AddSpace(4)
                        Text(bookingGrowth, fontSize = 11.sp, color = Color(0xFF4CAF50))
                    }
                    Icon(
                        imageVector = Icons.Default.Person, // replace with package icon
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            AddSpace(20)

            // Your Shipments header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Your Shipments", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = textThemeColor)
                Surface(
                    onClick = onNewBookingClick,
                    shape = RoundedCornerShape(20.dp),
                    color = actionTealColor
                ) {
                    Text(
                        text = "New Booking +",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            AddSpace(12)

            bookings.forEach { booking ->
                CustomerBookingCard(
                    booking = booking,
                    onDetailsClick = { onDetailsClick(booking) },
                    onContactDriverClick = { onContactDriverClick(booking) },
                    onTrackClick = { onTrackClick(booking) },
                    onCancelClick = { onCancelClick(booking) }
                )
                AddSpace(12)
            }

            AddSpace(20)

            // Booking Statistics
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Booking Statistics", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textThemeColor)
                    AddSpace(4)
                    Text("Breakdown by status", fontSize = 12.sp, color = Color.Gray)
                    AddSpace(16)

                    BookingStatRow("Confirmed", confirmedCount, total, Color(0xFF2196F3))
                    AddSpace(10)
                    BookingStatRow("In-Transit", inTransitCount, total, Color(0xFF9E9E9E))
                    AddSpace(10)
                    BookingStatRow("Delivered", deliveredCount, total, Color(0xFFFF9800))
                }
            }

            AddSpace(24)
        }
    }
}

@Composable
fun BookingStatRow(label: String, count: Int, total: Int, color: Color) {
    val percent = if (total > 0) count.toFloat() / total.toFloat() else 0f
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = textThemeColor, modifier = Modifier.width(80.dp))
        LinearProgressIndicator(
            progress = { percent },
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color.LightGray.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$count(${(percent * 100).toInt()}%)",
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier.width(56.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun CustomerBookingCard(
    booking: CustomerBooking,
    onDetailsClick: () -> Unit,
    onContactDriverClick: () -> Unit,
    onTrackClick: () -> Unit = {},
    onCancelClick: () -> Unit = {}
) {
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = actionTealColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(booking.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textThemeColor)
                }
                BookingStatusBadge(status = booking.status)
            }

            AddSpace(8)
            Text("Route", fontSize = 11.sp, color = Color.Gray)
            Text(booking.route, fontSize = 13.sp, color = textThemeColor)
            AddSpace(4)
            Text("Amount", fontSize = 11.sp, color = Color.Gray)
            Text(booking.amount, fontSize = 13.sp, color = textThemeColor)
            AddSpace(12)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallActionButton(text = "Details", containerColor = Color(0xFF333333), enabled = true, onClick = onDetailsClick)
                SmallActionButton(text = "Contact Driver", containerColor = Color(0xFF555555), enabled = true, onClick = onContactDriverClick)
                if (booking.status == BookingStatus.InTransit) {
                    SmallActionButton(text = "Track", containerColor = Color(0xFF4CAF50), enabled = true, onClick = onTrackClick)
                }
                if (booking.status == BookingStatus.Confirmed) {
                    SmallActionButton(text = "Cancel", containerColor = Color(0xFFFF1744), enabled = true, onClick = onCancelClick)
                }
            }
        }
    }
}

@Composable
fun BookingStatusBadge(status: BookingStatus) {
    val (label, color) = when (status) {
        BookingStatus.InTransit -> "In-transit" to Color(0xFFFFA726)
        BookingStatus.Confirmed -> "Confirmed" to Color(0xFF2196F3)
        BookingStatus.Completed -> "Completed" to Color(0xFF4CAF50)
        BookingStatus.Reviewed -> "Reviewed" to Color.LightGray
    }
    Surface(shape = RoundedCornerShape(20.dp), color = color) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}


// ─────────────────────────────────────────────
// NEW SHIPMENT - ROUTE SCREEN
// ─────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NewShipmentRouteScreen(
    pickupPoint: String = "",
    whereTo: String = "",
    onPickupChange: (String) -> Unit = {},
    onWhereToChange: (String) -> Unit = {},
    onBookNow: () -> Unit = {},
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
    var strictSchedule by remember { mutableStateOf(false) }
    var pickup by remember { mutableStateOf(pickupPoint) }
    var destination by remember { mutableStateOf(whereTo) }

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
                .verticalScroll(rememberScrollState())
        ) {
            // Map placeholder at top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Color(0xFFCFE8E5)),
                contentAlignment = Alignment.TopStart
            ) {
                Text(
                    "New Shipment",
                    modifier = Modifier.padding(20.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = textThemeColor
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
                    .padding(top = 16.dp)
            ) {
                // Pickup input
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(3.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = actionTealColor, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        TextField(
                            value = pickup,
                            onValueChange = { pickup = it; onPickupChange(it) },
                            placeholder = { Text("Pickup point", color = Color.Gray, fontSize = 14.sp) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                AddSpace(10)

                // Where to input
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(3.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        TextField(
                            value = destination,
                            onValueChange = { destination = it; onWhereToChange(it) },
                            placeholder = { Text("Where to?", color = Color.Gray, fontSize = 14.sp) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                AddSpace(20)

                Text(
                    "Choose Preferred Booking Date",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = textThemeColor,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                AddSpace(12)

                CalendarView(
                    currentMonth = currentMonth,
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it },
                    onMonthChanged = { currentMonth = it }
                )

                AddSpace(16)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { strictSchedule = !strictSchedule }
                ) {
                    Checkbox(
                        checked = strictSchedule,
                        onCheckedChange = { strictSchedule = it },
                        colors = CheckboxDefaults.colors(checkedColor = actionTealColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Strict Schedule", fontSize = 14.sp, color = textThemeColor)
                }

                AddSpace(20)

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    AppButton(text = "Book Now", onClick = onBookNow, width = 200.dp)
                }

                AddSpace(24)
            }
        }
    }
}


// ─────────────────────────────────────────────
// NEW SHIPMENT - PACKAGES SCREEN
// ─────────────────────────────────────────────

@Composable
fun NewShipmentPackagesScreen(
    addedPackages: List<Package> = listOf(
        Package("Electronics Household", "15cm / 23cm / 2cm", "~580 KGs", 1, fragile = true)
    ),
    quickAddPackages: List<Package> = listOf(
        Package("Mangoes Crate", "15cm / 23cm / 2cm", "~60 KGs", 1, fragile = true)
    ),
    onAddNewPackage: () -> Unit = {},
    onRemovePackage: (Package) -> Unit = {},
    onQuickAdd: (Package) -> Unit = {},
    onBack: () -> Unit = {},
    onNext: () -> Unit = {},
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
            AppHeading("New Shipment")
            AppSubHeading("Add a New Package")
            AddSpace(20)

            // Added packages header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Packages", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = textThemeColor)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${addedPackages.size} Package Added", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onAddNewPackage,
                        modifier = Modifier
                            .size(32.dp)
                            .background(actionTealColor, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }

            AddSpace(12)

            addedPackages.forEach { pkg ->
                PackageItemCard(
                    pkg = pkg,
                    actionLabel = "Remove",
                    actionColor = Color(0xFFFF1744),
                    onAction = { onRemovePackage(pkg) }
                )
                AddSpace(8)
            }

            AddSpace(16)

            Text("Quick Add", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = textThemeColor)
            AddSpace(12)

            quickAddPackages.forEach { pkg ->
                PackageItemCard(
                    pkg = pkg,
                    actionLabel = "Add",
                    actionColor = Color(0xFF4CAF50),
                    onAction = { onQuickAdd(pkg) }
                )
                AddSpace(8)
            }

            Spacer(modifier = Modifier.weight(1f))
            AddSpace(24)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppButton(
                    text = "Back",
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    width = 0.dp,
                    backgroundColor = Color(0xFF555555)
                )
                AppButton(
                    text = "Next",
                    onClick = onNext,
                    modifier = Modifier.weight(1f),
                    width = 0.dp
                )
            }

            AddSpace(24)
        }
    }
}

@Composable
fun PackageItemCard(
    pkg: Package,
    actionLabel: String,
    actionColor: Color,
    onAction: () -> Unit
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(pkg.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textThemeColor)
                Text(pkg.dimensions, fontSize = 12.sp, color = Color.Gray)
                Text(pkg.weight, fontSize = 12.sp, color = Color.Gray)
                Text("Quantity ${pkg.quantity}", fontSize = 12.sp, color = Color.Gray)
                if (pkg.fragile) Text("Fragile", fontSize = 12.sp, color = Color(0xFFFF6F00))
            }
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = actionColor),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(actionLabel, color = Color.White, fontSize = 13.sp)
            }
        }
    }
}


// ─────────────────────────────────────────────
// NEW SHIPMENT - PACKAGE DETAILS SCREEN
// ─────────────────────────────────────────────

@Composable
fun NewShipmentPackageDetailsScreen(
    onBack: () -> Unit = {},
    onNext: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val backgroundGradient = remember {
        Brush.verticalGradient(listOf(Color(0xFFE0F2F1), backgroundColor))
    }

    var name by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var length by remember { mutableStateOf("") }
    var width by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var fragile by remember { mutableStateOf(true) }
    var liquid by remember { mutableStateOf(true) }
    var flammable by remember { mutableStateOf(true) }
    var keepUpright by remember { mutableStateOf(true) }

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
            AppHeading("New Shipment")
            AppSubHeading("Add a New Package")
            AddSpace(24)

            AppSectionHeader("Basic")
            AddSpace(8)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AppTextField(value = name, onValueChange = { name = it }, placeholder = "Name")
                AppTextField(value = weight, onValueChange = { weight = it }, placeholder = "Weight")
                AppTextField(value = quantity, onValueChange = { quantity = it }, placeholder = "Quantity")
            }

            AddSpace(20)

            AppSectionHeader("Dimensions (in centimeters)")
            AddSpace(8)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AppTextField(value = length, onValueChange = { length = it }, placeholder = "Length (eg. 17cm)")
                AppTextField(value = width, onValueChange = { width = it }, placeholder = "Width (eg. 17cm)")
                AppTextField(value = height, onValueChange = { height = it }, placeholder = "Height (eg. 17cm)")
            }

            AddSpace(20)

            AppSectionHeader("Essentials")
            AddSpace(8)

            // Two-column checkbox layout
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    EssentialCheckbox(label = "Fragile", checked = fragile, onCheckedChange = { fragile = it })
                    EssentialCheckbox(label = "Flammable", checked = flammable, onCheckedChange = { flammable = it })
                    EssentialCheckbox(label = "Keep Upright", checked = keepUpright, onCheckedChange = { keepUpright = it })
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    EssentialCheckbox(label = "Liquid", checked = liquid, onCheckedChange = { liquid = it })
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            AddSpace(24)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppButton(
                    text = "Back",
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    width = 0.dp,
                    backgroundColor = Color(0xFF555555)
                )
                AppButton(
                    text = "Next",
                    onClick = onNext,
                    modifier = Modifier.weight(1f),
                    width = 0.dp
                )
            }

            AddSpace(24)
        }
    }
}

@Composable
fun EssentialCheckbox(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onCheckedChange(!checked) }
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = actionTealColor),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, fontSize = 14.sp, color = textThemeColor)
    }
}


// ─────────────────────────────────────────────
// NEW SHIPMENT - RECIPIENT DETAILS SCREEN
// ─────────────────────────────────────────────

@Composable
fun NewShipmentRecipientScreen(
    onBack: () -> Unit = {},
    onNext: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val backgroundGradient = remember {
        Brush.verticalGradient(listOf(Color(0xFFE0F2F1), backgroundColor))
    }

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var cnic by remember { mutableStateOf("") }
    var contactNo by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }

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
                .imePadding()
        ) {
            AppHeading("New Shipment")
            AppSubHeading("Recipient Details")
            AddSpace(28)

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AppTextField(value = firstName, onValueChange = { firstName = it }, placeholder = "First Name")
                AppTextField(value = lastName, onValueChange = { lastName = it }, placeholder = "Last Name")
                AppTextField(value = cnic, onValueChange = { cnic = it }, placeholder = "CNIC")
                AppTextField(value = contactNo, onValueChange = { contactNo = it }, placeholder = "Contact No")

                // Instructions multiline
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .shadow(4.dp, RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    TextField(
                        value = instructions,
                        onValueChange = { instructions = it },
                        placeholder = { Text("Instructions (Optional)", color = Color.Gray, fontSize = 13.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        maxLines = 5,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            AddSpace(24)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppButton(
                    text = "Back",
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    width = 0.dp,
                    backgroundColor = Color(0xFF555555)
                )
                AppButton(
                    text = "Next",
                    onClick = onNext,
                    modifier = Modifier.weight(1f),
                    width = 0.dp
                )
            }

            AddSpace(24)
        }
    }
}


// ─────────────────────────────────────────────
// FINDING TRUCK / LOADING SCREEN
// ─────────────────────────────────────────────

@Composable
fun FindingTruckScreen(
    task: String = "Chemical Delivery",
    departedDate: String = "20 Feb, 05:00 PM",
    currentLocation: String = "123 Main Street, Anytown, Pak 845103",
    tripCost: String = "Rs 10000",
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val backgroundGradient = remember {
        Brush.verticalGradient(listOf(Color(0xFFE0F2F1), backgroundColor))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Map fills top portion
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f)
                .background(Color(0xFFCFE8E5)),
            contentAlignment = Alignment.Center
        ) {
            Text("Map View", color = Color.Gray)
        }

        // Bottom sheet
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Finding Truck", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = textThemeColor)
                AddSpace(6)
                Text(
                    "Effortlessly find your perfect match with our streamlined 'Finding Truck feature.",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                AddSpace(16)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE1D5F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF5C3D8F), modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        TruckInfoRow("Task", task)
                        TruckInfoRow("Departed", departedDate)
                        TruckInfoRow("Current Location", currentLocation)
                        TruckInfoRow("Trip Cost", tripCost)
                    }
                }

                AddSpace(20)
            }
        }

        // Bottom nav overlay
        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 0.dp)) {
            DashboardBottomBar(
                onHomeClick = onHomeClick,
                onOrdersClick = onOrdersClick,
                onHistoryClick = onHistoryClick,
                onChatClick = onChatClick,
                onProfileClick = onProfileClick
            )
        }
    }
}

@Composable
fun TruckInfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("$label  ", fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 12.sp, color = textThemeColor)
    }
}


// ─────────────────────────────────────────────
// AVAILABLE TRUCKS SCREEN
// ─────────────────────────────────────────────

@Composable
fun AvailableTrucksScreen(
    trucks: List<AvailableTruck> = listOf(
        AvailableTruck("Chemical", "Karachi to Multan", "PKR 8,500", TruckBadge.Shared),
        AvailableTruck("Chemical", "Karachi to Multan", "PKR 9,500", TruckBadge.Shared),
        AvailableTruck("Chemical", "Karachi to Multan", "PKR 18,500", TruckBadge.Full)
    ),
    sharedRideEnabled: Boolean = false,
    onSharedRideToggle: (Boolean) -> Unit = {},
    onDetailsClick: (AvailableTruck) -> Unit = {},
    onSendRequest: (AvailableTruck) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val backgroundGradient = remember {
        Brush.verticalGradient(listOf(Color(0xFFE0F2F1), backgroundColor))
    }
    var sharedRide by remember { mutableStateOf(sharedRideEnabled) }

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
            AppHeading("Available Trucks")
            AddSpace(12)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Shared Ride", fontSize = 13.sp, color = textThemeColor, modifier = Modifier.padding(end = 8.dp))
                Switch(
                    checked = sharedRide,
                    onCheckedChange = { sharedRide = it; onSharedRideToggle(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF333333)
                    )
                )
            }

            AddSpace(12)

            trucks.forEach { truck ->
                AvailableTruckCard(
                    truck = truck,
                    onDetailsClick = { onDetailsClick(truck) },
                    onSendRequest = { onSendRequest(truck) }
                )
                AddSpace(12)
            }

            AddSpace(24)
        }
    }
}

@Composable
fun AvailableTruckCard(
    truck: AvailableTruck,
    onDetailsClick: () -> Unit,
    onSendRequest: () -> Unit
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = actionTealColor, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(truck.shipmentTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textThemeColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    TruckBadgeChip(badge = truck.badge)
                }
                AddSpace(4)
                Text("Route", fontSize = 11.sp, color = Color.Gray)
                Text(truck.route, fontSize = 13.sp, color = textThemeColor)
                AddSpace(4)
                Text("Amount", fontSize = 11.sp, color = Color.Gray)
                Text(truck.amount, fontSize = 13.sp, color = textThemeColor)
                AddSpace(12)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmallActionButton(text = "Details", containerColor = Color(0xFF333333), enabled = true, onClick = onDetailsClick)
                    SmallActionButton(text = "Send Request", containerColor = Color(0xFF4CAF50), enabled = true, onClick = onSendRequest)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            // Truck image placeholder
            Surface(
                modifier = Modifier.size(80.dp, 56.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFF9C4)
            ) {
                Box(contentAlignment = Alignment.Center) { Text("🚛", fontSize = 28.sp) }
            }
        }
    }
}

@Composable
fun TruckBadgeChip(badge: TruckBadge) {
    val (label, color) = when (badge) {
        TruckBadge.Shared -> "Shared" to Color(0xFFFFA726)
        TruckBadge.Full -> "Full" to Color(0xFF2196F3)
    }
    Surface(shape = RoundedCornerShape(20.dp), color = color) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}


// ─────────────────────────────────────────────
// DRIVER FOUND SCREEN
// ─────────────────────────────────────────────

@Composable
fun DriverFoundScreen(
    task: String = "Chemical Delivery",
    pickupDate: String = "20 Feb, 05:00 PM",
    currentLocation: String = "123 Main Street, Anytown, IND 845103",
    tripCost: String = "Rs 10000",
    vehicleNumber: String = "LXR-5166",
    ownerName: String = "Fahad Ahmed",
    registeringAuthority: String = "Quetta",
    vehicleModel: String = "Hino 500 Series",
    fuelType: String = "Petrol",
    onViewDetails: () -> Unit = {},
    onContactDriver: () -> Unit = {},
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
            AppHeading("Driver Found")
            AddSpace(20)

            // Driver info card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("A driver has accepted your request", fontSize = 13.sp, color = Color.Gray)
                    AddSpace(12)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE1D5F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF5C3D8F), modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            TruckInfoRow("Task", task)
                            TruckInfoRow("Pickup Date", pickupDate)
                            TruckInfoRow("Current Location", currentLocation)
                            TruckInfoRow("Trip Cost", tripCost)
                        }
                    }
                }
            }

            AddSpace(16)

            // Truck Info dark card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1A1A1A),
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Truck Info", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                    AddSpace(12)
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Vehicle Number", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text("Owner Name", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text("Registering authority", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text("Vehicle Model", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text("Fuel Type", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(vehicleNumber, fontSize = 12.sp, color = Color.White)
                            Text(ownerName, fontSize = 12.sp, color = Color.White)
                            Text(registeringAuthority, fontSize = 12.sp, color = Color.White)
                            Text(vehicleModel, fontSize = 12.sp, color = Color.White)
                            Text(fuelType, fontSize = 12.sp, color = Color.White)
                        }
                    }
                    AddSpace(16)
                    // Truck image placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(Color(0xFF2A2A2A), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🚛", fontSize = 56.sp)
                    }
                }
            }

            AddSpace(24)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppButton(text = "View Details", onClick = onViewDetails, modifier = Modifier.weight(1f), width = 0.dp, backgroundColor = Color(0xFF555555))
                AppButton(text = "Contact Driver", onClick = onContactDriver, modifier = Modifier.weight(1f), width = 0.dp)
            }

            AddSpace(24)
        }
    }
}


// ─────────────────────────────────────────────
// YOUR BOOKINGS SCREEN (Active + Completed)
// ─────────────────────────────────────────────

@Composable
fun YourBookingsScreen(
    activeBookings: List<CustomerBooking> = listOf(
        CustomerBooking("Electronics", "Karachi to Multan", "PKR 8,500", BookingStatus.InTransit),
        CustomerBooking("Chemical", "Karachi to Multan", "PKR 8,500", BookingStatus.Confirmed)
    ),
    completedBookings: List<CustomerBooking> = listOf(
        CustomerBooking("Electronics", "Karachi to Multan", "PKR 8,500", BookingStatus.Completed),
        CustomerBooking("Electronics", "Karachi to Multan", "PKR 8,500", BookingStatus.Reviewed),
        CustomerBooking("Electronics", "Karachi to Multan", "PKR 8,500", BookingStatus.Reviewed)
    ),
    onDetailsClick: (CustomerBooking) -> Unit = {},
    onContactDriverClick: (CustomerBooking) -> Unit = {},
    onTrackClick: (CustomerBooking) -> Unit = {},
    onCancelClick: (CustomerBooking) -> Unit = {},
    onReviewClick: (CustomerBooking) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val backgroundGradient = remember {
        Brush.verticalGradient(listOf(Color(0xFFE0F2F1), backgroundColor))
    }
    var selectedTab by remember { mutableStateOf("Active") }

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
        ) {
            AppHeading("Your Bookings")
            AddSpace(16)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TabButton(text = "Active", isSelected = selectedTab == "Active", onClick = { selectedTab = "Active" })
                TabButton(text = "Completed", isSelected = selectedTab == "Completed", onClick = { selectedTab = "Completed" })
            }

            AddSpace(16)

            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (selectedTab == "Active") {
                    activeBookings.forEach { booking ->
                        CustomerBookingCard(
                            booking = booking,
                            onDetailsClick = { onDetailsClick(booking) },
                            onContactDriverClick = { onContactDriverClick(booking) },
                            onTrackClick = { onTrackClick(booking) },
                            onCancelClick = { onCancelClick(booking) }
                        )
                        AddSpace(12)
                    }
                } else {
                    completedBookings.forEach { booking ->
                        CompletedBookingCard(
                            booking = booking,
                            onDetailsClick = { onDetailsClick(booking) },
                            onReviewClick = { onReviewClick(booking) }
                        )
                        AddSpace(12)
                    }
                }
                AddSpace(24)
            }
        }
    }
}

@Composable
fun CompletedBookingCard(
    booking: CustomerBooking,
    onDetailsClick: () -> Unit,
    onReviewClick: () -> Unit
) {
    val isReviewed = booking.status == BookingStatus.Reviewed

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, tint = actionTealColor, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(booking.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textThemeColor)
                }
                if (isReviewed) {
                    Text("Reviewed", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                }
            }
            AddSpace(8)
            Text("Route", fontSize = 11.sp, color = Color.Gray)
            Text(booking.route, fontSize = 13.sp, color = textThemeColor)
            AddSpace(4)
            Text("Amount", fontSize = 11.sp, color = Color.Gray)
            Text(booking.amount, fontSize = 13.sp, color = textThemeColor)
            AddSpace(12)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallActionButton(text = "Details", containerColor = Color(0xFF333333), enabled = true, onClick = onDetailsClick)
                SmallActionButton(
                    text = "Review",
                    containerColor = if (isReviewed) Color.LightGray else Color(0xFF4CAF50),
                    enabled = !isReviewed,
                    onClick = onReviewClick
                )
            }
        }
    }
}


// ─────────────────────────────────────────────
// CUSTOMER PROFILE SCREEN
// ─────────────────────────────────────────────

@Composable
fun CustomerProfileScreen(
    profileImageUrl: String? = null,
    rating: Float = 4.0f,
    isSwitchedToDriver: Boolean = false,
    onBookingDetailsClick: () -> Unit = {},
    onYourInformationClick: () -> Unit = {},
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
    var switchState by remember { mutableStateOf(isSwitchedToDriver) }

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

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE1D5F0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color(0xFF5C3D8F))
            }

            AddSpace(20)

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                val fullStars = rating.toInt()
                repeat(5) { i ->
                    Text("★", fontSize = 28.sp, color = if (i < fullStars) Color(0xFFFFB300) else Color(0xFFE0E0E0))
                }
            }

            AddSpace(32)

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column {
                    ProfileMenuItem(text = "Booking Details", onClick = onBookingDetailsClick)
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
                    ProfileMenuItem(text = "Your information", onClick = onYourInformationClick)
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            AddSpace(32)

            HorizontalDivider(color = Color.LightGray)
            AddSpace(16)

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
                        modifier = Modifier.size(32.dp).background(Color(0xFFFF1744), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("—", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Logout", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textThemeColor)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Switch to\nDriver", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(end = 8.dp))
                    Switch(
                        checked = switchState,
                        onCheckedChange = { switchState = it; onSwitchRoleToggle(it) },
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
// DRIVER PROFILE DETAIL SCREEN
// ─────────────────────────────────────────────

@Composable
fun DriverProfileDetailScreen(
    otherShipments: List<Pair<String, String>> = listOf(
        "Sofa Set" to "Karachi to Peshawar\n~600 KGs\nLoaded",
        "Glassware" to "Multan to Peshawar\n~5 KGs\nBooked"
    ),
    driver: DriverProfile = DriverProfile(
        name = "Muhammad Hassan",
        email = "hassan@example.com",
        location = "Karachi, Pakistan",
        memberSince = "Member since March 2020",
        isTopRated = true,
        rating = 3.0f,
        reviewerName = "Muhammad Asif",
        reviewDate = "16-11-2925",
        reviewText = "Good Experience!",
        vehicleModel = "Hino 500 Series",
        vehicleType = "20ft Container",
        licensePlate = "LEB-9472",
        color = "Metallic Silver",
        year = "2020",
        capacity = "15 cm cube",
        registrationNo = "LE5-2156"
    ),
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
            AppHeading("Other Shipments on Truck")
            AddSpace(16)

            // Other shipments horizontal row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                otherShipments.forEach { (title, info) ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textThemeColor)
                            AddSpace(4)
                            info.split("\n").forEach { line ->
                                Text(line, fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            AddSpace(24)
            AppSectionHeader("Driver Profile")
            AddSpace(12)

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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFBDBDBD)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(driver.name.first().toString(), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(driver.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textThemeColor)
                        }
                        if (driver.isTopRated) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFFFF9C4),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300))
                            ) {
                                Text(
                                    "⭐ Top Rated",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    color = Color(0xFFFF8F00)
                                )
                            }
                        }
                    }
                    AddSpace(12)
                    Text("✉  ${driver.email}", fontSize = 12.sp, color = Color.Gray)
                    AddSpace(4)
                    Text("📍 ${driver.location}", fontSize = 12.sp, color = Color.Gray)
                    AddSpace(4)
                    Text("🗓  ${driver.memberSince}", fontSize = 12.sp, color = Color.Gray)
                }
            }

            AddSpace(16)
            AppSectionHeader("Recent Reviews")
            AddSpace(8)

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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(driver.reviewerName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = textThemeColor)
                        Text(driver.reviewDate, fontSize = 12.sp, color = Color.Gray)
                    }
                    AddSpace(6)
                    Row {
                        repeat(5) { i ->
                            Text("★", fontSize = 18.sp, color = if (i < driver.rating.toInt()) Color(0xFFFFB300) else Color(0xFFE0E0E0))
                        }
                    }
                    AddSpace(6)
                    Text(driver.reviewText, fontSize = 13.sp, color = Color.Gray)
                }
            }

            AddSpace(16)
            AppSectionHeader("Vehicle Details")
            AddSpace(8)

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Person, null, tint = actionTealColor, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(driver.vehicleType, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textThemeColor)
                            Text("${driver.registrationNo} • Year: ${driver.year}", fontSize = 12.sp, color = Color.Gray)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE0F2F1)
                        ) {
                            Text(
                                "Capacity: ${driver.capacity}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontSize = 11.sp,
                                color = actionTealColor
                            )
                        }
                    }
                    AddSpace(12)
                    Text("Truck Model: ${driver.vehicleModel}", fontSize = 13.sp, color = textThemeColor)
                    Text("License Plate: ${driver.licensePlate}", fontSize = 13.sp, color = textThemeColor)
                    Text("Color: ${driver.color}", fontSize = 13.sp, color = textThemeColor)
                }
            }

            AddSpace(24)
        }
    }
}


// ─────────────────────────────────────────────
// CANCEL BOOKING SCREEN
// ─────────────────────────────────────────────

@Composable
fun CancelBookingScreen(
    onBack: () -> Unit = {},
    onGetInContact: () -> Unit = {},
    onConfirmCancel: (String) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val backgroundGradient = remember {
        Brush.verticalGradient(listOf(Color(0xFFE0F2F1), backgroundColor))
    }
    var reason by remember { mutableStateOf("") }

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
                .imePadding()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onBack() }
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textThemeColor)
                Spacer(modifier = Modifier.width(8.dp))
                AppHeading("Cancel Booking")
            }

            AddSpace(28)

            // Reason textarea
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .shadow(4.dp, RoundedCornerShape(12.dp)),
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

            AddSpace(28)

            // Get in Contact button
            Button(
                onClick = onGetInContact,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Get in Contact with Driver", color = Color.White, fontWeight = FontWeight.Bold)
            }

            AddSpace(12)

            // Confirm Cancel button
            AppButton(
                text = "Confirm Cancel",
                onClick = { onConfirmCancel(reason) },
                modifier = Modifier.fillMaxWidth(),
                width = 0.dp,
                height = 52.dp
            )
        }
    }
}


// ─────────────────────────────────────────────
// CUSTOMER SUPPORT CHAT SCREEN
// ─────────────────────────────────────────────

@Composable
fun CustomerSupportChatScreen(
    onBack: () -> Unit = {},
    onOptionSelected: (String) -> Unit = {}
) {
    val backgroundGradient = remember {
        Brush.verticalGradient(listOf(Color(0xFFE0F2F1), backgroundColor))
    }

    val supportOptions = listOf(
        "1. Damaged/Parcel Missing",
        "2. Report a Driver",
        "3. Account Issue",
        "4. Other. Talk to an Agent"
    )

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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textThemeColor)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF1B5E20)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("CS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Customer Support", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textThemeColor)
                        Text("Active 11m ago", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }

        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Timestamp
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Oct 07, 2025, 9:41 AM", fontSize = 11.sp, color = Color.Gray)
            }
            AddSpace(4)

            // User "Hi" bubble
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFF1B5E20)).align(Alignment.Bottom),
                    contentAlignment = Alignment.Center
                ) {
                    Text("CS", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp), color = Color.White, shadowElevation = 1.dp) {
                    Text("Hi", modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), fontSize = 14.sp, color = textThemeColor)
                }
            }

            // Bot welcome message (sent by support = dark bubble on right)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Surface(shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp), color = Color(0xFF333333), shadowElevation = 1.dp) {
                    Text(
                        "Welcome. Please choose an option from the following.",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }

            // Option buttons
            supportOptions.forEach { option ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Surface(
                        onClick = { onOptionSelected(option) },
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF333333),
                        shadowElevation = 1.dp
                    ) {
                        Text(
                            text = option,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Input bar
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 12.dp, vertical = 8.dp),
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
            IconButton(onClick = {}) { Icon(Icons.Default.Person, contentDescription = "Voice", tint = Color.Gray) }
            IconButton(onClick = {}) { Icon(Icons.Default.Person, contentDescription = "Emoji", tint = Color.Gray) }
            IconButton(onClick = {}) { Icon(Icons.Default.Person, contentDescription = "Attach", tint = Color.Gray) }
        }
    }
}


// ─────────────────────────────────────────────
// CUSTOMER ACTIVITY SCREEN (Chats + Notifications)
// ─────────────────────────────────────────────

@Composable
fun CustomerActivityScreen(
    chats: List<ChatContact> = listOf(
        ChatContact("Kamran", "Hello.", "1h", true),
        ChatContact("Waleed", "Received", "1h", true),
        ChatContact("Customer Support", "", "1h", false, isSupport = true)
    ),
    notifications: List<NotificationItem> = listOf(
        NotificationItem("Your parcel has been delivers", "3h", true),
        NotificationItem("Ahmed send you a message", "3h", true),
        NotificationItem("Your parcel has been delivers", "21h", false)
    ),
    onChatClick: (ChatContact) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChatNavClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    // Reuses ActivityScreen composable — same structure, only different default data
    ActivityScreen(
        chats = chats,
        notifications = notifications,
        onChatClick = onChatClick,
        onHomeClick = onHomeClick,
        onOrdersClick = onOrdersClick,
        onHistoryClick = onHistoryClick,
        onChatNavClick = onChatNavClick,
        onProfileClick = onProfileClick
    )
}


// ─────────────────────────────────────────────
// BOOKING SHIPMENTS DETAIL (Your Bookings > Shipments)
// ─────────────────────────────────────────────

@Composable
fun BookingShipmentsDetailScreen(
    shipments: List<ShipmentDetail> = listOf(
        ShipmentDetail(
            title = "Electronics Household",
            totalWeight = "~580 KGs",
            recipientName = "",
            totalFare = "",
            packages = listOf(
                ShipmentPackage("Refrigerator", 1, "", "", "15cm x 15cm x 4cm", "~280 KGs", "Keep Upright"),
                ShipmentPackage("Sofa", 3, "", "", "15cm x 15cm x 4cm", "~300 KGs")
            )
        )
    ),
    pickupAddress: String = "Shabir Chowk, Karachi",
    deliveryAddress: String = "Main Stand, Peshawar",
    totalWeight: String = "~580 KGs",
    pickupDate: String = "02, September, 2025",
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
            AppHeading("Your Bookings")
            AddSpace(16)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onBack() }
                    .padding(bottom = 16.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textThemeColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Shipments", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = textThemeColor)
            }

            shipments.forEach { shipment ->
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
                                Text(shipment.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textThemeColor)
                                AddSpace(4)
                                Text("Pickup Address: $pickupAddress", fontSize = 12.sp, color = textThemeColor)
                                Text("Delivery Address: $deliveryAddress", fontSize = 12.sp, color = textThemeColor)
                                Text("Total Weight: $totalWeight", fontSize = 12.sp, color = textThemeColor)
                                Text("Pickup Date: $pickupDate", fontSize = 12.sp, color = textThemeColor)
                            }
                            Surface(modifier = Modifier.size(60.dp, 40.dp), shape = RoundedCornerShape(8.dp), color = Color(0xFFFFF9C4)) {
                                Box(contentAlignment = Alignment.Center) { Text("🚛", fontSize = 24.sp) }
                            }
                        }

                        AddSpace(16)

                        shipment.packages.forEachIndexed { i, pkg ->
                            Text("Package ${i + 1}: ${pkg.name}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textThemeColor)
                            Text("Quantity: ${pkg.quantity}", fontSize = 12.sp, color = textThemeColor)
                            if (pkg.dimensions != null) {
                                Text("Dimensions", fontSize = 12.sp, color = textThemeColor)
                                Text("  Length: ${pkg.dimensions.split("x")[0].trim()}", fontSize = 12.sp, color = textThemeColor)
                                Text("  Width: ${pkg.dimensions.split("x").getOrNull(1)?.trim() ?: ""}", fontSize = 12.sp, color = textThemeColor)
                                Text("  Height: ${pkg.dimensions.split("x").getOrNull(2)?.trim() ?: ""}", fontSize = 12.sp, color = textThemeColor)
                            }
                            if (pkg.weight != null) Text("Weight: ${pkg.weight}", fontSize = 12.sp, color = textThemeColor)
                            if (pkg.note != null) Text(pkg.note, fontSize = 12.sp, color = textThemeColor, fontWeight = FontWeight.Medium)
                            if (i < shipment.packages.lastIndex) AddSpace(10)
                        }
                    }
                }
                AddSpace(12)
            }

            AddSpace(24)
        }
    }
}
@Preview(showBackground = true)
@Composable
fun PreviewCustomerDashboardScreen() {
    CustomerDashboardScreen()
}