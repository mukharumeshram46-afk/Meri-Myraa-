package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.assistant.AssistantState

@Composable
fun MyraaAvatar(
    state: AssistantState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatar_anim")

    // Breathing pulse
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = if (state == AssistantState.LISTENING || state == AssistantState.SPEAKING) 1.08f else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (state == AssistantState.LISTENING) 900 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Outer ring rotation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Color based on state
    val targetPrimaryColor = when (state) {
        AssistantState.IDLE -> Color(0xFF00E5FF)
        AssistantState.LISTENING -> Color(0xFF38BDF8)
        AssistantState.THINKING -> Color(0xFFFFB703)
        AssistantState.SPEAKING -> Color(0xFFFF2A85)
        AssistantState.EXECUTING -> Color(0xFFA855F7)
        AssistantState.SUCCESS -> Color(0xFF10B981)
        AssistantState.ERROR -> Color(0xFFEF4444)
        AssistantState.OFFLINE -> Color(0xFF64748B)
    }

    val animatedGlowColor by animateColorAsState(
        targetValue = targetPrimaryColor,
        animationSpec = tween(400),
        label = "glow_color"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .scale(pulseScale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag("myraa_avatar")
    ) {
        // Futuristic orbital glow canvas
        Canvas(modifier = Modifier.matchParentSize().rotate(rotation)) {
            val center = Offset(this.size.width / 2, this.size.height / 2)
            val radius = this.size.minDimension / 2 - 4.dp.toPx()

            // Outer glow halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        animatedGlowColor.copy(alpha = 0.45f),
                        animatedGlowColor.copy(alpha = 0.1f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius + 12.dp.toPx()
                ),
                radius = radius + 12.dp.toPx(),
                center = center
            )

            // Outer decorative cyber dash ring
            drawCircle(
                color = animatedGlowColor.copy(alpha = 0.7f),
                radius = radius,
                center = center,
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                        floatArrayOf(16f, 12f, 4f, 12f),
                        0f
                    )
                )
            )

            // Inner cyan thin ring
            drawCircle(
                color = Color.White.copy(alpha = 0.5f),
                radius = radius - 6.dp.toPx(),
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Inner Avatar Artwork
        Box(
            modifier = Modifier
                .size(size - 24.dp)
                .clip(CircleShape)
        ) {
            Image(
                painter = painterResource(id = R.drawable.myraa_avatar),
                contentDescription = "MYRAA Holographic Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }
    }
}
