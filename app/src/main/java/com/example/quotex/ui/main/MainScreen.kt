package com.example.quotex.ui.main

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.quotex.R
import com.example.quotex.model.Promise
import com.example.quotex.model.Quote
import com.example.quotex.ui.components.FuturisticLoadingIndicator
import com.example.quotex.ui.components.GlassCard
import com.example.quotex.ui.components.SectionHeader
import com.example.quotex.ui.theme.CosmicBlack
import com.example.quotex.ui.theme.CyberBlue
import com.example.quotex.ui.theme.DeepSpace
import com.example.quotex.ui.theme.ElectricGreen
import com.example.quotex.ui.theme.GlassSurface
import com.example.quotex.ui.theme.GlassSurfaceDark
import com.example.quotex.ui.theme.NebulaPurple
import com.example.quotex.ui.theme.NeonPink
import com.example.quotex.ui.theme.StarWhite

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
                // Add this FAB
                FloatingActionButton(
                    onClick = { onPromisesClick() },  // Navigate to Promises screen on click
                    containerColor = NebulaPurple,
                    contentColor = StarWhite
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
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
                // Quote Display Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp) // Fixed height to ensure visibility
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (proverbs.isEmpty()) {
                        // Show loading
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            FuturisticLoadingIndicator(
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
                        // Quote Pager
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val pagerState = rememberPagerState(initialPage = 0) { proverbs.size }

                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(280.dp) // Explicit height
                            ) { page ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Use basic card for reliability
                                    QuoteCard(
                                        quote = proverbs[page],
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            // Pager indicators
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
                                // Add logging
                                Log.d("MainScreen", "Promises button clicked, navigating with displayPromises=$displayPromises")
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

                // Promises Section - Add this after the "Promises Button" section
                SectionHeader(
                    title = "MY PROMISES",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                )

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        val promises by viewModel.promises.observeAsState(emptyList())

                        if (promises.isEmpty()) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                FuturisticLoadingIndicator(
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                Text(
                                    text = "No promises saved yet",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = StarWhite,
                                    textAlign = TextAlign.Center
                                )

                                Button(
                                    onClick = onPromisesClick,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NebulaPurple
                                    ),
                                    modifier = Modifier.padding(top = 16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("ADD PROMISES")
                                }
                            }
                        } else {
                            Log.d("MainScreen", "Showing ${promises.size} promises in pager")
                            PromisesPager(
                                promises = promises,
                                onEditPromise = { /* Navigate to edit screen */
                                    Log.d("MainScreen", "Edit promise requested: ${it.title}")
                                    onPromisesClick()
                                },
                                onDeletePromise = { /* Handle delete action */
                                    Log.d("MainScreen", "Delete promise requested: ${it.title}")
                                    // Handle delete through your viewModel
                                }
                            )
                        }
                    }
                }

                // Debug status display - add this just before "App Info"
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "DEBUG STATUS",
                            style = MaterialTheme.typography.titleSmall,
                            color = NeonPink
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Promises Enabled: ${if (displayPromises) "YES" else "NO"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (displayPromises) ElectricGreen else NeonPink
                        )

                        Text(
                            text = "Promises Count: ${viewModel.promises.observeAsState(emptyList()).value.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = StarWhite
                        )

                        Button(
                            onClick = {
                                viewModel.forceInitializePromises()
                                onPromisesClick()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricGreen
                            ),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("FORCE INITIALIZE PROMISES")
                        }
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

// Simple QuoteCard implementation that guarantees visibility
@Composable
fun QuoteCard(
    quote: Quote,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = GlassSurface.copy(alpha = 0.8f),
            contentColor = StarWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "\"${quote.text}\"",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp),
                color = StarWhite
            )

            Text(
                text = quote.reference,
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = CyberBlue,
                modifier = Modifier
                    .align(Alignment.End)
                    .background(
                        color = GlassSurfaceDark.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

// PromiseCard implementation to display promises - moved to top level
@Composable
fun PromiseCard(
    promise: Promise,
    modifier: Modifier = Modifier,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = GlassSurface.copy(alpha = 0.8f),
            contentColor = StarWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title section
            Text(
                text = promise.title,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp),
                color = ElectricGreen
            )

            // Verse section
            Text(
                text = "\"${promise.verse}\"",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp),
                color = StarWhite
            )

            // Reference and actions section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reference
                Text(
                    text = promise.reference,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = CyberBlue,
                    modifier = Modifier
                        .background(
                            color = GlassSurfaceDark.copy(alpha = 0.5f),
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )

                // Action buttons
                Row {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = StarWhite.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = NeonPink.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// PromisesPager implementation - moved to top level
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PromisesPager(
    promises: List<Promise>,
    modifier: Modifier = Modifier,
    initialPage: Int = 0,
    onEditPromise: (Promise) -> Unit = {},
    onDeletePromise: (Promise) -> Unit = {}
) {
    if (promises.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FuturisticLoadingIndicator(
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "No promises saved yet",
                    textAlign = TextAlign.Center,
                    color = StarWhite
                )
            }
        }
        return
    }

    val pagerState = rememberPagerState(initialPage = initialPage) { promises.size }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            if (page < promises.size) {
                PromiseCard(
                    promise = promises[page],
                    onEditClick = { onEditPromise(promises[page]) },
                    onDeleteClick = { onDeletePromise(promises[page]) },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No more promises available",
                        textAlign = TextAlign.Center,
                        color = StarWhite
                    )
                }
            }
        }

        // Add page indicator dots
        if (promises.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(promises.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) ElectricGreen else StarWhite.copy(alpha = 0.3f)
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(if (pagerState.currentPage == iteration) 10.dp else 8.dp)
                    )
                }
            }
        }
    }
}