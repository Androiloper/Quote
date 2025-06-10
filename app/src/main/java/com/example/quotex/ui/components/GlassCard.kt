package com.example.quotex.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.quotex.model.Quote
import com.example.quotex.ui.theme.AccentOrange
import com.example.quotex.ui.theme.CyberBlue
import com.example.quotex.ui.theme.ElectricGreen
import com.example.quotex.ui.theme.GlassSurface
import com.example.quotex.ui.theme.GlassSurfaceDark
import com.example.quotex.ui.theme.NebulaPurple
import com.example.quotex.ui.theme.NeonPink
import androidx.compose.runtime.LaunchedEffect

import com.example.quotex.ui.theme.StarWhite // Ensure StarWhite is imported




/*
/**
 * A futuristic card with glowing borders and glass-like appearance
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = modifier
    ) {
        /*
        // Glow effect behind the card
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(4.dp)
                .alpha(glowAlpha)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                NebulaPurple.copy(alpha = 0.3f),
                                NebulaPurple.copy(alpha = 0.1f),
                                NebulaPurple.copy(alpha = 0.0f)
                            )
                        ),
                        radius = size.width * 0.8f,
                        center = Offset(size.width / 2, size.height / 2)
                    )
                }
        )

         */

        // Actual card
        Card(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            NebulaPurple.copy(alpha = 0.7f),
                            CyberBlue.copy(alpha = 0.7f),
                            NebulaPurple.copy(alpha = 0.7f)
                        )
                    ),
                    shape = MaterialTheme.shapes.medium
                ),
            colors = CardDefaults.cardColors(
                containerColor = GlassSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            content()
        }
    }
}

 */

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.2f),
                        Color.White.copy(alpha = 0.1f)
                    )
                )
            )
           // .padding(16.dp)
    ) {
        content()
    }
}

@Composable
fun FuturisticQuoteCard(
    quote: Quote,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }

    // Set visible to true after composition to trigger animation
    LaunchedEffect(Unit) { // Keyed on Unit to run once
        kotlinx.coroutines.delay(100) // Optional delay for entry
        visible = true
    }
    GlassCard(
        modifier = modifier.padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Quote text with animation
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(700)) +
                        slideInVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            initialOffsetY = { -40 }
                        )
            ) {
                Text( // Line 179
                    text = "\"${quote.text}\"", // Line 180 - CORRECTED: Escaped quotes
                    style = MaterialTheme.typography.bodyLarge, // Line 181
                    modifier = Modifier.padding(bottom = 24.dp), // Line 182
                    textAlign = TextAlign.Center, // Line 183
                    fontWeight = FontWeight.Medium, // Line 184
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2f // Line 185
                ) // Line 186
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Reference with delayed animation
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = 700,
                        delayMillis = 500
                    )
                ) + slideInVertically(
                    animationSpec = tween(700, 500),
                    initialOffsetY = { 40 }
                )
            ) {
                Text(
                    text = quote.reference,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = CyberBlue,
                    modifier = Modifier
                        .align(Alignment.End)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    GlassSurfaceDark.copy(alpha = 0.0f),
                                    GlassSurfaceDark.copy(alpha = 0.5f),
                                    GlassSurfaceDark.copy(alpha = 0.8f)
                                )
                            ),
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .height(2.dp)
                .width(24.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            NeonPink,
                            NeonPink.copy(alpha = 0.0f)
                        )
                    )
                )
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = NeonPink
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .height(2.dp)
                .weight(1f)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            NeonPink.copy(alpha = 0.7f),
                            NeonPink.copy(alpha = 0.0f)
                        )
                    )
                )
        )
    }
}

@Composable
fun FuturisticToggle(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isChecked) 1.03f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "toggle animation"
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(GlassSurface.copy(alpha = if (isChecked) 0.7f else 0.4f))
                .clickable { onCheckedChange(!isChecked) }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with glow effect
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                (if (isChecked) ElectricGreen else Color.Gray).copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isChecked) ElectricGreen else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Toggle text
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            // Custom switch
            Switch(
                checked = isChecked,
                onCheckedChange = { onCheckedChange(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NebulaPurple,
                    checkedTrackColor = ElectricGreen.copy(alpha = 0.3f),
                    checkedBorderColor = ElectricGreen,
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color.DarkGray.copy(alpha = 0.3f),
                    uncheckedBorderColor = Color.Gray
                )
            )
        }
    }
}

@Composable
fun FuturisticLoadingIndicator(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing)
        ),
        label = "rotation"
    )

    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing)
        ),
        label = "inner rotation"
    )

    Box(
        modifier = modifier.size(80.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer circle
        Box(
            modifier = Modifier
                .size(80.dp)
                .drawBehind {
                    drawCircle(
                        color = NebulaPurple,
                        style = Stroke(width = 2.dp.toPx()),
                        radius = size.width / 2
                    )
                    drawArc(
                        color = CyberBlue,
                        startAngle = rotation,
                        sweepAngle = 120f,
                        useCenter = false,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
        )

        // Middle circle
        Box(
            modifier = Modifier
                .size(50.dp)
                .drawBehind {
                    drawCircle(
                        color = NeonPink,
                        style = Stroke(width = 1.5f.dp.toPx()),
                        radius = size.width / 2
                    )
                    drawArc(
                        color = AccentOrange,
                        startAngle = innerRotation,
                        sweepAngle = 80f,
                        useCenter = false,
                        style = Stroke(width = 1.5f.dp.toPx())
                    )
                }
        )

        // Inner circle
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(NebulaPurple.copy(alpha = 0.3f), CircleShape)
                .border(1.dp, ElectricGreen, CircleShape)
        )
    }
}

// mano