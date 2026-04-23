package com.example.cargoconnectapp.ui.navigation

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.cargoconnectapp.ui.models.destinations.*
import com.example.cargoconnectapp.ui.themes.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter


// ─────────────────────────────────────────────
// LAYOUT HELPERS
// ─────────────────────────────────────────────

@Composable
fun AddSpace(size: Int) {
    Spacer(modifier = Modifier.height(size.dp))
}

@Composable
fun AddSpacer(size: Dp) {
    Spacer(modifier = Modifier.height(size))
}


// ─────────────────────────────────────────────
// TYPOGRAPHY
// ─────────────────────────────────────────────

@Composable
fun AppHeading(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground
) {
    Text(
        text = text,
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = modifier
    )
}

@Composable
fun AppSubHeading(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground
) {
    Text(
        text = text,
        fontSize = 18.sp,
        color = color,
        modifier = modifier.padding(top = 4.dp)
    )
}

@Composable
fun AppSectionHeader(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.DarkGray
) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textThemeColor)
        Text(text = value, fontSize = 14.sp, color = Color.Gray)
    }
}


// ─────────────────────────────────────────────
// SHARED OVERHAUL COMPONENTS
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
// INPUT FIELDS
// ─────────────────────────────────────────────

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp)
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(4.dp, shape),
        color = backgroundColor,
        shape = shape
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(text = placeholder, color = Color.Gray, fontSize = 13.sp) },
            enabled = enabled,
            readOnly = readOnly,
            isError = isError,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            singleLine = singleLine,
            maxLines = maxLines,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun AppPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Password",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp)
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(4.dp, shape),
        color = backgroundColor,
        shape = shape
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(text = placeholder, color = Color.Gray, fontSize = 14.sp) },
            enabled = enabled,
            readOnly = readOnly,
            isError = isError,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = Color.Gray
                    )
                }
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun FileUploadRow(
    label: String,
    selectedFileName: String,
    onFileSelected: (Uri?) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> onFileSelected(uri) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(12.dp),
        color = inputColor,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Text(
                    text = selectedFileName.ifEmpty { "No file selected" },
                    fontSize = 14.sp,
                    color = if (selectedFileName.isEmpty()) Color.Gray else textThemeColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Button(
                onClick = { launcher.launch("image/*") },
                colors = ButtonDefaults.buttonColors(containerColor = actionTealColor),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(40.dp).width(100.dp)
            ) {
                Text("Browse", color = Color.White, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun LocationInputField(
    label: String,
    value: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) actionTealColor.copy(alpha = 0.1f) else inputColor,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) actionTealColor else Color.Gray.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_mylocation),
                contentDescription = null,
                tint = if (isSelected) actionTealColor else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Text(
                    text = value,
                    fontSize = 14.sp,
                    color = if (value.contains("Select") || value.contains("Tap")) Color.Gray else textThemeColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (isSelected) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_edit),
                    contentDescription = "Selected",
                    tint = actionTealColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}


// ─────────────────────────────────────────────
// BUTTONS
// ─────────────────────────────────────────────

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    width: Dp = 180.dp,
    height: Dp = 54.dp,
    textSize: Int = 16,
    backgroundColor: Color = actionTealColor,
    contentColor: Color = Color.White,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp)
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.width(width).height(height),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
            disabledContainerColor = textThemeColor,
            disabledContentColor = inputColor
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 2.dp,
            disabledElevation = 0.dp
        ),
        shape = shape
    ) {
        Text(text = text, fontSize = textSize.sp)
    }
}

@Composable
fun SmallActionButton(
    text: String,
    containerColor: Color,
    enabled: Boolean,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.width(85.dp).height(32.dp),
        shape = RoundedCornerShape(16.dp),
        color = containerColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color(0xFF333333) else Color.White
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            color = if (isSelected) Color.White else Color.Gray,
            fontWeight = FontWeight.Medium
        )
    }
}


// ─────────────────────────────────────────────
// HEADERS & NAVIGATION
// ─────────────────────────────────────────────

@Composable
fun AppHeader(
    title: String,
    modifier: Modifier = Modifier,
    showBackArrow: Boolean = false,
    showProfileIcon: Boolean = false,
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    profileImageUrl: String? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    titleColor: Color = MaterialTheme.colorScheme.onBackground
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (showBackArrow) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = titleColor
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                if (showProfileIcon) {
                    ProfileIcon(imageUrl = profileImageUrl, onClick = onProfileClick)
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }
        }
        AddSpacer(5.dp)
        HorizontalDivider(thickness = 1.dp, color = titleColor)
        AddSpacer(20.dp)
    }
}

@Composable
fun DashboardBottomBar(
    currentTab: DriverDestination,
    onHomeClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onChatClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            selected = currentTab is DriverDashboard,
            onClick = onHomeClick,
            colors = NavigationBarItemDefaults.colors(selectedIconColor = actionTealColor, indicatorColor = actionTealColor.copy(alpha = 0.1f))
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Inventory, contentDescription = "Orders") },
            selected = currentTab is DriverShipmentRequest,
            onClick = onOrdersClick,
            colors = NavigationBarItemDefaults.colors(selectedIconColor = actionTealColor, indicatorColor = actionTealColor.copy(alpha = 0.1f))
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Assignment, contentDescription = "History") },
            selected = currentTab is DriverDeliveries,
            onClick = onHistoryClick,
            colors = NavigationBarItemDefaults.colors(selectedIconColor = actionTealColor, indicatorColor = actionTealColor.copy(alpha = 0.1f))
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.ChatBubble, contentDescription = "Chat") },
            selected = currentTab is DriverChat,
            onClick = onChatClick,
            colors = NavigationBarItemDefaults.colors(selectedIconColor = actionTealColor, indicatorColor = actionTealColor.copy(alpha = 0.1f))
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
            selected = currentTab is DriverProfileScreen,
            onClick = onProfileClick,
            colors = NavigationBarItemDefaults.colors(selectedIconColor = actionTealColor, indicatorColor = actionTealColor.copy(alpha = 0.1f))
        )
    }
}
@Composable
fun CustomerBottomBar(
    currentTab: CustomerDestination,
    onHomeClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onChatClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            selected = currentTab is CustomerDashboard,
            onClick = onHomeClick,
            colors = NavigationBarItemDefaults.colors(selectedIconColor = actionTealColor, indicatorColor = actionTealColor.copy(alpha = 0.1f))
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.LocalShipping, contentDescription = "New Shipment") },
            selected = currentTab is CustomerNewShipment,
            onClick = onOrdersClick,
            colors = NavigationBarItemDefaults.colors(selectedIconColor = actionTealColor, indicatorColor = actionTealColor.copy(alpha = 0.1f))
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Assignment, contentDescription = "Bookings") },
            selected = currentTab is CustomerBookings,
            onClick = onHistoryClick,
            colors = NavigationBarItemDefaults.colors(selectedIconColor = actionTealColor, indicatorColor = actionTealColor.copy(alpha = 0.1f))
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.ChatBubble, contentDescription = "Chat") },
            selected = currentTab is CustomerChat,
            onClick = onChatClick,
            colors = NavigationBarItemDefaults.colors(selectedIconColor = actionTealColor, indicatorColor = actionTealColor.copy(alpha = 0.1f))
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
            selected = currentTab is CustomerProfile,
            onClick = onProfileClick,
            colors = NavigationBarItemDefaults.colors(selectedIconColor = actionTealColor, indicatorColor = actionTealColor.copy(alpha = 0.1f))
        )
    }
}


// ─────────────────────────────────────────────
// PROFILE
// ─────────────────────────────────────────────

@Composable
fun ProfileIcon(
    imageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrEmpty()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Profile Photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Default Profile Icon",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}


// ─────────────────────────────────────────────
// PROGRESS & REGISTRATION
// ─────────────────────────────────────────────

@Composable
fun RegistrationProgressBar(currentStep: Int, totalSteps: Int = 5) {
    val progressTarget = currentStep.toFloat() / totalSteps.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing),
        label = "StepProgress"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Step $currentStep of $totalSteps",
                style = MaterialTheme.typography.labelMedium,
                color = actionTealColor
            )
            Text(
                text = "${(progressTarget * 100).toInt()}% Complete",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .background(actionTealColor, RoundedCornerShape(4.dp))
            )
        }
    }
}


// ─────────────────────────────────────────────
// CHIPS
// ─────────────────────────────────────────────

@Composable
fun CityChip(city: String, onRemove: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = actionTealColor.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, actionTealColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = city,
                fontSize = 13.sp,
                color = actionTealColor,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(onClick = onRemove, modifier = Modifier.size(20.dp)) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                    contentDescription = "Remove",
                    tint = actionTealColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}


// ─────────────────────────────────────────────
// ROUTE & COUNTER
// ─────────────────────────────────────────────

@Composable
fun RouteItem(
    city: String,
    address: String,
    distance: String,
    isReached: Boolean,
    onDelayClick: () -> Unit = {},
    onReachedClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = city, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "$address  •  ~$distance",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallActionButton(
                text = "Add Delay",
                containerColor = if (isReached) Color.LightGray else Color(0xFFFF1744),
                enabled = !isReached,
                onClick = onDelayClick
            )
            SmallActionButton(
                text = "Reached",
                containerColor = if (isReached) Color.LightGray else actionTealColor,
                enabled = !isReached,
                onClick = onReachedClick
            )
        }
    }
}

@Composable
fun CounterRow(
    label: String,
    value: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, modifier = Modifier.weight(1f), fontSize = 14.sp, color = Color.Gray)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onDecrease, modifier = Modifier.size(24.dp)) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_delete),
                    contentDescription = "Decrease",
                    tint = textThemeColor
                )
            }
            Surface(
                color = inputColor,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.width(80.dp).height(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = value, fontSize = 13.sp, color = textThemeColor)
                }
            }
            IconButton(onClick = onIncrease, modifier = Modifier.size(24.dp)) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_input_add),
                    contentDescription = "Increase",
                    tint = textThemeColor
                )
            }
        }
    }
}


// ─────────────────────────────────────────────
// CALENDAR
// ─────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarView(
    currentMonth: YearMonth,
    selectedDate: LocalDate?,
    minDate: LocalDate? = null,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (YearMonth) -> Unit
) {
    val today = LocalDate.now()
    val baseMinDate = minDate ?: today

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Month navigation header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onMonthChanged(currentMonth.minusMonths(1)) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month", tint = textThemeColor)
                }
                Text(
                    text = currentMonth.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textThemeColor
                )
                IconButton(onClick = { onMonthChanged(currentMonth.plusMonths(1)) }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Month", tint = textThemeColor)
                }
            }

            AddSpace(12)

            // Day of week labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN").forEach { day ->
                    Text(
                        text = day,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            AddSpace(8)

            // Calendar grid
            val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value // Monday = 1
            val daysInMonth = currentMonth.lengthOfMonth()
            val totalCells = firstDayOfWeek - 1 + daysInMonth
            val weeksNeeded = (totalCells + 6) / 7

            Column {
                for (week in 0 until weeksNeeded) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (dayOfWeek in 1..7) {
                            val dayNumber = week * 7 + dayOfWeek - (firstDayOfWeek - 1)
                            if (dayNumber in 1..daysInMonth) {
                                val date = currentMonth.atDay(dayNumber)
                                val isSelected = date == selectedDate
                                val isPast = date.isBefore(baseMinDate)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) textThemeColor else Color.Transparent)
                                        .clickable(enabled = !isPast) { onDateSelected(date) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayNumber.toString(),
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isSelected -> Color.White
                                            isPast -> Color.LightGray
                                            else -> textThemeColor
                                        }
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

//POST DELAY PERIOD AAAAAA

@Composable
fun ProfileMenuItem(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textThemeColor)
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = textThemeColor
        )
    }
}


@Composable
fun ChatMessageItem(content: String, isSentByMe: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isSentByMe) Arrangement.End else Arrangement.Start
    ) {
        if (!isSentByMe) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFBDBDBD))
                    .align(Alignment.Bottom),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isSentByMe) 16.dp else 4.dp,
                bottomEnd = if (isSentByMe) 4.dp else 16.dp
            ),
            color = if (isSentByMe) actionTealColor else Color.White,
            shadowElevation = 1.dp
        ) {
            Text(
                text = content,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                fontSize = 14.sp,
                color = if (isSentByMe) Color.White else textThemeColor
            )
        }
    }
}

@Composable
fun EnhancedChatListItem(name: String, lastMessage: String, time: String, hasUnread: Boolean, onClick: () -> Unit) {
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
                if (hasUnread) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(statusRedColor).border(2.dp, Color.White, CircleShape).align(Alignment.TopEnd))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(time, fontSize = 12.sp, color = Color.Gray)
                }
                Text(lastMessage, fontSize = 14.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun EnhancedNotificationCard(message: String, time: String, isUnread: Boolean) {
    GlassCard(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = actionTealColor.copy(alpha = 0.1f)) {
                Icon(Icons.Default.NotificationsActive, null, modifier = Modifier.padding(8.dp), tint = actionTealColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(message, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(time, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
            }
            if (isUnread) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(actionTealColor))
            }
        }
    }
}

@Preview
@Composable
fun show () {
    RouteItem("Lahore", "Truck Adda", "1200KMs", false, {})
}
