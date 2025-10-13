package com.truonganim.admob.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Beautiful gradient button with animation
 */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    gradientColors: List<Color> = listOf(
        Color(0xFFE91E63), // Pink
        Color(0xFF9C27B0)  // Purple
    ),
    height: Dp = 56.dp,
    cornerRadius: Dp = 28.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Scale animation when pressed
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    // Shimmer animation
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )
    
    Box(
        modifier = modifier
            .height(height)
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 4.dp else 8.dp,
                shape = RoundedCornerShape(cornerRadius),
                clip = false
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                brush = Brush.horizontalGradient(
                    colors = if (enabled) gradientColors else listOf(
                        Color.Gray.copy(alpha = 0.5f),
                        Color.Gray.copy(alpha = 0.3f)
                    )
                )
            )
            .border(
                width = 2.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(cornerRadius)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !isLoading,
                onClick = onClick
            )
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

/**
 * Preset gradient color schemes
 */
object GradientPresets {
    val Purple = listOf(
        Color(0xFF9C27B0),
        Color(0xFF673AB7)
    )
    
    val Blue = listOf(
        Color(0xFF2196F3),
        Color(0xFF0288D1)
    )
    
    val Pink = listOf(
        Color(0xFFE91E63),
        Color(0xFF9C27B0)
    )
    
    val Orange = listOf(
        Color(0xFFFF5722),
        Color(0xFFFF9800)
    )
    
    val Green = listOf(
        Color(0xFF4CAF50),
        Color(0xFF8BC34A)
    )
    
    val Cyan = listOf(
        Color(0xFF00BCD4),
        Color(0xFF0097A7)
    )
    
    val DeepPurple = listOf(
        Color(0xFF673AB7),
        Color(0xFF512DA8)
    )
    
    val Teal = listOf(
        Color(0xFF009688),
        Color(0xFF00796B)
    )
    
    // Special gradients
    val Sunset = listOf(
        Color(0xFFFF6B6B),
        Color(0xFFFFE66D)
    )
    
    val Ocean = listOf(
        Color(0xFF667EEA),
        Color(0xFF764BA2)
    )
    
    val Fire = listOf(
        Color(0xFFFF416C),
        Color(0xFFFF4B2B)
    )
    
    val Aurora = listOf(
        Color(0xFF00F260),
        Color(0xFF0575E6)
    )
}

