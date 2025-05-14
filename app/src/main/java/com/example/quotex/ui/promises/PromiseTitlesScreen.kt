package com.example.quotex.ui.promises

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
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
fun PromiseTitlesScreen(
    viewModel: PromisesViewModel,
    onBackClick: () -> Unit,
    onTitleClick: (String) -> Unit
) {
    val promises by viewModel.promises.observeAsState(emptyList())
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    // Create the cosmic background
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

        // Extract unique titles from promises
        val uniqueTitles = promises.map { it.title }.distinct().sorted()

        // Group promises by title for counting
        val promisesByTitle = promises.groupBy { it.title }

        Scaffold(
            containerColor = Color.Transparent,
            contentColor = StarWhite,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "PROMISE CATEGORIES",
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
                    onClick = { showAddCategoryDialog = true },
                    containerColor = NebulaPurple,
                    contentColor = StarWhite
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add New Category"
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (uniqueTitles.isEmpty()) {
                    // Empty state
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FuturisticLoadingIndicator(
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Text(
                            text = "No promise categories yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = StarWhite,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Tap the + button to add a category",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StarWhite.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )

                        Button(
                            onClick = { viewModel.addSamplePromises() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NebulaPurple
                            ),
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("ADD SAMPLE CATEGORIES")
                        }
                    }
                } else {
                    // List of titles with promise counts
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        item {
                            Text(
                                text = "Select a category to view promises",
                                style = MaterialTheme.typography.bodyLarge,
                                color = StarWhite.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                            )
                        }

                        items(uniqueTitles) { title ->
                            PromiseTitleCard(
                                title = title,
                                count = promisesByTitle[title]?.size ?: 0,
                                onClick = { onTitleClick(title) }
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

        // Add Category Dialog
        if (showAddCategoryDialog) {
            AddCategoryDialog(
                onDismiss = { showAddCategoryDialog = false },
                onCategoryAdded = { newCategory ->
                    showAddCategoryDialog = false
                    // When a new category is added, immediately navigate to it
                    viewModel.createNewCategory(newCategory)
                    onTitleClick(newCategory)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onCategoryAdded: (String) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }

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
                    text = "CREATE NEW CATEGORY",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = ElectricGreen
                )

                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Category Name") },
                    placeholder = { Text("Enter a name for your category") },
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
                            if (categoryName.isNotBlank()) {
                                onCategoryAdded(categoryName.trim())
                            }
                        },
                        enabled = categoryName.isNotBlank(),
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

@Composable
fun PromiseTitleCard(
    title: String,
    count: Int,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = ElectricGreen,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "$count ${if (count == 1) "promise" else "promises"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = StarWhite.copy(alpha = 0.7f)
                )
            }

            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "View promises",
                tint = StarWhite.copy(alpha = 0.7f)
            )
        }
    }
}