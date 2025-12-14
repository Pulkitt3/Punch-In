package com.app.punchinapplication.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

/**
 * Widget instruction data
 */
data class WidgetInstruction(
    val widgetId: String,
    val title: String,
    val message: String
)

/**
 * Widget Highlight Overlay
 * Covers entire screen except highlighted widget, shows instruction for that widget
 */
@Composable
fun WidgetHighlightOverlay(
    currentWidgetId: String?,
    instructions: Map<String, WidgetInstruction>,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onDismiss: () -> Unit,
    canGoNext: Boolean,
    canGoPrevious: Boolean,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(true) }
    
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "overlay_alpha"
    )

    if (alpha > 0f && currentWidgetId != null && isVisible) {
        val instruction = instructions[currentWidgetId]
        
        if (instruction != null) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .zIndex(1000f)
                    .alpha(alpha)
            ) {
                // Dark overlay covering everything
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f))
                )
                
                // Instruction card at bottom
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Step indicator
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                instructions.keys.forEachIndexed { index, widgetId ->
                                    val isCurrent = widgetId == currentWidgetId
                                    val isPast = instructions.keys.indexOf(currentWidgetId) > index
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                color = when {
                                                    isCurrent -> MaterialTheme.colorScheme.primary
                                                    isPast -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                                },
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                    )
                                    if (index < instructions.size - 1) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                }
                            }
                            
                            Text(
                                text = instruction.title,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Text(
                                text = instruction.message,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                lineHeight = 24.sp
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (canGoPrevious) {
                                    OutlinedButton(
                                        onClick = onPrevious,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(50.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Previous", fontSize = 16.sp)
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                                
                                Button(
                                    onClick = {
                                        if (canGoNext) {
                                            onNext()
                                        } else {
                                            isVisible = false
                                            onDismiss()
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = if (canGoNext) "Next" else "Got it",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

