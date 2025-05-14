package com.example.quotex.ui.promises

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.quotex.R
import com.example.quotex.model.Promise
import com.example.quotex.ui.components.GlassCard
import com.example.quotex.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromiseDetailScreen(
    viewModel: PromisesViewModel,
    title: String,
    onBackClick: () -> Unit
) {
    val promises by viewModel.promises.observeAsState(emptyList())
    var showAddPromiseDialog by remember { mutableStateOf(false) }

    // Filter promises by the selected title
    val filteredPromises = promises.filter { it.title == title }

    // Create cosmic background - the same as in PromiseTitlesScreen
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
                        contentDescription = "Add Promise to $title"
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (filteredPromises.isEmpty()) {
                    // Empty state
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No promises in '$title' yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = StarWhite,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Tap the + button to add one",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StarWhite.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    // List of promises for this title
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        item {
                            Text(
                                text = "${filteredPromises.size} ${if (filteredPromises.size == 1) "promise" else "promises"} in this category",
                                style = MaterialTheme.typography.bodyLarge,
                                color = StarWhite.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                            )
                        }

                        items(filteredPromises) { promise ->
                            PromiseItemDetail(
                                promise = promise,
                                onEditClick = {
                                    viewModel.setEditingPromise(promise)
                                    showAddPromiseDialog = true
                                },
                                onDeleteClick = { viewModel.deletePromise(promise) }
                            )
                        }

                        // Add some bottom spacing
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }

        // Add/Edit Promise Dialog
        if (showAddPromiseDialog) {
            AddPromiseDialog(
                categoryTitle = title,
                promise = viewModel.editingPromise.value,
                onDismiss = {
                    showAddPromiseDialog = false
                    viewModel.clearEditingPromise()
                },
                onSave = { verse, reference ->
                    val existingPromise = viewModel.editingPromise.value
                    if (existingPromise != null) {
                        // Update existing promise
                        viewModel.updatePromise(
                            existingPromise.copy(
                                verse = verse,
                                reference = reference
                            )
                        )
                    } else {
                        // Add new promise
                        viewModel.addPromise(title, verse, reference)
                    }
                    showAddPromiseDialog = false
                    viewModel.clearEditingPromise()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPromiseDialog(
    categoryTitle: String,
    promise: Promise? = null,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    // If promise is not null, we're editing an existing promise
    val isEditing = promise != null

    var verse by remember { mutableStateOf(promise?.verse ?: "") }
    var reference by remember { mutableStateOf(promise?.reference ?: "") }

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
                    text = if (isEditing) "EDIT PROMISE" else "ADD PROMISE TO '$categoryTitle'",
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (isEditing) CyberBlue else ElectricGreen
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
                        Text(if (isEditing) "UPDATE" else "SAVE")
                    }
                }
            }
        }
    }
}

@Composable
fun PromiseItemDetail(
    promise: Promise,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "\"${promise.verse}\"",
                style = MaterialTheme.typography.bodyLarge,
                color = StarWhite
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = promise.reference,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = CyberBlue
                )

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

                    IconButton(
                        onClick = { showDeleteConfirm = true },
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

            // Delete confirmation
            AnimatedVisibility(visible = showDeleteConfirm) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { showDeleteConfirm = false },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = StarWhite
                        )
                    ) {
                        Text("CANCEL")
                    }

                    TextButton(
                        onClick = {
                            onDeleteClick()
                            showDeleteConfirm = false
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = NeonPink
                        )
                    ) {
                        Text("DELETE")
                    }
                }
            }
        }
    }
}