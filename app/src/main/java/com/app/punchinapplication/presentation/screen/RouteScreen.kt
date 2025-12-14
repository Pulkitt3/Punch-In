package com.app.punchinapplication.presentation.screen

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.app.punchinapplication.CoachMarkFloatingActionButton
import com.app.punchinapplication.coachmark.CoachMarkHost
import com.app.punchinapplication.coachmark.rememberCoachMarkState
import com.app.punchinapplication.data.local.entity.PunchInEntity
import com.app.punchinapplication.presentation.components.WarningAlertOverlay
import com.app.punchinapplication.presentation.viewmodel.PunchInViewModel
import com.app.punchinapplication.presentation.viewmodel.RouteViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Route Screen
 * Displays Google Maps with route plotting based on selected punch-ins
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteScreen(
    onNavigateBack: () -> Unit,
    isLocked: Boolean,
    viewModel: RouteViewModel = koinViewModel(),
    punchInViewModel: PunchInViewModel = koinViewModel()
) {
    val allPunchIns by viewModel.allPunchIns.collectAsState()
    val selectedPunchIns by viewModel.selectedPunchIns.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isWarning by punchInViewModel.isWarning.collectAsState()
    
    // State for bottom sheet
    var showBottomSheet by remember { mutableStateOf(false) }
    
    // Current location state
    val context = LocalContext.current
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    // Permission state
    var hasLocationPermission by remember { mutableStateOf(false) }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        hasLocationPermission = fineLocationGranted || coarseLocationGranted
        
        if (hasLocationPermission) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        currentLocation = location
                    }
                }
            } catch (e: SecurityException) {
                // Permission denied
            }
        }
    }
    
    // Check and request permissions on first load
    LaunchedEffect(Unit) {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        hasLocationPermission = fineLocationGranted || coarseLocationGranted
        
        if (hasLocationPermission) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        currentLocation = location
                    }
                }
            } catch (e: SecurityException) {
                // Permission denied
            }
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
    
    // Auto-select all punch-ins when screen loads
    LaunchedEffect(allPunchIns) {
        if (allPunchIns.isNotEmpty() && selectedPunchIns.isEmpty()) {
            viewModel.selectAll()
        }
    }
    
    // Get first punch-in (sorted by timestamp) for initial zoom
    val firstPunchIn = remember(selectedPunchIns) {
        selectedPunchIns.sortedBy { it.timestamp }.firstOrNull()
    }
    
    // Calculate bounds for all selected punch-ins to fit them in view
    val bounds = remember(selectedPunchIns) {
        if (selectedPunchIns.isNotEmpty()) {
            val builder = LatLngBounds.builder()
            selectedPunchIns.forEach { punchIn ->
                builder.include(LatLng(punchIn.latitude, punchIn.longitude))
            }
            builder.build()
        } else {
            null
        }
    }
    
    // Calculate center point
    val center = remember(selectedPunchIns, currentLocation) {
        if (selectedPunchIns.isNotEmpty()) {
            val avgLat = selectedPunchIns.map { it.latitude }.average()
            val avgLng = selectedPunchIns.map { it.longitude }.average()
            LatLng(avgLat, avgLng)
        } else if (currentLocation != null) {
            // Use current location if no punch-ins available
            LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
        } else {
            LatLng(0.0, 0.0) // Default location
        }
    }
    
    // Initialize camera to first punch-in location or current location
    val cameraPositionState = rememberCameraPositionState {
        position = when {
            firstPunchIn != null -> {
                CameraPosition.fromLatLngZoom(
                    LatLng(firstPunchIn.latitude, firstPunchIn.longitude),
                    15f
                )
            }
            currentLocation != null -> {
                CameraPosition.fromLatLngZoom(
                    LatLng(currentLocation!!.latitude, currentLocation!!.longitude),
                    15f
                )
            }
            else -> {
                CameraPosition.fromLatLngZoom(center, 15f)
            }
        }
    }
    
    // Update camera: zoom to first location, then show all bounds, or focus on current location if no data
    LaunchedEffect(selectedPunchIns, firstPunchIn, currentLocation) {
        if (selectedPunchIns.isNotEmpty()) {
            // First, zoom to the first punch-in location (chronologically first)
            if (firstPunchIn != null) {
                val firstPosition = CameraPosition.fromLatLngZoom(
                    LatLng(firstPunchIn.latitude, firstPunchIn.longitude),
                    15f
                )
                cameraPositionState.animate(
                    CameraUpdateFactory.newCameraPosition(firstPosition)
                )
                
                // After a short delay, zoom out to show all points with bounds
                if (selectedPunchIns.size > 1 && bounds != null) {
                    delay(1000)
                    val padding = 100 // Padding in pixels
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngBounds(bounds, padding)
                    )
                }
            } else if (bounds != null) {
                // Fallback: show all bounds if first location not available
                val padding = 100
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(bounds, padding)
                )
            }
        } else if (currentLocation != null) {
            // No data available - focus on current location
            val currentPosition = CameraPosition.fromLatLngZoom(
                LatLng(currentLocation!!.latitude, currentLocation!!.longitude),
                15f
            )
            cameraPositionState.animate(
                CameraUpdateFactory.newCameraPosition(currentPosition)
            )
        }
    }
    
    // CoachMark state
    val coachMarkState = rememberCoachMarkState()
    
    CoachMarkHost(
        state = coachMarkState
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Plot Route") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showBottomSheet = !showBottomSheet }) {
                            Icon(Icons.Default.List, contentDescription = "Show Punch-Ins")
                        }
                    }
                )
            },
            floatingActionButton = {
                if (selectedPunchIns.isNotEmpty()) {
                    CoachMarkFloatingActionButton(onClick = { showBottomSheet = !showBottomSheet })
                }
            }
        ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Full screen map
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    mapType = MapType.NORMAL,
                    isMyLocationEnabled = selectedPunchIns.isEmpty() && hasLocationPermission // Show user location when no data and permission granted
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = selectedPunchIns.isEmpty() && hasLocationPermission, // Enable when no data and permission granted
                    mapToolbarEnabled = false
                )
            ) {
                // Draw markers for all selected punch-ins
                selectedPunchIns.forEachIndexed { index, punchIn ->
                    Marker(
                        state = MarkerState(
                            position = LatLng(punchIn.latitude, punchIn.longitude)
                        ),
                        title = "Punch-In ${index + 1}",
                        snippet = SimpleDateFormat("HH:mm", Locale.getDefault())
                            .format(Date(punchIn.timestamp))
                    )
                }
                
                // Show current location marker when no punch-ins are available
                if (selectedPunchIns.isEmpty() && currentLocation != null) {
                    Marker(
                        state = MarkerState(
                            position = LatLng(
                                currentLocation!!.latitude,
                                currentLocation!!.longitude
                            )
                        ),
                        title = "Current Location",
                        snippet = "Your current position"
                    )
                }
                
                // Draw polyline connecting all selected punch-ins in order
                if (selectedPunchIns.size > 1) {
                    // Sort by timestamp to create route in chronological order
                    val sortedPunchIns = selectedPunchIns.sortedBy { it.timestamp }
                    val points = sortedPunchIns.map {
                        LatLng(it.latitude, it.longitude)
                    }
                    Polyline(
                        points = points,
                        color = MaterialTheme.colorScheme.primary,
                        width = 10f
                    )
                }
            }
            
            // Lock overlay
            if (isLocked) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(32.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Please complete punch-in first",
                            modifier = Modifier.padding(24.dp),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        
        // Bottom sheet for punch-in selection
        if (showBottomSheet) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showBottomSheet = false }
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 600.dp)
                        .clickable(enabled = false, onClick = { }), // Prevent clicks from propagating
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Drag handle
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(4.dp)
                                    .background(
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                        RoundedCornerShape(2.dp)
                                    )
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Select Punch-Ins",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { showBottomSheet = false }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Close")
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = { viewModel.selectAll() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Select All", fontSize = 12.sp)
                            }
                            TextButton(
                                onClick = { viewModel.clearSelections() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Clear", fontSize = 12.sp)
                            }
                        }
                        
                        if (isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.height(400.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(allPunchIns) { punchIn ->
                                    PunchInListItem(
                                        punchIn = punchIn,
                                        isSelected = selectedPunchIns.contains(punchIn),
                                        onToggle = { viewModel.togglePunchInSelection(punchIn) }
                                    )
                                }
                                
                                if (allPunchIns.isEmpty()) {
                                    item {
                                        Text(
                                            text = "No punch-ins available",
                                            modifier = Modifier.padding(16.dp),
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }
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
@Composable
fun PunchInListItem(
    punchIn: PunchInEntity,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val time = SimpleDateFormat("HH:mm", Locale.getDefault())
        .format(Date(punchIn.timestamp))
    val date = SimpleDateFormat("MMM dd", Locale.getDefault())
        .format(Date(punchIn.timestamp))
    
    Card(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isSelected) {
                        Icons.Default.CheckCircle
                    } else {
                        Icons.Default.Add
                    },
                    contentDescription = null,
                    tint = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    }
                )
                Column {
                    Text(
                        text = "$date at $time",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${String.format("%.4f", punchIn.latitude)}, ${String.format("%.4f", punchIn.longitude)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}


