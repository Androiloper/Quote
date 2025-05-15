// app/src/main/java/com/example/quotex/ui/categories/SubtitleListScreen.kt
// Updated to use real data from repository

package com.example.quotex.ui.categories

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.quotex.R
import com.example.quotex.model.Promise
import com.example.quotex.model.Subtitle
import com.example.quotex.ui.components.FuturisticLoadingIndicator
import com.example.quotex.ui.components.GlassCard
import com.example.quotex.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubtitleListScreen(
    viewModel: CategoryViewModel = hiltViewModel(),
    category: String,
    title: String,
    onBackClick: () -> Unit
) {
    var showAddSubtitleDialog by remember { mutableStateOf(false) }
    var showAddPromiseDialog by remember { mutableStateOf(false) }

    // Collect data from ViewModel
    val subtitles by viewModel.subtitles.collectAsState()
    val promises by viewModel.promises.collectAsState()
    val selectedSubtitle by viewModel.selectedSubtitle.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Load data when the screen is first composed
    LaunchedEffect(title) {
        viewModel.loadSubtitlesByTitle(title)
    }

    // Default to the first subtitle if none is selected
    LaunchedEffect(subtitles) {
        if (subtitles.isNotEmpty() && selectedSubtitle == null) {
            viewModel.selectSubtitle(subtitles.first().id)
        }
    }

    // Create cosmic background
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
            containerColor = Color.Transparent,
            contentColor = StarWhite,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            title,
                            style = MaterialTheme.typography.headlineLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
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
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddPromiseDialog = true },
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
                    .padding(horizontal = 16.dp)
            ) {
                // Subtitle section with title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Sub TITLE",
                        style = MaterialTheme.typography.titleLarge,
                        color = StarWhite
                    )

                    // Add subtitle button
                    IconButton(
                        onClick = { showAddSubtitleDialog = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Subtitle",
                            tint = StarWhite
                        )
                    }
                }

                if (isLoading) {
                    // Show loading indicator for subtitles
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FuturisticLoadingIndicator()
                    }
                } else if (subtitles.isEmpty()) {
                    // Show empty state for subtitles
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No subtitles found for this title",
                            style = MaterialTheme.typography.bodyLarge,
                            color = StarWhite.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Subtitle list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    ) {
                        items(subtitles) { subtitle ->
                            SubtitleItem(
                                subtitle = subtitle.name,
                                isSelected = selectedSubtitle?.id == subtitle.id,
                                onClick = { viewModel.selectSubtitle(subtitle.id) }
                            )
                        }
                    }
                }

                // Pagination indicators if we have multiple subtitles
                if (subtitles.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        subtitles.forEachIndexed { index, subtitle ->
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .padding(horizontal = 4.dp)
                                    .background(
                                        if (selectedSubtitle?.id == subtitle.id) StarWhite
                                        else StarWhite.copy(alpha = 0.3f),
                                        CircleShape
                                    )
                            )
                        }
                    }
                }

                // Promises section with title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "All PROMISES",
                        style = MaterialTheme.typography.titleLarge,
                        color = StarWhite,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Show promises for selected subtitle
                if (selectedSubtitle == null) {
                    // No subtitle selected yet
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (subtitles.isEmpty())
                                "Create a subtitle first"
                            else
                                "Select a subtitle to see promises",
                            style = MaterialTheme.typography.bodyLarge,
                            color = StarWhite.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else if (isLoading) {
                    // Loading promises
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        FuturisticLoadingIndicator()
                    }
                } else if (promises.isEmpty()) {
                    // No promises yet
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No promises for this subtitle",
                                style = MaterialTheme.typography.bodyLarge,
                                color = StarWhite.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )

                            Button(
                                onClick = { showAddPromiseDialog = true },
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
                                Text("Add First Promise")
                            }
                        }
                    }
                } else {
                    // Promises list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(promises) { promise ->
                            PromiseItem(promise = promise)
                        }
                    }
                }
            }
        }

        // Add Subtitle Dialog
        if (showAddSubtitleDialog) {
            AddSubtitleDialog(
                onDismiss = { showAddSubtitleDialog = false },
                onSave = { subtitleName ->
                    // Create new subtitle for this title
                    viewModel.createSubtitle(subtitleName, title)
                    showAddSubtitleDialog = false
                }
            )
        }

        // Add Promise Dialog
        if (showAddPromiseDialog) {
            if (selectedSubtitle != null) {
                AddPromiseDialog(
                    subtitle = selectedSubtitle?.name ?: "Default",
                    onDismiss = { showAddPromiseDialog = false },
                    onSave = { verse, reference ->
                        // Create new promise for this subtitle
                        viewModel.createPromise(
                            subtitleId = selectedSubtitle?.id ?: 0,
                            title = title,
                            verse = verse,
                            reference = reference
                        )
                        showAddPromiseDialog = false
                    }
                )
            } else if (subtitles.isNotEmpty()) {
                // If no subtitle is selected but we have subtitles,
                // select the first one and then show the dialog
                viewModel.selectSubtitle(subtitles.first().id)
                // The dialog will show once the selectedSubtitle is updated
            } else {
                // No subtitles exist, show create subtitle dialog instead
                showAddSubtitleDialog = true
                showAddPromiseDialog = false
            }
        }
    }
}

@Composable
fun SubtitleItem(
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) NebulaPurple.copy(alpha = 0.3f) else GlassSurface.copy(alpha = 0.2f),
            contentColor = StarWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp),
        border = if (isSelected) {
            BorderStroke(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(CyberBlue, NeonPink)
                )
            )
        } else null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) StarWhite else StarWhite.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun PromiseItem(
    promise: Promise
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(GlassSurface.copy(alpha = 0.2f))
                .padding(16.dp)
        ) {
            // Promise title
            Text(
                text = promise.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = ElectricGreen,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Promise verse
            Text(
                text = "\"${promise.verse}\"",
                style = MaterialTheme.typography.bodyLarge,
                color = StarWhite,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Promise reference
            Text(
                text = promise.reference,
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = CyberBlue,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubtitleDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var subtitleName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = GlassSurface,
                contentColor = StarWhite
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "CREATE NEW SUBTITLE",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = ElectricGreen
                )

                OutlinedTextField(
                    value = subtitleName,
                    onValueChange = { subtitleName = it },
                    label = { Text("Subtitle Name") },
                    placeholder = { Text("Enter a name for the subtitle") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricGreen,
                        unfocusedBorderColor = StarWhite.copy(alpha = 0.5f),
                        focusedTextColor = StarWhite,
                        unfocusedTextColor = StarWhite,
                        cursorColor = ElectricGreen,
                        focusedLabelColor = ElectricGreen,
                        unfocusedLabelColor = StarWhite.copy(alpha = 0.7f)
                    ),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = 8.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = StarWhite.copy(alpha = 0.7f)
                        )
                    ) {
                        Text("CANCEL")
                    }

                    Button(
                        onClick = {
                            if (subtitleName.isNotBlank()) {
                                onSave(subtitleName.trim())
                            }
                        },
                        enabled = subtitleName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NebulaPurple,
                            disabledContainerColor = NebulaPurple.copy(alpha = 0.5f)
                        )
                    ) {
                        Text("CREATE")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPromiseDialog(
    subtitle: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var verse by remember { mutableStateOf("") }
    var reference by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = GlassSurface,
                contentColor = StarWhite
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "ADD PROMISE TO '$subtitle'",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = ElectricGreen
                )

                OutlinedTextField(
                    value = verse,
                    onValueChange = { verse = it },
                    label = { Text("Promise Verse") },
                    placeholder = { Text("Enter the verse text") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricGreen,
                        unfocusedBorderColor = StarWhite.copy(alpha = 0.5f),
                        focusedTextColor = StarWhite,
                        unfocusedTextColor = StarWhite,
                        cursorColor = ElectricGreen,
                        focusedLabelColor = ElectricGreen,
                        unfocusedLabelColor = StarWhite.copy(alpha = 0.7f)
                    ),
                    minLines = 3
                )

                OutlinedTextField(
                    value = reference,
                    onValueChange = { reference = it },
                    label = { Text("Reference") },
                    placeholder = { Text("E.g., John 3:16") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricGreen,
                        unfocusedBorderColor = StarWhite.copy(alpha = 0.5f),
                        focusedTextColor = StarWhite,
                        unfocusedTextColor = StarWhite,
                        cursorColor = ElectricGreen,
                        focusedLabelColor = ElectricGreen,
                        unfocusedLabelColor = StarWhite.copy(alpha = 0.7f)
                    ),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = 8.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = StarWhite.copy(alpha = 0.7f)
                        )
                    ) {
                        Text("CANCEL")
                    }

                    Button(
                        onClick = {
                            if (verse.isNotBlank()) {
                                onSave(verse.trim(), reference.trim())
                            }
                        },
                        enabled = verse.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NebulaPurple,
                            disabledContainerColor = NebulaPurple.copy(alpha = 0.5f)
                        )
                    ) {
                        Text("SAVE")
                    }
                }
            }
        }
    }
}