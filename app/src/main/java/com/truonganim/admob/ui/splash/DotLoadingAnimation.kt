package com.truonganim.admob.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Dot Loading Animation
 * 5 dots bouncing up and down in wave pattern
 */
@Composable
fun DotLoadingAnimation(
    modifier: Modifier = Modifier,
    dotSize: Dp = 8.dp,
    dotColor: Color = Color.White,
    dotSpacing: Dp = 6.dp,
    animationDuration: Int = 1200
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dot_loading")

    // Create 5 dots with wave effect
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(dotSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(5) { index ->
            val offsetY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -15f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = animationDuration
                        0f at 0 with FastOutSlowInEasing
                        -15f at (animationDuration / 2) with FastOutSlowInEasing
                        0f at animationDuration with FastOutSlowInEasing
                    },
                    repeatMode = RepeatMode.Restart,
                    initialStartOffset = StartOffset(index * (animationDuration / 5))
                ),
                label = "dot_$index"
            )

            Box(
                modifier = Modifier
                    .size(dotSize)
                    .offset(y = offsetY.dp)
                    .background(dotColor, CircleShape)
            )
        }
    }
}

