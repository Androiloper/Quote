// app/src/main/java/com/example/quotex/ui/categories/TitleListScreen.kt

package com.example.quotex.ui.categories

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.quotex.R
import com.example.quotex.model.Title
import com.example.quotex.ui.components.FuturisticLoadingIndicator
import com.example.quotex.ui.components.GlassCard
import com.example.quotex.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitleListScreen(
    viewModel: CategoryViewModel = hiltViewModel(),
    category: String,
    onBackClick: () -> Unit,
    onTitleClick: (String) -> Unit
) {
    var showAddTitleDialog by remember { mutableStateOf(false) }
    var showEditTitleDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var titleToEdit by remember { mutableStateOf<Title?>(null) }
    var titleToDelete by remember { mutableStateOf<Title?>(null) }

    // Get titles for the selected category
    val titles by viewModel.titles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Show error if needed
    error?.let {
        LaunchedEffect(it) {
            // You would display a snackbar here
            Log.e("TitleListScreen", "Error: $it")
            // Clear error after displaying
            viewModel.clearError()
        }
    }

    // Load titles when the screen is first composed
    LaunchedEffect(category) {
        viewModel.loadTitlesByCategory(category)
    }

    // Create cosmic background - the same as in CategoryListScreen
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
                            category,
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
                    onClick = { showAddTitleDialog = true },
                    containerColor = NebulaPurple,
                    contentColor = StarWhite
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Title"
                    )
                }
            }
        ) { paddingValues ->
            // Adding a blue border around the list area to match the design

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent
                            )
                        ),
                        shape = MaterialTheme.shapes.medium
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                CyberBlue.copy(alpha = 0.7f),
                                CyberBlue.copy(alpha = 0.4f),
                                CyberBlue.copy(alpha = 0.7f)
                            )
                        ),
                        shape = MaterialTheme.shapes.medium
                    )
            )
             {


                if (isLoading) {
                    // Show loading indicator
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        FuturisticLoadingIndicator()
                    }
                } else if (titles.isEmpty()) {
                    // Show empty state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No titles found for this category",
                                style = MaterialTheme.typography.bodyLarge,
                                color = StarWhite.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )

                            Button(
                                onClick = { showAddTitleDialog = true },
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
                                Text("Create First Title")
                            }
                        }
                    }
                } else {
                    // Show titles list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 8.dp, horizontal = 8.dp)
                    ) {
                        items(titles) { title ->
                            TitleItem(
                                title = title,
                                onClick = { onTitleClick(title.name) },
                                onEditClick = {
                                    titleToEdit = title
                                    showEditTitleDialog = true
                                },
                                onDeleteClick = {
                                    titleToDelete = title
                                    showDeleteConfirmation = true
                                }
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

        // Add Title Dialog
        if (showAddTitleDialog) {
            AddTitleDialog(
                onDismiss = { showAddTitleDialog = false },
                onSave = { titleName ->
                    // Create new title and associate with this category
                    viewModel.createTitle(titleName, category)
                    showAddTitleDialog = false
                }
            )
        }

        // Edit Title Dialog
        if (showEditTitleDialog && titleToEdit != null) {
            EditTitleDialog(
                title = titleToEdit!!,
                onDismiss = {
                    showEditTitleDialog = false
                    titleToEdit = null
                },
                onSave = { title, newName ->
                    viewModel.updateTitle(title, newName)
                    showEditTitleDialog = false
                    titleToEdit = null
                }
            )
        }

        // Delete Confirmation Dialog
        if (showDeleteConfirmation && titleToDelete != null) {
            DeleteTitleConfirmationDialog(
                titleName = titleToDelete!!.name,
                onDismiss = {
                    showDeleteConfirmation = false
                    titleToDelete = null
                },
                onConfirm = {
                    viewModel.deleteTitle(titleToDelete!!)
                    showDeleteConfirmation = false
                    titleToDelete = null
                }
            )
        }
    }
}

@Composable
fun TitleItem(
    title: Title,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            GlassSurface.copy(alpha = 0.3f),
                            GlassSurface.copy(alpha = 0.6f)
                        )
                    )
                )
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title text
            Text(
                text = title.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = StarWhite,
                modifier = Modifier.weight(1f)
            )

            // Action buttons
            Row {
                // Edit button
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .size(36.dp)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit ${title.name}",
                        tint = CyberBlue
                    )
                }

                // Delete button
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(36.dp)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete ${title.name}",
                        tint = NeonPink
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTitleDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var titleName by remember { mutableStateOf("") }

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
                    text = "CREATE NEW TITLE",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = ElectricGreen
                )

                OutlinedTextField(
                    value = titleName,
                    onValueChange = { titleName = it },
                    label = { Text("Title Name") },
                    placeholder = { Text("Enter a name for the title") },
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
                            if (titleName.isNotBlank()) {
                                onSave(titleName.trim())
                            }
                        },
                        enabled = titleName.isNotBlank(),
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
fun EditTitleDialog(
    title: Title,
    onDismiss: () -> Unit,
    onSave: (Title, String) -> Unit
) {
    var titleName by remember { mutableStateOf(title.name) }

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
                    text = "EDIT TITLE",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = CyberBlue
                )

                OutlinedTextField(
                    value = titleName,
                    onValueChange = { titleName = it },
                    label = { Text("Title Name") },
                    placeholder = { Text("Enter a name for the title") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberBlue,
                        unfocusedBorderColor = StarWhite.copy(alpha = 0.5f),
                        focusedTextColor = StarWhite,
                        unfocusedTextColor = StarWhite,
                        cursorColor = CyberBlue,
                        focusedLabelColor = CyberBlue,
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
                            if (titleName.isNotBlank()) {
                                onSave(title, titleName.trim())
                            }
                        },
                        enabled = titleName.isNotBlank() && titleName != title.name,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberBlue,
                            disabledContainerColor = CyberBlue.copy(alpha = 0.5f)
                        )
                    ) {
                        Text("SAVE")
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteTitleConfirmationDialog(
    titleName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GlassSurface,
        textContentColor = StarWhite,
        titleContentColor = NeonPink,
        title = {
            Text("Delete Title")
        },
        text = {
            Column {
                Text("Are you sure you want to delete title '$titleName'?")
                Text(
                    "This will permanently delete the title and all associated subtitles and promises.",
                    color = StarWhite.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonPink
                )
            ) {
                Text("DELETE")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = StarWhite
                )
            ) {
                Text("CANCEL")
            }
        }
    )
}