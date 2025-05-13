// ui/settings/SettingsScreen.kt
package com.example.quotex.ui.settings

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quotex.R
import com.example.quotex.ui.components.FuturisticToggle
import com.example.quotex.ui.components.GlassCard
import com.example.quotex.ui.components.SectionHeader
import com.example.quotex.ui.main.MainViewModel
import com.example.quotex.ui.theme.*



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    // Observe the state
    val displayMode by viewModel.displayMode.observeAsState(0)
    val displayPromises by viewModel.displayPromises.observeAsState(false)
    val scrollState = rememberScrollState()

    // Background with cosmic theme
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpace)
            .drawBehind {
                // Create a gradient background with stars
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            CosmicBlack,
                            DeepSpace
                        )
                    )
                )

                // Small cosmic particles
                for (i in 0..100) {
                    val x = (Math.random() * size.width).toFloat()
                    val y = (Math.random() * size.height).toFloat()
                    val radius = (Math.random() * 2f + 0.5f).toFloat()
                    val alpha = (Math.random() * 0.8f + 0.2f).toFloat()

                    drawCircle(
                        color = StarWhite.copy(alpha = alpha),
                        radius = radius,
                        center = Offset(x, y)
                    )
                }

                // Larger stars
                for (i in 0..15) {
                    val x = (Math.random() * size.width).toFloat()
                    val y = (Math.random() * size.height).toFloat()
                    val radius = (Math.random() * 3f + 1.5f).toFloat()

                    drawCircle(
                        color = StarWhite,
                        radius = radius,
                        center = Offset(x, y)
                    )
                }

                // A few colored stars
                for (i in 0..5) {
                    val x = (Math.random() * size.width).toFloat()
                    val y = (Math.random() * size.height).toFloat()
                    val radius = (Math.random() * 3f + 1f).toFloat()

                    val colors = listOf(CyberBlue, NeonPink, ElectricGreen, NebulaPurple)
                    val color = colors[(Math.random() * colors.size).toInt()]

                    drawCircle(
                        color = color.copy(alpha = 0.7f),
                        radius = radius,
                        center = Offset(x, y)
                    )
                }
            }
    ) {
        // Nebula effect in the background
        Image(
            painter = painterResource(id = R.drawable.nebula_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.3f)
                .blur(20.dp)
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentColor = StarWhite,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "SETTINGS",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = StarWhite
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = GlassSurface.copy(alpha = 0.5f),
                        titleContentColor = StarWhite,
                        navigationIconContentColor = StarWhite
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Display Settings Section
                SectionHeader(
                    title = "DISPLAY OPTIONS",
                    modifier = Modifier.fillMaxWidth()
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Display Mode Setting
                    FuturisticToggle(
                        text = when (displayMode) {
                            0 -> "Disable Lock Screen Quotes"
                            1 -> "Display on Screen On"
                            else -> "Display on Unlock"
                        },
                        isChecked = displayMode > 0,
                        onCheckedChange = { viewModel.toggleDisplayMode() },
                        icon = Icons.Rounded.Notifications
                    )

                    // Display Promises Setting
                    FuturisticToggle(
                        text = "Display Promises",
                        isChecked = displayPromises,
                        onCheckedChange = { viewModel.toggleDisplayPromises() },
                        icon = Icons.Rounded.Favorite
                    )

                    // Theme Setting (Demo only)
                    var isDarkTheme by remember { mutableStateOf(true) }
                    FuturisticToggle(
                        text = "Dark Theme",
                        isChecked = isDarkTheme,
                        onCheckedChange = { isDarkTheme = it },
                        icon = Icons.Rounded.Info // Using Info icon as a placeholder for Dark Mode
                    )

                    // Daily Reminder Setting (Demo only)
                    var dailyReminder by remember { mutableStateOf(false) }
                    FuturisticToggle(
                        text = "Daily Reminder",
                        isChecked = dailyReminder,
                        onCheckedChange = { dailyReminder = it },
                        icon = Icons.Rounded.Info // Using Alarm icon as a placeholder for Daily Reminder
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Appearance Settings Section
                SectionHeader(
                    title = "APPEARANCE",
                    modifier = Modifier.fillMaxWidth()
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Font Size Setting (Demo)
                    var fontSize by remember { mutableStateOf(1) } // 0=small, 1=medium, 2=large

                    GlassCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Font Size",
                                style = MaterialTheme.typography.titleMedium,
                                color = StarWhite
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    onClick = { if (fontSize > 0) fontSize-- },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (fontSize == 0) NebulaPurple else GlassSurface
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Small")
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = { fontSize = 1 },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (fontSize == 1) NebulaPurple else GlassSurface
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Medium")
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = { if (fontSize < 2) fontSize++ },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (fontSize == 2) NebulaPurple else GlassSurface
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Large")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Advanced Settings Section
                SectionHeader(
                    title = "ADVANCED",
                    modifier = Modifier.fillMaxWidth()
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Storage Options
                    GlassCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Info, // Using Info icon as a placeholder for Storage Options
                                contentDescription = null,
                                tint = ElectricGreen,
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Data Management",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = StarWhite
                                )

                                Text(
                                    text = "Backup and restore your promises",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = StarWhite.copy(alpha = 0.7f)
                                )
                            }

                            IconButton(onClick = { /* Open data management dialog */ }) {
                                Icon(
                                    imageVector = Icons.Rounded.Info, // Using Info icon as a placeholder for the action
                                    contentDescription = "Open",
                                    tint = StarWhite
                                )
                            }
                        }
                    }

                    // About
                    GlassCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = null,
                                tint = CyberBlue,
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "About QuoteX",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = StarWhite
                                )

                                Text(
                                    text = "Version 1.0.0",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = StarWhite.copy(alpha = 0.7f)
                                )
                            }

                            IconButton(onClick = { /* Open about screen */ }) {
                                Icon(
                                    imageVector = Icons.Rounded.Info,   // Using Info icon as a placeholder for the action
                                    contentDescription = "Open",
                                    tint = StarWhite
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}