package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun WaveformVisualizer(
    amplitude: Float,
    isListeningOrSpeaking: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val barCount = 28
    val primaryCyan = Color(0xFF00E5FF)
    val magentaPink = Color(0xFFFF2A85)
    val idleBlue = Color(0xFF38BDF8)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .testTag("waveform_visualizer")
    ) {
        val totalWidth = size.width
        val maxHeight = size.height
        val barWidth = (totalWidth / (barCount * 1.8f)).coerceAtLeast(3f)
        val gap = (totalWidth - (barCount * barWidth)) / (barCount + 1)

        val activeAmp = if (isListeningOrSpeaking) {
            amplitude.coerceIn(0.15f, 1.0f)
        } else {
            0.08f
        }

        for (i in 0 until barCount) {
            val normalizedIdx = i.toFloat() / barCount
            val wave = sin((normalizedIdx * 4 * Math.PI + phase).toDouble()).toFloat()
            val centerWeight = 1f - kotlin.math.abs(0.5f - normalizedIdx) * 1.2f

            val dynamicHeight = if (isListeningOrSpeaking) {
                (maxHeight * (0.25f + 0.75f * activeAmp * (0.5f + 0.5f * wave) * centerWeight))
                    .coerceIn(6f, maxHeight)
            } else {
                (maxHeight * 0.12f * (0.8f + 0.2f * wave)).coerceIn(4f, maxHeight * 0.2f)
            }

            val x = gap + i * (barWidth + gap)
            val y = (maxHeight - dynamicHeight) / 2f

            val brush = Brush.verticalGradient(
                colors = listOf(primaryCyan, magentaPink),
                startY = y,
                endY = y + dynamicHeight
            )

            drawRoundRect(
                brush = brush,
                topLeft = Offset(x, y),
                size = Size(barWidth, dynamicHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}
