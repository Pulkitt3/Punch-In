package com.app.punchinapplication.presentation.components


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max
import kotlin.math.roundToInt

/* ---------- Data models & controller ---------- */

data class SpotlightTargetState(
    val key: String,
    var bounds: Rect? = null,
    var radiusPx: Float = 0f,
    var title: String = "",
    var description: String = ""
)

class SpotlightController {
    // ordered keys for sequence
    private val _orderedKeys = mutableStateListOf<String>()
    val orderedKeys: List<String> get() = _orderedKeys

    // map key -> state
    private val _map = mutableStateMapOf<String, SpotlightTargetState>()
    private val _currentIndex = mutableStateOf(-1)
    val currentIndex: State<Int> get() = _currentIndex

    // external observe for UI
    private val _visible = MutableStateFlow(false)
    val visible = _visible.asStateFlow()

    fun registerTarget(key: String, title: String, description: String) {
        if (!_map.containsKey(key)) {
            _map[key] = SpotlightTargetState(key = key, title = title, description = description)
            _orderedKeys.add(key)
        } else {
            _map[key]?.title = title
            _map[key]?.description = description
        }
    }

    fun updateBounds(key: String, bounds: Rect, radiusPx: Float) {
        _map[key]?.bounds = bounds
        _map[key]?.radiusPx = radiusPx
    }

    fun unregister(key: String) {
        _map.remove(key)
        _orderedKeys.remove(key)
    }

    fun getStateForKey(key: String): SpotlightTargetState? = _map[key]

    fun start(sequence: List<String>? = null) {
        if (sequence != null) {
            // ensure registered keys exist
            _orderedKeys.clear()
            _orderedKeys.addAll(sequence.filter { _map.containsKey(it) })
        }
        if (_orderedKeys.isNotEmpty()) {
            _currentIndex.value = 0
            _visible.value = true
        }
    }

    fun next() {
        val idx = _currentIndex.value
        if (idx < _orderedKeys.lastIndex) {
            _currentIndex.value = idx + 1
        } else {
            finish()
        }
    }

    fun prev() {
        val idx = _currentIndex.value
        if (idx > 0) _currentIndex.value = idx - 1
    }

    fun finish() {
        _currentIndex.value = -1
        _visible.value = false
    }
}

@Composable
fun rememberSpotlightController(): SpotlightController = remember { SpotlightController() }

/* ---------- Target wrapper ---------- */

/**
 * Wrap any composable that should be targetable by the Spotlight.
 * key: unique id
 * radius: extra padding around the view in dp
 */
@Composable
fun SpotlightTarget(
    key: String,
    controller: SpotlightController,
    title: String,
    description: String,
    radius: Dp = 12.dp,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    // register (or update) target metadata
    LaunchedEffect(key) {
        controller.registerTarget(key, title, description)
    }

    Box(
        modifier = Modifier
            .onGloballyPositioned { coords ->
                // position in window coords
                val topLeft = coords.positionInWindow()
                val size = coords.size
                val rect = Rect(
                    offset = Offset(topLeft.x, topLeft.y),
                    size = Size(size.width.toFloat(), size.height.toFloat())
                )
                val radiusPx = with(density) { max(size.width, size.height) / 2f + radius.toPx() }
                controller.updateBounds(key, rect, radiusPx)
            }
    ) {
        content()
    }
}

/* ---------- Spotlight overlay (Compose-only drawing) ---------- */

@Composable
fun SpotlightOverlay(
    controller: SpotlightController,
    overlayColor: Color = Color(0xB2000000),
    bubbleColor: Color = Color(0xFF2196F3), // Blue color like in the image
    bubbleWidth: Dp = 200.dp,
    onFinish: () -> Unit
) {
    val visible by controller.visible.collectAsState(initial = false)
    val idx by controller.currentIndex
    if (!visible || idx < 0 || idx >= controller.orderedKeys.size) return

    val key = controller.orderedKeys[idx]
    val targetState = controller.getStateForKey(key) ?: return
    val bounds = targetState.bounds ?: return
    val radiusPx = targetState.radiusPx

    val density = LocalDensity.current
    val screenWidthPx = with(density) { LocalView.current.width.toFloat() }
    val screenHeightPx = with(density) { LocalView.current.height.toFloat() }

    // center point for the circle
    val center = Offset(bounds.left + bounds.width / 2f, bounds.top + bounds.height / 2f)

    // Compose animation for spotlight entrance
    var animPlay by remember { mutableStateOf(true) }
    val alpha by animateFloatAsState(if (animPlay) 1f else 0f, label = "overlay_alpha")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(alpha = alpha)
            .pointerInput(Unit) {
                // Tap anywhere to dismiss/next
                detectTapGestures {
                    controller.next()
                    if (controller.currentIndex.value == -1) {
                        onFinish()
                    }
                }
            }
            .drawBehind {
                drawIntoCanvas { canvas ->
                    // Draw full-screen dim background
                    val paintDim = Paint().apply { color = overlayColor }
                    canvas.drawRect(size.toRect(), paintDim)

                    // Clear circle using BlendMode.Clear to reveal the target
                    val paintClear = Paint().apply { blendMode = BlendMode.Clear }
                    canvas.drawCircle(center, radiusPx, paintClear)
                }
            }
    ) {
        // Calculate bubble position - above the target, slightly offset to the left
        val bubbleWidthPx = with(density) { bubbleWidth.toPx() }
        val marginPx = with(density) { 16.dp.toPx() }
        val pointerSizePx = with(density) { 12.dp.toPx() } // Size of the triangular pointer
        val bubblePaddingPx = with(density) { 16.dp.toPx() }
        val estimatedBubbleHeight = with(density) { 80.dp.toPx() }
        
        // Position bubble above the target, centered horizontally but can be offset
        val bubbleY = bounds.top - estimatedBubbleHeight - pointerSizePx - marginPx
        // Center the bubble horizontally on the target, but ensure it stays within screen bounds
        val bubbleX = (center.x - bubbleWidthPx / 2f).coerceIn(
            marginPx,
            screenWidthPx - bubbleWidthPx - marginPx
        )
        
        // Pointer position (center of the bubble bottom, pointing to target center)
        val pointerX = center.x - bubbleX // Relative to bubble
        val pointerY = estimatedBubbleHeight // At the bottom of bubble

        // Draw the blue bubble with pointer
        Box(
            modifier = Modifier
                .offset { IntOffset(bubbleX.roundToInt(), bubbleY.roundToInt()) }
                .width(bubbleWidth)
                .drawBehind {
                    // Draw rounded rectangle bubble using DrawScope API
                    val cornerRadius = with(density) { 12.dp.toPx() }
                    
                    // Draw bubble background
                    drawRoundRect(
                        color = bubbleColor,
                        topLeft = Offset(0f, 0f),
                        size = Size(bubbleWidthPx, estimatedBubbleHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                    )
                    
                    // Draw triangular pointer pointing down to the target
                    val path = Path().apply {
                        moveTo(pointerX - pointerSizePx, estimatedBubbleHeight)
                        lineTo(pointerX, estimatedBubbleHeight + pointerSizePx)
                        lineTo(pointerX + pointerSizePx, estimatedBubbleHeight)
                        close()
                    }
                    drawPath(path, bubbleColor)
                }
        ) {
            // Text content inside the bubble
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = bubblePaddingPx.dp,
                        vertical = 12.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title - bold, larger
                Text(
                    text = targetState.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Description - regular, smaller
                Text(
                    text = targetState.description,
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
