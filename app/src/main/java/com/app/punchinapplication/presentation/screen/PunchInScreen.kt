package com.app.punchinapplication.presentation.screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.app.punchinapplication.CoachMarkIconButton
import com.app.punchinapplication.CoachMarkTextView
import com.app.punchinapplication.R
import com.app.punchinapplication.coachmark.CircleRevealEffect
import com.app.punchinapplication.coachmark.CoachMarkHost
import com.app.punchinapplication.coachmark.NoCoachMarkButtons
import com.app.punchinapplication.coachmark.rememberCoachMarkState
import com.app.punchinapplication.presentation.components.WarningAlertOverlay
import com.app.punchinapplication.presentation.navigation.ScreenLockManager
import com.app.punchinapplication.presentation.viewmodel.PunchInViewModel
import com.app.punchinapplication.util.PunchInTimer
import com.app.punchinapplication.withCoachMark
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

/**
 * Punch-In Screen
 * Handles location tracking and punch-in recording
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PunchInScreen(
    onNavigateBack: () -> Unit,
    viewModel: PunchInViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val currentLocation by viewModel.currentLocation.collectAsState()
    val isPunchingIn by viewModel.isPunchingIn.collectAsState()
    val punchInSuccess by viewModel.punchInSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val timeRemaining by viewModel.timeRemaining.collectAsState()
    val isWarning by viewModel.isWarning.collectAsState()
    val isOverdue by viewModel.isOverdue.collectAsState()
    
    val timer = PunchInTimer()
    val formattedTime = timer.formatTime(timeRemaining)
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (fineLocationGranted || coarseLocationGranted) {
            viewModel.getCurrentLocation()
        } else {
            viewModel.clearError()
            // Error will be shown through errorMessage state
        }
    }
    
    // Check and request permissions on first load
    LaunchedEffect(Unit) {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        
        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        
        if (fineLocationGranted || coarseLocationGranted) {
            viewModel.getCurrentLocation()
        } else {
            // Request permissions
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    
    LaunchedEffect(punchInSuccess) {
        if (punchInSuccess) {
            ScreenLockManager.unlock()
            // Delay before clearing success message so user can see it
            delay(3000) // 3 seconds
            viewModel.clearSuccess()
        }
    }
    
    LaunchedEffect(isOverdue) {
        if (isOverdue) {
            ScreenLockManager.lock()
        } else {
            ScreenLockManager.unlock()
        }
    }
    
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            // Delay before clearing error message so user can see it
            delay(4000) // 4 seconds (longer for errors)
            viewModel.clearError()
        }
    }

    val coachMarkState = rememberCoachMarkState()

    CoachMarkHost(
        state = coachMarkState
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { CoachMarkTextView("Punch-In", "Punch-In Screen",2) },
                    navigationIcon = {
                        CoachMarkIconButton(
                            onNavigateBack, "Back", "Click on this back Arrow button to go previous page", 1, R.drawable.outline_arrow_left)
                      /*  IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }*/
                    }
                )
            }
        )
        { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                withCoachMark(
                    position = 3,
                    title = "Punch-in Timer",
                    description = "Timer running when you punch-in and 10 min timer is set.",
                    revealEffect = com.app.punchinapplication.coachmark.DefaultRevealEffect,
                    backgroundCoachStyle = NoCoachMarkButtons
                ) {
                    // Timer Card

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isOverdue -> MaterialTheme.colorScheme.errorContainer
                                isWarning -> MaterialTheme.colorScheme.tertiaryContainer
                                else -> MaterialTheme.colorScheme.primaryContainer
                            }
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    )
                    {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = if (isOverdue) "Overdue!" else "Time Remaining",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = when {
                                    isOverdue -> MaterialTheme.colorScheme.onErrorContainer
                                    isWarning -> MaterialTheme.colorScheme.onTertiaryContainer
                                    else -> MaterialTheme.colorScheme.onPrimaryContainer
                                }
                            )

                            Text(
                                text = formattedTime,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    isOverdue -> MaterialTheme.colorScheme.onErrorContainer
                                    isWarning -> MaterialTheme.colorScheme.onTertiaryContainer
                                    else -> MaterialTheme.colorScheme.onPrimaryContainer
                                }
                            )

                            if (isWarning && !isOverdue) {
                                Text(
                                    text = "⚠️ Warning: Punch-in required soon!",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }

                            if (isOverdue) {
                                Text(
                                    text = "⚠️ Please punch in immediately!",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
                // Location Card
                withCoachMark(
                    position = 4,
                    title = "Current Location",
                    description = " This is a Current Location lat lng data",
                    revealEffect = com.app.punchinapplication.coachmark.DefaultRevealEffect,
                    backgroundCoachStyle = NoCoachMarkButtons
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    )
                    {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Current Location",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (currentLocation != null) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Latitude: ${
                                            String.format(
                                                "%.6f",
                                                currentLocation!!.latitude
                                            )
                                        }",
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Longitude: ${
                                            String.format(
                                                "%.6f",
                                                currentLocation!!.longitude
                                            )
                                        }",
                                        fontSize = 14.sp
                                    )
                                }
                            } else {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                Text(
                                    text = "Getting location...",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }

                            TextButton(
                                onClick = {
                                    if (viewModel.hasLocationPermission()) {
                                        viewModel.getCurrentLocation()
                                    } else {
                                        permissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    }
                                }
                            ) {
                                Text("Refresh Location")
                            }
                        }
                    }
                }

                withCoachMark(
                    position = 5,
                    title = "Punch In Button",
                    description = "Click on this and punch-in lat lng data",
                    revealEffect = com.app.punchinapplication.coachmark.DefaultRevealEffect,
                    backgroundCoachStyle = NoCoachMarkButtons
                ) {
                    // Punch In Button
                    Button(
                        onClick = { viewModel.punchIn() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        enabled = !isPunchingIn && currentLocation != null,
                        shape = RoundedCornerShape(12.dp)
                    )
                    {
                        if (isPunchingIn) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(
                                text = "Punch In",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Success/Error Messages
                if (punchInSuccess) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = "✓ Punch-in recorded successfully!",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (errorMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "✗ $errorMessage",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Global Warning Alert Overlay (shows when 1 minute is left)
            WarningAlertOverlay(
                isWarning = isWarning,
                onDismiss = { }
            )
        }
    }
}

