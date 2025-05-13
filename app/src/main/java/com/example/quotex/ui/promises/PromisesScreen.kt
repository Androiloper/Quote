package com.example.quotex.ui.promises

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.quotex.R
import com.example.quotex.model.Promise
import com.example.quotex.ui.components.FuturisticLoadingIndicator
import com.example.quotex.ui.components.GlassCard
import com.example.quotex.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromisesScreen(
    viewModel: PromisesViewModel,
    onBackClick: () -> Unit
) {
    val promises by viewModel.promises.observeAsState(emptyList())
    val editingPromise by viewModel.editingPromise.observeAsState()

    var showAddPromiseDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Debug logging
    LaunchedEffect(Unit) {
        Log.d("PromisesScreen", "Screen composing: Promises count = ${promises.size}")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpace)
            .drawBehind {
                // Create a gradient background with stars
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            DeepSpace,
                            DeepSpace.copy(blue = 0.15f)
                        )
                    )
                )

                // Small cosmic particles
                for (i in 0..80) {
                    val x = (Math.random() * size.width).toFloat()
                    val y = (Math.random() * size.height).toFloat()
                    val radius = (Math.random() * 1.5f + 0.5f).toFloat()
                    val alpha = (Math.random() * 0.6f + 0.2f).toFloat()

                    drawCircle(
                        color = StarWhite.copy(alpha = alpha),
                        radius = radius,
                        center = Offset(x, y)
                    )
                }
            }
    ) {
        // Abstract nebula effect
        Image(
            painter = painterResource(id = R.drawable.nebula_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.15f)
                .blur(30.dp)
        )

        Scaffold(
            containerColor = Color.Transparent,
            contentColor = StarWhite,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "COSMIC PROMISES",
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
                    ),
                    actions = {
                        // Add a debug action to add sample promises
                        IconButton(onClick = { viewModel.addSamplePromises() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Add Samples",
                                tint = StarWhite
                            )
                        }
                        IconButton(onClick = { /* Search */ }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = StarWhite
                            )
                        }
                    }
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
            // Search box
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        viewModel.setSearchQuery(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search promises") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                viewModel.setSearchQuery("")
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberBlue,
                        unfocusedBorderColor = StarWhite.copy(alpha = 0.5f),
                        focusedTextColor = StarWhite,
                        unfocusedTextColor = StarWhite,
                        cursorColor = CyberBlue,
                        focusedContainerColor = GlassSurface.copy(alpha = 0.3f),
                        unfocusedContainerColor = GlassSurface.copy(alpha = 0.2f)
                    ),
                    shape = MaterialTheme.shapes.medium,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        viewModel.setSearchQuery(searchQuery)
                    }),
                    singleLine = true
                )

                if (promises.isEmpty()) {
                    // Empty state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (searchQuery.isNotEmpty()) {
                                Text(
                                    text = "No matching promises found",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = StarWhite,
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                FuturisticLoadingIndicator()
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = "Tap the + button to add your first promise",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = StarWhite,
                                    textAlign = TextAlign.Center
                                )

                                // Debug button to add sample promises
                                Button(
                                    onClick = { viewModel.addSamplePromises() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ElectricGreen
                                    ),
                                    modifier = Modifier.padding(top = 16.dp)
                                ) {
                                    Text("ADD SAMPLE PROMISES")
                                }
                            }
                        }
                    }
                } else {
                    // List of promises
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        itemsIndexed(promises) { index, promise ->
                            // Use animatedVisibilityScope for item animations
                            val visibleState = remember { MutableTransitionState(false) }

                            LaunchedEffect(Unit) {
                                visibleState.targetState = true
                            }

                            AnimatedVisibility(
                                visibleState = visibleState,
                                enter = fadeIn(
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        delayMillis = index * 50
                                    )
                                ) + slideInVertically(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    ),
                                    initialOffsetY = { 100 }
                                ),
                                exit = fadeOut() + slideOutVertically()
                            ) {
                                PromiseItem(
                                    promise = promise,
                                    onEditClick = { viewModel.setEditingPromise(promise) },
                                    onDeleteClick = { viewModel.deletePromise(promise) },
                                    onShareClick = { /* Share promise */ },
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add/Edit promise dialog
        if (showAddPromiseDialog || editingPromise != null) {
            PromiseDialog(
                promise = editingPromise,
                onDismiss = {
                    showAddPromiseDialog = false
                    viewModel.setEditingPromise(null)
                },
                onSave = { title, verse, reference ->
                    if (editingPromise != null) {
                        viewModel.updatePromise(
                            editingPromise!!.copy(
                                title = title,
                                verse = verse,
                                reference = reference
                            )
                        )
                    } else {
                        viewModel.addPromise(title, verse, reference)
                    }
                    showAddPromiseDialog = false
                }
            )
        }
    }
}

@Composable
fun PromiseItem(
    promise: Promise,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = promise.title,
                style = MaterialTheme.typography.titleLarge,
                color = ElectricGreen
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "\"${promise.verse}\"",
                style = MaterialTheme.typography.bodyLarge,
                color = StarWhite
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        onClick = onShareClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = StarWhite.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromiseDialog(
    promise: Promise?,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    val isEditing = promise != null

    // State for form fields
    var title by remember { mutableStateOf(promise?.title ?: "") }
    var verse by remember { mutableStateOf(promise?.verse ?: "") }
    var reference by remember { mutableStateOf(promise?.reference ?: "") }

    // Focus requesters
    val titleFocus = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        titleFocus.requestFocus()
    }

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
                    text = if (isEditing) "EDIT PROMISE" else "NEW PROMISE",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isEditing) CyberBlue else ElectricGreen
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(titleFocus),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricGreen,
                        unfocusedBorderColor = StarWhite.copy(alpha = 0.5f),
                        focusedTextColor = StarWhite,
                        unfocusedTextColor = StarWhite,
                        cursorColor = ElectricGreen,
                        focusedLabelColor = ElectricGreen,
                        unfocusedLabelColor = StarWhite.copy(alpha = 0.7f)
                    )
                )

                OutlinedTextField(
                    value = verse,
                    onValueChange = { verse = it },
                    label = { Text("Verse") },
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
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricGreen,
                        unfocusedBorderColor = StarWhite.copy(alpha = 0.5f),
                        focusedTextColor = StarWhite,
                        unfocusedTextColor = StarWhite,
                        cursorColor = ElectricGreen,
                        focusedLabelColor = ElectricGreen,
                        unfocusedLabelColor = StarWhite.copy(alpha = 0.7f)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray.copy(alpha = 0.5f)
                        )
                    ) {
                        Text("CANCEL")
                    }

                    Button(
                        onClick = { onSave(title, verse, reference) },
                        enabled = title.isNotBlank() && verse.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NebulaPurple
                        )
                    ) {
                        Text(if (isEditing) "UPDATE" else "SAVE")
                    }
                }
            }
        }
    }
}