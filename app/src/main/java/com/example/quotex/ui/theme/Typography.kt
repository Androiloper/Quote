package com.example.quotex.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quotex.R

// We'll use custom fonts for the futuristic look
// Note: You'll need to add these fonts to your res/font directory
val EudoxusSans = FontFamily(
    Font(R.font.eudoxus_sans_regular, FontWeight.Normal),
    Font(R.font.eudoxus_sans_medium, FontWeight.Medium),
    Font(R.font.eudoxus_sans_bold, FontWeight.Bold)
)

val TypographyRe = Typography(
    // Large display text for quotes
    displayLarge = TextStyle(
        fontFamily = EudoxusSans,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp
    ),
    // Medium display text for section headers
    displayMedium = TextStyle(
        fontFamily = EudoxusSans,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.25).sp
    ),
    // Small display text
    displaySmall = TextStyle(
        fontFamily = EudoxusSans,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    // Headline text for card titles
    headlineLarge = TextStyle(
        fontFamily = EudoxusSans,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    // Body text for quotes
    bodyLarge = TextStyle(
        fontFamily = EudoxusSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    // Reference text for quote sources
    bodyMedium = TextStyle(
        fontFamily = EudoxusSans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    // Small text for metadata
    bodySmall = TextStyle(
        fontFamily = EudoxusSans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp
    ),
    // Title text for settings
    titleLarge = TextStyle(
        fontFamily = EudoxusSans,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    )
)

// Modernized shapes with smoother rounded corners
val Shapes = Shapes(
    // Small components like buttons
    small = RoundedCornerShape(12.dp),
    // Medium components like cards
    medium = RoundedCornerShape(16.dp),
    // Large components like bottom sheets
    large = RoundedCornerShape(24.dp)
)