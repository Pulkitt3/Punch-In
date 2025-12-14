package com.app.punchinapplication.coachmark

import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.animation.core.TwoWayConverter


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 *a position based object should be converted to
 * AnimationVector2D, whereas an object that describes
 * rectangle bounds should convert to AnimationVector4D.
 */
internal val rectToVector = TwoWayConverter(
    convertToVector = { rect: Rect ->
        AnimationVector4D(rect.left, rect.top, rect.width, rect.height)
    },
    convertFromVector = { vector: AnimationVector4D ->
        Rect(
            offset = Offset(vector.v1, vector.v2),
            size = Size(vector.v3, vector.v4)
        )
    }
)

fun Color.invert(): Color {
    val red = 1f - this.red
    val green = 1f - this.green
    val blue = 1f - this.blue
    return Color(red, green, blue,this.alpha,this.colorSpace)
}

object DefaultRevealEffect : RevealEffect {
    private val rect = Animatable(Rect(offset = Offset.Zero, size = Size.Zero), rectToVector)

    override suspend fun enterAnimation(targetBounds: Rect) {
        val x = targetBounds.topLeft.x - 50f
        val y = targetBounds.topLeft.y - 50f
        val newOffset = Offset(x, y)
        val height = targetBounds.size.height + 100f
        val width = targetBounds.size.width + 100f
        val newSize = Size(width, height)
        val newBound = Rect(newOffset, newSize)

        rect.snapTo(Rect(targetBounds.center, size = Size.Zero))
        rect.animateTo(newBound, tween(500, easing = LinearEasing))

    }

    override suspend fun exitAnimation(targetBounds: Rect) {
        rect.animateTo(
            Rect(targetBounds.center, size = Size.Zero),
            tween(500, easing = LinearEasing)
        )
    }

    override fun drawTargetShape(targetBounds: Rect, drawScope: DrawScope): Rect {
        val x = targetBounds.topLeft.x - 50f
        val y = targetBounds.topLeft.y - 50f
        val newOffset = Offset(x, y)
        val height = targetBounds.size.height + 100f
        val width = targetBounds.size.width + 100f
        val newSize = Size(width, height)
        val newBound = Rect(newOffset, newSize)
        drawScope.apply {
            drawRect(
                color = Color.Transparent,
                size = rect.value.size,
                blendMode = BlendMode.Clear,        //this is important for custom implementation
                topLeft = rect.value.topLeft
            )
        }
        return newBound
    }
}