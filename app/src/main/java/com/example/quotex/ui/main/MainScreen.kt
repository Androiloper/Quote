// ui/main/MainScreen.kt
package com.example.quotex.ui.main

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.quotex.R
import com.example.quotex.ui.components.*
import com.example.quotex.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    onSettingsClick: () -> Unit,
    onPromisesClick: () -> Unit,
    showSnackbar: (String) -> Unit
) {
    val proverbs by viewModel.proverbsForToday.observeAsState(emptyList())
    val scrollState = rememberScrollState()

    // Background stars effect
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpace)
            .drawBehind {
                // Create a gradient background with stars - same as before
                // ...
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
                        // Settings button now navigates to the dedicated settings screen
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
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Quote Display takes center stage
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
                        // Quote Pager - enlarged for better visibility
                        val pagerState = rememberPagerState(pageCount = { proverbs.size })

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxWidth()
                            ) { page ->
                                FuturisticQuoteCard(
                                    quote = proverbs[page],
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Add pager indicators
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                repeat(proverbs.size) { index ->
                                    val isSelected = pagerState.currentPage == index
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .size(
                                                width = if (isSelected) 24.dp else 8.dp,
                                                height = 8.dp
                                            )
                                            .background(
                                                color = if (isSelected) NebulaPurple else StarWhite.copy(
                                                    alpha = 0.3f
                                                ),
                                                shape = MaterialTheme.shapes.small
                                            )
                                    )
                                }
                            }
                        }
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
                            onClick = { /* Refresh today's quote */ },
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