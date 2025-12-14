package com.app.punchinapplication.presentation.components

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

/**
 * Global Warning Alert Overlay
 * Shows a full-screen alert when 1 minute is left before punch-in is required
 * Persists dismissal state so it doesn't show again after user clicks "Got it"
 */
@Composable
fun WarningAlertOverlay(
    isWarning: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("warning_alert_prefs", Context.MODE_PRIVATE)
    }
    
    // Track if alert should be shown
    var showAlert by remember { mutableStateOf(false) }
    
    // Check if warning is active and alert hasn't been dismissed
    LaunchedEffect(isWarning) {
        if (isWarning) {
            val currentTime = System.currentTimeMillis()
            val warningStartTime = prefs.getLong("warning_start_time", 0L)
            val dismissedTime = prefs.getLong("warning_dismissed_time", 0L)
            
            // If no warning start time, this is a new warning period
            if (warningStartTime == 0L) {
                // Save warning start time
                prefs.edit().putLong("warning_start_time", currentTime).apply()
                showAlert = true
            } else {
                // Check if alert was dismissed for THIS warning period
                // If dismissedTime is 0 or less than warningStartTime, it wasn't dismissed for this period
                if (dismissedTime == 0L || dismissedTime < warningStartTime) {
                    showAlert = true
                } else {
                    // Already dismissed for this warning period
                    showAlert = false
                }
            }
        } else {
            // Warning cleared - hide alert and reset for next warning period
            showAlert = false
            // Clear tracking when warning ends (after punch-in) so next warning can show
            prefs.edit()
                .remove("warning_start_time")
                .remove("warning_dismissed_time")
                .apply()
        }
    }
    
    if (showAlert && isWarning) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Warning Icon/Emoji
                    Text(
                        text = "⏰",
                        fontSize = 64.sp
                    )
                    
                    // Title
                    Text(
                        text = "Time Warning",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        textAlign = TextAlign.Center
                    )
                    
                    // Message
                    Text(
                        text = "Only 1 minute remaining before your next punch-in is required!\n\nPlease prepare to punch in soon.",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Got it Button
                    Button(
                        onClick = {
                            // Save dismissal time - this prevents alert from showing again
                            val currentTime = System.currentTimeMillis()
                            prefs.edit()
                                .putLong("warning_dismissed_time", currentTime)
                                .apply()
                            showAlert = false
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Got it",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
