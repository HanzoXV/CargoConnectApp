package com.example.cargoconnectapp.ui.navigation.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import com.example.cargoconnectapp.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cargoconnectapp.ui.models.helpermodels.UserRoles
import com.example.cargoconnectapp.ui.models.map.OSMContainer
import com.example.cargoconnectapp.ui.models.map.getAddressFromGeoPoint
import com.example.cargoconnectapp.ui.models.viewmodels.AuthViewModel
import com.example.cargoconnectapp.ui.navigation.*
import com.example.cargoconnectapp.ui.themes.*
import org.osmdroid.util.GeoPoint
import java.time.LocalDate
import java.time.YearMonth

@Preview
@Composable
fun SplashScreen(onNavigateToLogin: () -> Unit) {
    val backgroundGradient = remember {
        Brush.verticalGradient(
            listOf(Color(0xFFE0F2F1), backgroundColor)
        )
    }
    Surface(color = backgroundColor,
        modifier = Modifier.background(backgroundGradient)) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,

        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Image(
                        painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                        contentDescription = "Cargo Connect Logo",
                        modifier = Modifier.size(180.dp)
                    )

                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = actionTealColor,
                    strokeWidth = 5.dp
                )
            }
        }
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2500)
            onNavigateToLogin()
        }
    }
}


@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {}
) {
    val backgroundGradient = remember {
        Brush.verticalGradient(
            listOf(Color(0xFFE0F2F1), backgroundColor)
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(horizontal = 28.dp, vertical = 140.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AddSpace(40)
            AppHeading("Cargo Connect")
            AppSubHeading("Sign in to continue")
            AddSpace(56)

            AppTextField(
                value = viewModel.userEmail,
                onValueChange = {viewModel.userEmail = it},
                placeholder = "Email or Phone Number"
            )
            AddSpace(16)
            AppPasswordField(
                value = viewModel.userPassword,
                onValueChange = {viewModel.userPassword = it},
                placeholder = "Password"
            )
            AddSpace(24)

            if (viewModel.errorMessage != null) {
                Text(
                    text = viewModel.errorMessage!!,
                    color = Color.Red,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Row {
                Text(
                    text = "Don't have an account? ",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = textThemeColor,
                )
                Text(
                    text = "Register Now",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = actionTealColor,
                    modifier = Modifier.clickable { onRegisterClick() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onLoginClick,
                enabled = !viewModel.isLoading,
                modifier = Modifier
                    .width(180.dp)
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = actionTealColor,
                    contentColor = inputColor,
                    disabledContainerColor = actionTealColor,
                    disabledContentColor = inputColor
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 2.dp,
                    disabledElevation = 0.dp
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                        color = inputColor,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Login",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

//---------------------------- R E G I S T E R - S C R E E N S -----------------------//
@Composable
fun RegisterStep1(viewModel: AuthViewModel,
    onNavigate: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val backgroundGradient = remember {
        Brush.verticalGradient(
            listOf(Color(0xFFE0F2F1), backgroundColor)
        )
    }
    BackHandler(enabled = true) {
        onBackToLogin()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .padding(horizontal = 28.dp)
            .padding(top = 48.dp, bottom = 24.dp)
            .verticalScroll(rememberScrollState())
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        AppHeading("Cargo Connect")
        AppSubHeading("Choose your role to get started")

        Spacer(modifier = Modifier.weight(1f))

        Surface(
            onClick = {
                viewModel.userRole = UserRoles.Driver
                viewModel.userSteps = 5
                viewModel.currentStep = 1
                onNavigate()
            },
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocalShipping, null, tint = actionTealColor, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Continue as a Driver", fontWeight = FontWeight.Bold)
                    Text("Earn money by delivering cargo", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            onClick = {
                viewModel.userRole = UserRoles.Customer
                viewModel.userSteps = 2
                viewModel.currentStep = 1
                onNavigate()
            },
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Person, null, tint = actionTealColor, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Continue as a Customer", fontWeight = FontWeight.Bold)
                    Text("Ship your goods anywhere safely", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1.2f))
    }
}
@Composable
fun RegisterStep2(viewModel: AuthViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val backgroundGradient = remember {
        Brush.verticalGradient(
            listOf(Color(0xFFE0F2F1), backgroundColor)
        )
    }
    BackHandler(enabled = true) {
        onBack()
        viewModel.currentStep--
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .padding(horizontal = 28.dp)
            .padding(top = 48.dp, bottom = 24.dp)
            .verticalScroll(rememberScrollState())
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RegistrationProgressBar(viewModel.currentStep, totalSteps = viewModel.userSteps)

        Spacer(modifier = Modifier.height(40.dp))

        AppHeading("Cargo Connect")
        AppSubHeading("Personal Details")

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppTextField(value = viewModel.userFirstName, onValueChange = {viewModel.userFirstName = it}, placeholder = "First Name")
            AppTextField(value = viewModel.userLastName, onValueChange = {viewModel.userLastName = it}, placeholder = "Last Name")
            AppTextField(value = viewModel.userCNIC, onValueChange = {viewModel.userCNIC = it}, placeholder = "CNIC Number")
            AppTextField(value = viewModel.userContact, onValueChange = {viewModel.userContact = it}, placeholder = "Contact Number")
            AppTextField(value = viewModel.userCity, onValueChange = {viewModel.userCity = it}, placeholder = "City")
            AppTextField(value = viewModel.userStreetAddress, onValueChange = {viewModel.userStreetAddress = it}, placeholder = "Street Address")
            if (viewModel.userRole == UserRoles.Driver) {
                AppTextField(
                    value = viewModel.licenseNo,
                    onValueChange = { viewModel.licenseNo = it },
                    placeholder = "License No"
                )
            }
        }
        var errorText = ""
        if(viewModel.errorMessage != null) {errorText = viewModel.errorMessage!!}
        Text(
            text = errorText,
            color = statusRedColor,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.Start).padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.width(200.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {if (viewModel.userFirstName.isBlank() ||
                    viewModel.userLastName.isBlank() ||
                    viewModel.userCNIC.isBlank() ||
                    viewModel.userContact.isBlank() ||
                    viewModel.userCity.isBlank() ||
                    viewModel.userStreetAddress.isBlank()
                ) {
                    viewModel.errorMessage = "All fields must be filled"
                } else {
                    viewModel.errorMessage = ""
                    viewModel.currentStep++
                    onNext()
                }},
                colors = ButtonDefaults.buttonColors(containerColor = actionTealColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Text("Next", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
@Composable
fun RegisterStep3(
    viewModel: AuthViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val backgroundGradient = remember {
        Brush.verticalGradient(
            listOf(Color(0xFFE0F2F1), backgroundColor)
        )
    }
    BackHandler(enabled = true) {
        onBack()
        viewModel.currentStep--
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .padding(horizontal = 28.dp)
            .padding(top = 48.dp, bottom = 24.dp)
            .verticalScroll(rememberScrollState())
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RegistrationProgressBar(viewModel.currentStep, totalSteps = viewModel.userSteps)

        Spacer(modifier = Modifier.height(40.dp))

        AppHeading("Cargo Connect")
        AppSubHeading("Vehicle Details")

        Spacer(modifier = Modifier.height(32.dp))

        AppSectionHeader("Vehicle Information")
        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppTextField(
                value = viewModel.vehicleModel,
                onValueChange = { viewModel.vehicleModel = it },
                placeholder = "Model (e.g., Hino 300)"
            )
            AppTextField(
                value = viewModel.vehicleRegNo,
                onValueChange = { viewModel.vehicleRegNo = it },
                placeholder = "Registration Number"
            )
            AppTextField(
                value = viewModel.vehicleWeightCapacity,
                onValueChange = { viewModel.vehicleWeightCapacity = it },
                placeholder = "Weight Capacity (KGs)"
            )
            AppTextField(
                value = viewModel.vehicleType,
                onValueChange = { viewModel.vehicleType = it },
                placeholder = "Type (e.g., Pickup, Van, Truck)"
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Vehicle Dimensions Section
        AppSectionHeader("Vehicle Dimensions (in meters)")
        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppTextField(
                value = viewModel.vehicleLength,
                onValueChange = { viewModel.vehicleLength = it },
                placeholder = "Length (e.g., 4.5m)"
            )
            AppTextField(
                value = viewModel.vehicleWidth,
                onValueChange = { viewModel.vehicleWidth = it },
                placeholder = "Width (e.g., 1.8m)"
            )
            AppTextField(
                value = viewModel.vehicleHeight,
                onValueChange = { viewModel.vehicleHeight = it },
                placeholder = "Height (e.g., 2.2m)"
            )
        }

        // Error Message
        var errorText = ""
        if (viewModel.errorMessage != null) {
            errorText = viewModel.errorMessage!!
        }
        Text(
            text = errorText,
            color = statusRedColor,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(24.dp))

        // Navigation Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    if (viewModel.vehicleModel.isBlank() ||
                        viewModel.vehicleRegNo.isBlank() ||
                        viewModel.vehicleWeightCapacity.isBlank() ||
                        viewModel.vehicleType.isBlank() ||
                        viewModel.vehicleLength.isBlank() ||
                        viewModel.vehicleWidth.isBlank() ||
                        viewModel.vehicleHeight.isBlank()
                    ) {
                        viewModel.errorMessage = "All fields must be filled"
                    } else {
                        viewModel.errorMessage = null
                        viewModel.currentStep++
                        onNext()
                    }
                },
                modifier = Modifier
                    .width(120.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = actionTealColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Next", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
@Composable
fun RegisterStep4(
    viewModel: AuthViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit,
    context: Context
) {
    val backgroundGradient = remember {
        Brush.verticalGradient(
            listOf(Color(0xFFE0F2F1), backgroundColor)
        )
    }
    BackHandler(enabled = true) {
        onBack()
        viewModel.currentStep--
    }

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(horizontal = 28.dp)
                .padding(top = 48.dp, bottom = 24.dp)
                .verticalScroll(scrollState)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RegistrationProgressBar(
                currentStep = viewModel.currentStep,
                totalSteps = viewModel.userSteps
            )

            Spacer(modifier = Modifier.height(40.dp))

            AppHeading("Cargo Connect")
            AppSubHeading("Account Information")

            Spacer(modifier = Modifier.height(32.dp))

            // Account Information Section
            AppSectionHeader("Login Credentials")
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppTextField(
                    value = viewModel.userEmail,
                    onValueChange = { viewModel.userEmail = it },
                    placeholder = "Email Address"
                )
                AppPasswordField(
                    value = viewModel.userPassword,
                    onValueChange = { viewModel.userPassword = it },
                    placeholder = "Password"
                )
                AppPasswordField(
                    value = viewModel.userConfirmPassword,
                    onValueChange = { viewModel.userConfirmPassword = it },
                    placeholder = "Confirm Password"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Profile Photo Section
            AppSectionHeader("Profile Photo")
            Spacer(modifier = Modifier.height(12.dp))

            FileUploadRow(
                label = "Upload Photo",
                selectedFileName = getFileName(context, viewModel.userPhotoLink),
                onFileSelected = { uri ->
                    viewModel.userPhotoLink = uri
                }
            )

            var errorText = ""
            if (viewModel.errorMessage != null) {
                errorText = viewModel.errorMessage!!
            }
            Text(
                text = errorText,
                color = statusRedColor,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        when {
                            viewModel.userEmail.isBlank() ||
                                    viewModel.userPassword.isBlank() ||
                                    viewModel.userConfirmPassword.isBlank() -> {
                                viewModel.errorMessage = "All fields must be filled"
                            }
                            viewModel.userPassword != viewModel.userConfirmPassword -> {
                                viewModel.errorMessage = "Passwords do not match"
                            }
                            viewModel.userPassword.length < 6 -> {
                                viewModel.errorMessage = "Password must be at least 6 characters"
                            }
                            viewModel.userPhotoLink == null -> {
                                viewModel.errorMessage = "Please upload a profile photo"
                            }
                            else -> {
                                viewModel.errorMessage = null

                                if (viewModel.userRole == UserRoles.Customer) {
                                    onNext()
                                } else {
                                    onNext()
                                    viewModel.currentStep++
                                }
                            }
                        }
                    },
                    enabled = !viewModel.isLoading,
                    modifier = Modifier
                        .width(120.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = actionTealColor,
                        disabledContainerColor = actionTealColor.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp
                        )
                    } else {
                        Text(
                            text = if (viewModel.userRole == UserRoles.Customer) "Register" else "Next",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
@Composable
fun RegisterStep5(
    viewModel: AuthViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit,
    context: Context
) {
    val backgroundGradient = remember {
        Brush.verticalGradient(
            listOf(Color(0xFFE0F2F1), backgroundColor)
        )
    }
    BackHandler(enabled = true) {
        onBack()
        viewModel.currentStep--
    }

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(horizontal = 28.dp)
                .padding(top = 48.dp, bottom = 24.dp)
                .verticalScroll(scrollState)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RegistrationProgressBar(
                viewModel.currentStep,
                totalSteps = viewModel.userSteps
            )

            Spacer(modifier = Modifier.height(40.dp))

            AppHeading("Cargo Connect")
            AppSubHeading("Attach Documents")

            Spacer(modifier = Modifier.height(32.dp))

            AppSectionHeader("Required Documents")
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FileUploadRow(
                    label = "CNIC",
                    selectedFileName = getFileName(context, viewModel.userCNICLink),
                    onFileSelected = { uri ->
                        viewModel.userCNICLink = uri
                    }
                )

                FileUploadRow(
                    label = "Driving License",
                    selectedFileName = getFileName(context, viewModel.userLicenseLink),
                    onFileSelected = { uri ->
                        viewModel.userLicenseLink = uri
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            AppSectionHeader("Photos of Your Vehicle")
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FileUploadRow(
                    label = "Vehicle Front Side",
                    selectedFileName = getFileName(context, viewModel.vehiclePhotoFront),
                    onFileSelected = { uri ->
                        viewModel.vehiclePhotoFront = uri
                    }
                )

                FileUploadRow(
                    label = "Vehicle Back Side",
                    selectedFileName = getFileName(context, viewModel.vehiclePhotoBack),
                    onFileSelected = { uri ->
                        viewModel.vehiclePhotoBack = uri
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (viewModel.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(8.dp),
                    color = actionTealColor
                )
                Text(
                    "Uploading files...",
                    color = actionTealColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            var errorText = ""
            if (viewModel.errorMessage != null) {
                errorText = viewModel.errorMessage!!
            }
            Text(
                text = errorText,
                color = statusRedColor,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        when {
                            viewModel.userCNICLink == null -> {
                                viewModel.errorMessage = "Please upload CNIC"
                            }
                            viewModel.userLicenseLink == null -> {
                                viewModel.errorMessage = "Please upload Driving License"
                            }
                            viewModel.vehiclePhotoFront == null -> {
                                viewModel.errorMessage = "Please upload vehicle front photo"
                            }
                            viewModel.vehiclePhotoBack == null -> {
                                viewModel.errorMessage = "Please upload vehicle back photo"
                            }
                            else -> {
                                viewModel.errorMessage = null
                                onNext()
                                viewModel.currentStep++
                            }
                        }
                    },
                    enabled = !viewModel.isLoading,
                    modifier = Modifier
                        .width(120.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = actionTealColor,
                        disabledContainerColor = actionTealColor.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp
                        )
                    } else {
                        Text("Finish", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
fun getFileName(context: Context, uri: Uri?): String {
    if (uri == null) return ""

    var fileName = ""
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        fileName = cursor.getString(nameIndex)
    }
    return fileName.ifEmpty { "Selected file" }
}
@Preview
@Composable
fun E() {
   SplashScreen {  }
}