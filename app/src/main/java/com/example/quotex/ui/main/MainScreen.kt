package com.example.quotex.ui.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DateRange //alarm
import androidx.compose.material.icons.rounded.ExitToApp //darkmode
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.quotex.model.Quote
import com.example.quotex.ui.components.FuturisticLoadingIndicator
import com.example.quotex.ui.components.FuturisticQuoteCard
import com.example.quotex.ui.components.FuturisticToggle
import com.example.quotex.ui.components.GlassCard
import com.example.quotex.ui.components.SectionHeader
import com.example.quotex.ui.theme.CosmicBlack
import com.example.quotex.ui.theme.CyberBlue
import com.example.quotex.ui.theme.DeepSpace
import com.example.quotex.ui.theme.ElectricGreen
import com.example.quotex.ui.theme.GlassSurface
import com.example.quotex.ui.theme.NebulaPurple
import com.example.quotex.ui.theme.NeonPink
import com.example.quotex.ui.theme.StarWhite
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.painterResource
import com.example.quotex.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    onPromisesClick: () -> Unit,
    showSnackbar: (String) -> Unit
) {
    val proverbs by viewModel.proverbsForToday.observeAsState(emptyList())
    val displayMode by viewModel.displayMode.observeAsState(0)
    val displayPromises by viewModel.displayPromises.observeAsState(false)

    val scrollState = rememberScrollState()

    // Background stars for cosmic feel
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
                            "COSMIC WISDOM",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = GlassSurface.copy(alpha = 0.5f),
                        titleContentColor = StarWhite
                    ),
                    actions = {
                        IconButton(onClick = {}) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = "Settings",
                                tint = StarWhite
                            )
                        }
                        IconButton(onClick = {}) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = "About",
                                tint = StarWhite
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Quote Display Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (proverbs.isEmpty()) {
                        FuturisticLoadingIndicator(
                            modifier = Modifier.padding(24.dp)
                        )
                    } else {
                        // Quote Pager
                        val pagerState = rememberPagerState(pageCount = { proverbs.size })

                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxWidth()
                        ) { page ->
                            FuturisticQuoteCard(
                                quote = proverbs[page],
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Settings Section
                SectionHeader(
                    title = "INTERFACE SETTINGS",
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 16.dp),
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
                        icon = Icons.Rounded.ExitToApp
                    )

                    // Daily Reminder Setting (Demo only)
                    var dailyReminder by remember { mutableStateOf(false) }
                    FuturisticToggle(
                        text = "Daily Reminder",
                        isChecked = dailyReminder,
                        onCheckedChange = { dailyReminder = it },
                        icon = Icons.Rounded.DateRange
                    )
                }

                // Promises Button
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        NeonPink.copy(alpha = 0.2f),
                                        NeonPink.copy(alpha = 0.4f),
                                        NeonPink.copy(alpha = 0.2f)
                                    )
                                )
                            )
                            .padding(16.dp)
                            .clickable { onPromisesClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "VIEW PROMISES",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = StarWhite
                        )
                    }
                }

                // App Info
                Text(
                    text = "QuoteX v1.0 | Daily Cosmic Wisdom",
                    style = MaterialTheme.typography.bodySmall,
                    color = StarWhite.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
        }
    }
}