package com.example.quotex.ui.main

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import com.example.quotex.data.repository.PromisesRepository // For CATEGORY_SEPARATOR
import com.example.quotex.model.Promise
import com.example.quotex.model.Quote
import com.example.quotex.ui.components.ClickableQuotePager
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
    onPromisesClick: () -> Unit, // This navigates to the CategoryListScreen
    onQuoteClick: (Int) -> Unit, // For navigating to a specific chapter
    showSnackbar: (String) -> Unit // For showing messages
) {
    val proverbs by viewModel.proverbsForToday.observeAsState(emptyList())
    // displayMode and displayPromises are observed for other logic, not directly used in this snippet for brevity
    // val displayMode by viewModel.displayMode.observeAsState(0)
    // val displayPromises by viewModel.displayPromises.observeAsState(false)
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        Log.d("MainScreen", "Screen composing: Proverbs count = ${proverbs.size}")
        // You might want to trigger a refresh or check data status here
        // viewModel.refreshQuotes() // If needed on initial load
        // viewModel.refreshPromises() // If needed
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpace)
            .drawBehind {
                drawRect(brush = Brush.verticalGradient(colors = listOf(CosmicBlack, DeepSpace)))
                for (i in 0..100) {
                    val x = (Math.random() * size.width).toFloat()
                    val y = (Math.random() * size.height).toFloat()
                    val radius = (Math.random() * 2f + 0.5f).toFloat()
                    val alphaValue = (Math.random() * 0.8f + 0.2f).toFloat()
                    drawCircle(color = StarWhite.copy(alpha = alphaValue), radius = radius, center = Offset(x, y))
                }
                for (i in 0..15) {
                    val x = (Math.random() * size.width).toFloat()
                    val y = (Math.random() * size.height).toFloat()
                    val radius = (Math.random() * 3f + 1.5f).toFloat()
                    drawCircle(color = StarWhite, radius = radius, center = Offset(x, y))
                }
                for (i in 0..5) {
                    val x = (Math.random() * size.width).toFloat()
                    val y = (Math.random() * size.height).toFloat()
                    val radius = (Math.random() * 3f + 1f).toFloat()
                    val colors = listOf(CyberBlue, NeonPink, ElectricGreen, NebulaPurple)
                    val color = colors[(Math.random() * colors.size).toInt()]
                    drawCircle(color = color.copy(alpha = 0.7f), radius = radius, center = Offset(x, y))
                }
            }
    ) {
        Image(
            painter = painterResource(id = R.drawable.nebula_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().alpha(0.3f).blur(20.dp)
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
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = GlassSurface.copy(alpha = 0.5f),
                        titleContentColor = StarWhite
                    ),
                    actions = {
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Rounded.Settings, "Settings", tint = StarWhite)
                        }
                        IconButton(onClick = { /* TODO: About action */ }) {
                            Icon(Icons.Rounded.Info, "About", tint = StarWhite)
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { onPromisesClick() }, // Navigates to promise management screens
                    containerColor = NebulaPurple,
                    contentColor = StarWhite
                ) {
                    Icon(Icons.Default.Add, "Manage Promises")
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
                        .height(350.dp)
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (proverbs.isEmpty()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            FuturisticLoadingIndicator(modifier = Modifier.padding(24.dp))
                            Text(
                                "Loading today's wisdom...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = StarWhite,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                            LaunchedEffect(Unit) { viewModel.refreshQuotes() }
                        }
                    } else {
                        ClickableQuotePager(
                            quotes = proverbs,
                            onQuoteClick = { chapterNumber ->
                                Log.d("MainScreen", "Quote clicked for chapter: $chapterNumber")
                                onQuoteClick(chapterNumber)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Daily Inspiration Button
                GlassCard(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("TODAY'S INSPIRATION", style = MaterialTheme.typography.titleMedium, color = ElectricGreen)
                        Text(
                            "\"A verse each day keeps the troubles away\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StarWhite,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Button(
                            onClick = { viewModel.refreshQuotes() },
                            colors = ButtonDefaults.buttonColors(containerColor = NebulaPurple),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(Icons.Rounded.Refresh, null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("REFRESH QUOTE")
                        }
                    }
                }

                // Browse Chapters Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("BROWSE CHAPTERS", style = MaterialTheme.typography.titleMedium, color = NeonPink)
                        Text(
                            "Explore all verses by chapter",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StarWhite,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (chapter in 1..5) { ChapterButton(chapter = chapter, onClick = { onQuoteClick(chapter) }) }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (chapter in 6..10) { ChapterButton(chapter = chapter, onClick = { onQuoteClick(chapter) }) }
                        }
                    }
                }

                // "VIEW PROMISES" Button (navigates to CategoryListScreen via onPromisesClick)
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
                                    colors = listOf(NeonPink.copy(alpha = 0.2f), NeonPink.copy(alpha = 0.4f), NeonPink.copy(alpha = 0.2f))
                                )
                            )
                            .padding(16.dp)
                            .clickable { onPromisesClick() }, // This should navigate to CategoryListScreen
                        contentAlignment = Alignment.Center
                    ) {
                        Text("VIEW MY PROMISES", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = StarWhite)
                    }
                }


                // Promises Section (Pager for displaying promises from MainViewModel)
                SectionHeader(
                    title = "MY PROMISES HIGHLIGHTS", // Changed title for clarity
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)
                )

                GlassCard(
                    modifier = Modifier.fillMaxWidth().height(350.dp).padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        val promises by viewModel.promises.observeAsState(emptyList())

                        if (promises.isEmpty()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                FuturisticLoadingIndicator(modifier = Modifier.padding(bottom = 16.dp))
                                Text("No promises saved yet.", style = MaterialTheme.typography.bodyLarge, color = StarWhite, textAlign = TextAlign.Center)
                                Button(
                                    onClick = onPromisesClick, // Navigate to add/manage promises
                                    colors = ButtonDefaults.buttonColors(containerColor = NebulaPurple),
                                    modifier = Modifier.padding(top = 16.dp)
                                ) {
                                    Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("ADD/VIEW PROMISES")
                                }
                            }
                        } else {
                            Log.d("MainScreen", "Displaying ${promises.size} promises in pager from MainViewModel")
                            PromisesPager( // Uses the corrected PromisesPager and PromiseCard
                                promises = promises,
                                onEditPromise = { promiseToEdit ->
                                    Log.d("MainScreen", "Edit promise requested: ${promiseToEdit.title}")
                                    // Navigation to edit screen should be handled by onPromisesClick
                                    // and then navigating within the promise management flow.
                                    // This FAB is more for a quick view or add.
                                    showSnackbar("Edit '${promiseToEdit.title.substringAfter(PromisesRepository.CATEGORY_SEPARATOR)}' via 'VIEW MY PROMISES'")
                                    onPromisesClick()
                                },
                                onDeletePromise = { promiseToDelete ->
                                    Log.d("MainScreen", "Delete promise requested: ${promiseToDelete.title}")
                                    // Deletion should happen in the dedicated promise management screens.
                                    // Direct deletion from MainScreen might be complex if it needs ViewModel interaction.
                                    // For now, viewModel.deletePromise(promiseToDelete) // Assuming MainViewModel has this
                                    showSnackbar("Delete '${promiseToDelete.title.substringAfter(PromisesRepository.CATEGORY_SEPARATOR)}' via 'VIEW MY PROMISES'")
                                    onPromisesClick()
                                }
                            )
                        }
                    }
                }

                Text(
                    "QuoteX v1.0 | Daily Cosmic Wisdom",
                    style = MaterialTheme.typography.bodySmall,
                    color = StarWhite.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
        }
    }
}

@Composable
fun ChapterButton(chapter: Int, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = NebulaPurple.copy(alpha = 0.7f), contentColor = StarWhite),
        modifier = Modifier.size(48.dp),
        contentPadding = PaddingValues(0.dp),
        shape = CircleShape
    ) {
        Text("$chapter", style = MaterialTheme.typography.titleMedium)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PromisesPager(
    promises: List<Promise>,
    modifier: Modifier = Modifier,
    initialPage: Int = 0,
    onEditPromise: (Promise) -> Unit,
    onDeletePromise: (Promise) -> Unit
) {
    if (promises.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FuturisticLoadingIndicator(modifier = Modifier.padding(bottom = 16.dp))
                Text("No promises to display.", textAlign = TextAlign.Center, color = StarWhite)
            }
        }
        return
    }

    val pagerState = rememberPagerState(initialPage = initialPage) { promises.size }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = modifier.fillMaxWidth().weight(1f)
        ) { page ->
            if (page < promises.size) {
                PromiseCard( // This is the corrected PromiseCard
                    promise = promises[page],
                    onEditClick = { onEditPromise(promises[page]) },
                    onDeleteClick = { onDeletePromise(promises[page]) },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No more promises.", textAlign = TextAlign.Center, color = StarWhite)
                }
            }
        }

        if (promises.size > 1) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(promises.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) ElectricGreen else StarWhite.copy(alpha = 0.3f)
                    Box(Modifier.padding(2.dp).clip(CircleShape).background(color).size(if (pagerState.currentPage == iteration) 10.dp else 8.dp))
                }
            }
        }
    }
}

@Composable
fun PromiseCard(
    promise: Promise,
    modifier: Modifier = Modifier,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = GlassSurface.copy(alpha = 0.8f), contentColor = StarWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val actualPromiseTitle = promise.title.substringAfter(PromisesRepository.CATEGORY_SEPARATOR, promise.title)
            Text(
                text = actualPromiseTitle,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp),
                color = ElectricGreen
            )

            Text(
                text = "\"${promise.verse}\"",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp),
                color = StarWhite
            )

            val refParts = promise.reference.split(PromisesRepository.TITLE_SEPARATOR)
            val titleNameFromRef = if (refParts.isNotEmpty()) refParts[0] else ""
            val subtitleNameFromRef = if (refParts.size > 1) refParts[1] else ""
            val scriptureRefDisplay = if (refParts.size > 2) refParts[2].trim() else "N/A"

            val displayReferenceParts = mutableListOf<String>()
            if(titleNameFromRef.isNotBlank() && titleNameFromRef.lowercase() != "general") displayReferenceParts.add(titleNameFromRef)
            if(subtitleNameFromRef.isNotBlank() && subtitleNameFromRef.lowercase() != "general") displayReferenceParts.add(subtitleNameFromRef)
            if(scriptureRefDisplay.isNotBlank() && scriptureRefDisplay.lowercase() != "n/a") displayReferenceParts.add(scriptureRefDisplay)
            val finalDisplayReference = displayReferenceParts.joinToString(" > ").ifEmpty { "Details in App" }


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = finalDisplayReference,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = CyberBlue,
                    modifier = Modifier
                        .background(color = GlassSurfaceDark.copy(alpha = 0.5f), shape = MaterialTheme.shapes.small)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .weight(1f, fill = false), // Ensure text doesn't push buttons off
                    maxLines = 2,
                )

                Row {
                    IconButton(onClick = onEditClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, "Edit", tint = StarWhite.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, "Delete", tint = NeonPink.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}