package com.app.punchinapplication.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.delay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.app.punchinapplication.CoachMarkActionButton
import com.app.punchinapplication.CoachMarkIconButton
import com.app.punchinapplication.CoachMarkTextView
import com.app.punchinapplication.R
import com.app.punchinapplication.coachmark.CoachMarkHost
import com.app.punchinapplication.coachmark.rememberCoachMarkState
import com.app.punchinapplication.presentation.components.DonutChartWithLegend
import com.app.punchinapplication.presentation.components.WarningAlertOverlay
import com.app.punchinapplication.presentation.navigation.ScreenLockManager
import com.app.punchinapplication.presentation.viewmodel.HomeViewModel
import com.app.punchinapplication.presentation.viewmodel.LoginViewModel
import com.app.punchinapplication.presentation.viewmodel.PunchInViewModel
import org.koin.androidx.compose.koinViewModel
import java.util.Calendar

/**
 * Home Screen
 * Displays weekly punch-in data in donut charts with overlay layout
 */
@Composable
fun HomeScreen(
    onNavigateToPunchIn: () -> Unit,
    onNavigateToRoute: () -> Unit,
    onLogout: () -> Unit,
    isLocked: Boolean,
    viewModel: HomeViewModel = koinViewModel(),
    punchInViewModel: PunchInViewModel = koinViewModel(),
    loginViewModel: LoginViewModel = koinViewModel()
) {
    val weeklyPunchIns by viewModel.weeklyPunchIns.collectAsState()
    val allPunchIns by viewModel.allPunchIns.collectAsState()
    val punchInsByDay = viewModel.getPunchInsByDay()
    val isOverdue by punchInViewModel.isOverdue.collectAsState()
    val isWarning by punchInViewModel.isWarning.collectAsState()
    
    // Use all punch-ins for recent activity, but weekly for chart
    val recentPunchIns = remember(allPunchIns) {
        allPunchIns.take(5)
    }
    
    // Update screen lock based on punch-in status
    LaunchedEffect(isOverdue) {
        if (isOverdue) {
            ScreenLockManager.lock()
        } else {
            ScreenLockManager.unlock()
        }
    }
    
    // Refresh data when screen is visible
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }
    
    // Convert day numbers to day names
    val dayNames = mapOf(
        Calendar.MONDAY to "Monday",
        Calendar.TUESDAY to "Tuesday",
        Calendar.WEDNESDAY to "Wednesday",
        Calendar.THURSDAY to "Thursday",
        Calendar.FRIDAY to "Friday",
        Calendar.SATURDAY to "Saturday",
        Calendar.SUNDAY to "Sunday"
    )
    
    val chartData = punchInsByDay.mapKeys { dayNames[it.key] ?: "Unknown" }
        .mapValues { it.value.toFloat() }
        .filter { it.value > 0 }

    
    // CoachMark state
    val coachMarkState = rememberCoachMarkState()
    
    // Scroll state for auto-scrolling to coach mark widgets
    val scrollState = rememberScrollState()
    
    // Store widget positions (position ID -> Y offset in pixels relative to Column)
    val widgetPositions = remember { mutableStateMapOf<Int, Float>() }
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    
    // Track current coach mark position and auto-scroll when it changes
    val currentCoachPosition by remember { 
        derivedStateOf { coachMarkState.currentKeyPosition } 
    }
    
    // Track if we're currently scrolling to avoid multiple scrolls and hide coach mark during scroll
    var isScrolling by remember { mutableStateOf(false) }
    var showCoachMark by remember { mutableStateOf(true) }
    
    // Auto-scroll to widget when coach mark position changes
    LaunchedEffect(currentCoachPosition) {
        if (currentCoachPosition > 0 && widgetPositions.containsKey(currentCoachPosition) && !isScrolling) {
            val targetY = widgetPositions[currentCoachPosition] ?: return@LaunchedEffect
            
            // Small initial delay to ensure layout is complete
            delay(150)
            
            // Get current scroll state
            val scrollOffset = scrollState.value
            val maxScroll = scrollState.maxValue
            
            // Calculate viewport height (screen height minus system bars and padding)
            val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }
            // Approximate system bar heights (status bar ~24dp, nav bar ~48dp)
            val systemBarsHeight = with(density) { 72.dp.toPx() }
            val padding = with(density) { 32.dp.toPx() } // Top and bottom padding
            val viewportHeight = screenHeight - systemBarsHeight - padding
            
            // Calculate widget position in scrollable space
            val widgetY = targetY
            val widgetHeight = with(density) { 150.dp.toPx() } // Approximate widget height
            
            // Check if widget is visible in viewport
            val visibleTop = scrollOffset
            val visibleBottom = scrollOffset + viewportHeight
            val widgetTop = widgetY
            val widgetBottom = widgetY + widgetHeight
            
            val isFullyVisible = widgetTop >= visibleTop && widgetBottom <= visibleBottom
            
            if (!isFullyVisible) {
                isScrolling = true
                showCoachMark = false // Hide coach mark during scroll to prevent showing with stale coordinates
                
                // Calculate target scroll position to bring widget into view with padding
                val scrollPadding = with(density) { 100.dp.toPx() }
                val targetScroll = (widgetY - scrollPadding).coerceIn(0f, maxScroll.toFloat()).toInt()
                
                // Animate scroll to bring widget into view (this is a suspend function, so it waits)
                scrollState.animateScrollTo(targetScroll)
                
                // Wait for layout coordinates to update after scroll completes
                // onGloballyPositioned should fire and update coordinates automatically when scroll happens
                delay(700) // Give sufficient time for coordinates to update via onGloballyPositioned
                
                // Show coach mark again with updated coordinates
                // The onGloballyPositioned callback should have updated the coordinates by now
                showCoachMark = true
                isScrolling = false
            } else {
                // Widget is already visible, ensure coach mark is shown
                showCoachMark = true
                delay(100) // Small delay for layout stability
            }
        }
    }
    
    CoachMarkHost(
        state = coachMarkState,
        showCoach = showCoachMark
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        widgetPositions[1] = coordinates.positionInParent().y
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CoachMarkTextView("Punch-In Dashboard", "See All weekly Punch-in data in donut chart", 1)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Coach Mark Button - Show Instructions
                    // Logout Button
                    Box(
                        modifier = Modifier.onGloballyPositioned { coordinates ->
                            widgetPositions[7] = coordinates.positionInParent().y
                        }
                    ) {
                        CoachMarkIconButton({
                            loginViewModel.logout()
                            onLogout()
                        }, "Logout", "Click on this Logout button and logout", 7, R.drawable.outline_logout)
                    }
                }
            }
            
            // Weekly Stats Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        widgetPositions[2] = coordinates.positionInParent().y
                    },
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CoachMarkTextView("Weekly Punch-Ins", "see Weekly Punch-Ins",2)
                    DonutChartWithLegend(
                        data = chartData,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            // Quick Actions
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        widgetPositions[3] = coordinates.positionInParent().y
                    },
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CoachMarkTextView("Quick Actions", "see Quick Actions ",3 )
                    Box(
                        modifier = Modifier.onGloballyPositioned { coordinates ->
                            widgetPositions[4] = coordinates.positionInParent().y
                        }
                    ) {
                        CoachMarkActionButton(onNavigateToPunchIn, "Punch In", "Click on this Punch In button and navigate to punch-in screen", 4, Icons.Default.AddCircle, true)
                    }
                    Box(
                        modifier = Modifier.onGloballyPositioned { coordinates ->
                            widgetPositions[5] = coordinates.positionInParent().y
                        }
                    ) {
                        CoachMarkActionButton(onNavigateToRoute, "Plot Route", "Click on this Plot Route button and navigate to Google map route screen", 5, Icons.Default.LocationOn, !isLocked)
                    }

                    
                  /*  ActionButton(
                        icon = Icons.Default.LocationOn,
                        text = "Plot Route",
                        onClick = onNavigateToRoute,
                        enabled = !isLocked,
                        modifier = Modifier.fillMaxWidth()
                    )*/
                }
            }
            
            // Recent Activity
            if (recentPunchIns.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            widgetPositions[6] = coordinates.positionInParent().y
                        },
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier.onGloballyPositioned { coordinates ->
                                widgetPositions[5] = coordinates.positionInParent().y
                            }
                        ) {
                            CoachMarkTextView("Recent Activity", "see top 5 Recent Activity", 6)
                        }
                        recentPunchIns.forEach { punchIn ->
                            PunchInItem(punchIn = punchIn)
                        }
                    }
                }
            }
        }
        

        // Overlay for locked state
        if (isLocked) {
            LockedOverlay(
                onNavigateToPunchIn = onNavigateToPunchIn,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(10f)
            )
        }
        
        // Global Warning Alert Overlay (shows when 1 minute is left)
        WarningAlertOverlay(
            isWarning = isWarning,
            onDismiss = { }
        )
        }
    }
}


@Composable
fun ActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 16.sp)
    }
}

@Composable
fun PunchInItem(punchIn: com.app.punchinapplication.data.local.entity.PunchInEntity) {
    val time = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        .format(java.util.Date(punchIn.timestamp))
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Lat: ${String.format("%.4f", punchIn.latitude)}, Lng: ${String.format("%.4f", punchIn.longitude)}",
                fontSize = 14.sp
            )
            Text(
                text = time,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Icon(
            Icons.Default.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun LockedOverlay(
    onNavigateToPunchIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                
                Text(
                    text = "Punch-In Required",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                
                Text(
                    text = "You need to punch in before accessing other screens",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Button(
                    onClick = onNavigateToPunchIn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Go to Punch-In", fontSize = 16.sp)
                }
            }
        }
    }
}

