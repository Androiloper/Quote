package com.example.quotex.ui.main

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.quotex.R
import com.example.quotex.ui.components.ClickableQuotePager
import com.example.quotex.ui.components.GlassCard
import com.example.quotex.ui.components.SectionHeader
import com.example.quotex.ui.theme.*
import com.example.quotex.util.ProverbsUtil

/**
 * Updated MainScreen implementation incorporating the quote chapter navigation
 * This can be used as a reference for updating your actual MainScreen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenUpdated(
    viewModel: MainViewModel,
    onSettingsClick: () -> Unit,
    onPromisesClick: () -> Unit,
    onQuoteClick: (Int) -> Unit,
    showSnackbar: (String) -> Unit
) {
    val proverbs by viewModel.proverbsForToday.observeAsState(emptyList())
    val displayMode by viewModel.displayMode.observeAsState(0)
    val displayPromises by viewModel.displayPromises.observeAsState(false)
    val scrollState = rememberScrollState()

    // Add debug logging
    LaunchedEffect(Unit) {
        Log.d("MainScreen", "Screen composing: Proverbs count = ${proverbs.size}")
    }

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
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = "Settings",
                                tint = StarWhite
                            )
                        }
                        IconButton(onClick = { /* About action */ }) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = "About",
                                tint = StarWhite
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { onPromisesClick() },
                    containerColor = NebulaPurple,
                    contentColor = StarWhite
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add Promise"
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Quote Display Section with clickable quotes
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (proverbs.isEmpty()) {
                        // Show loading
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = ElectricGreen,
                                modifier = Modifier.padding(24.dp)
                            )
                            Text(
                                text = "Loading today's wisdom...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = StarWhite,
                                modifier = Modifier.padding(top = 16.dp)
                            )

                            // Debug call to explicitly load quotes - remove in production
                            LaunchedEffect(Unit) {
                                Log.d("MainScreen", "Triggering quote refresh")
                                viewModel.refreshQuotes()
                            }
                        }
                    } else {
                        // Clickable Quote Pager - when user clicks, navigate to chapter screen
                        ClickableQuotePager(
                            quotes = proverbs,
                            onQuoteClick = { chapterNumber ->
                                Log.d("MainScreen", "Quote clicked - navigating to chapter $chapterNumber")
                                onQuoteClick(chapterNumber)
                            }
                        )
                    }
                }

                // Daily Inspiration Button
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TODAY'S INSPIRATION",
                            style = MaterialTheme.typography.titleMedium,
                            color = ElectricGreen
                        )

                        Text(
                            text = "\"A verse each day keeps the troubles away\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StarWhite,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Button(
                            onClick = { viewModel.refreshQuotes() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NebulaPurple
                            ),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("REFRESH QUOTE")
                        }
                    }
                }

                // Browse All Chapters Card - NEW FEATURE
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "BROWSE WISDOM",
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonPink
                        )

                        Text(
                            text = "Explore all verses by chapter",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StarWhite,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // Chapter buttons - just showing a few examples
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (chapter in 1..5) {
                                ChapterButton(
                                    chapter = chapter,
                                    onClick = { onQuoteClick(chapter) }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (chapter in 6..10) {
                                ChapterButton(
                                    chapter = chapter,
                                    onClick = { onQuoteClick(chapter) }
                                )
                            }
                        }
                    }
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
                            .clickable {
                                onPromisesClick()
                            },
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

@Composable
fun ChapterButton(
    chapter: Int,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = NebulaPurple.copy(alpha = 0.7f),
            contentColor = StarWhite
        ),
        modifier = Modifier.size(48.dp),
        contentPadding = PaddingValues(0.dp),
        shape = CircleShape
    ) {
        Text(
            text = "$chapter",
            style = MaterialTheme.typography.titleMedium
        )
    }
}