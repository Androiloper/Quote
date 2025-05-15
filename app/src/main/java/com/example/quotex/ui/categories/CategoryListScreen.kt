// app/src/main/java/com/example/quotex/ui/categories/CategoryListScreen.kt

package com.example.quotex.ui.categories

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.quotex.R
import com.example.quotex.model.Category
import com.example.quotex.model.Promise
import com.example.quotex.ui.components.FuturisticLoadingIndicator
import com.example.quotex.ui.components.GlassCard
import com.example.quotex.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    viewModel: CategoryViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onPromiseClick: (Long) -> Unit
) {
    // State
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showEditCategoryDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Recently, 1 = Favorite
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    // Collect data from ViewModel
    val categories by viewModel.categories.collectAsState()
    val recentPromises by viewModel.recentPromises.collectAsState()
    val favoritePromises by viewModel.favoritePromises.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Show error snackbar if needed
    error?.let {
        LaunchedEffect(it) {
            // You would display a snackbar here
            Log.e("CategoryListScreen", "Error: $it")
            // Clear error after displaying
            viewModel.clearError()
        }
    }

    // Load data when the screen is first composed
    LaunchedEffect(Unit) {
        viewModel.loadCategories()
        viewModel.loadRecentPromises()
        viewModel.loadFavoritePromises()
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
                            "PROMISES",
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
                    onClick = {
                        showAddCategoryDialog = true
                    },
                    containerColor = NebulaPurple,
                    contentColor = StarWhite
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Category"
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
                // Tabs for Recently and Favorite
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TabButton(
                        text = "Recently",
                        isSelected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    TabButton(
                        text = "Favorite",
                        isSelected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    )
                }

                // Promises section based on selected tab
                if (selectedTab == 0 && recentPromises.isEmpty() ||
                    selectedTab == 1 && favoritePromises.isEmpty()) {
                    // Show empty state for promises
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedTab == 0)
                                "No recent promises"
                            else
                                "No favorite promises",
                            color = StarWhite.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    // Show promises list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(vertical = 8.dp)
                    ) {
                        val promises = if (selectedTab == 0) recentPromises else favoritePromises

                        items(promises) { promise ->
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onPromiseClick(promise.id) }
                            ) {
                                Text(
                                    text = promise.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(16.dp),
                                    color = StarWhite
                                )
                            }
                        }
                    }
                }

                // CATEGORIES title
                Text(
                    text = "CATEGORIES",
                    style = MaterialTheme.typography.titleLarge,
                    color = ElectricGreen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    textAlign = TextAlign.Center
                )

                if (isLoading) {
                    // Loading state for categories
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FuturisticLoadingIndicator()
                    }
                } else if (categories.isEmpty()) {
                    // Empty state for categories
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "No categories yet",
                                color = StarWhite.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Button(
                                onClick = { showAddCategoryDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NebulaPurple
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Category")
                            }
                        }
                    }
                } else {
                    // Grid of categories
                    // Display in rows of 2
                    val chunkedCategories = categories.chunked(2)

                    Column(modifier = Modifier.fillMaxWidth()) {
                        chunkedCategories.forEach { rowCategories ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                rowCategories.forEach { category ->
                                    CategoryButton(
                                        category = category,
                                        onClick = { onCategoryClick(category.name) },
                                        onEditClick = {
                                            categoryToEdit = category
                                            showEditCategoryDialog = true
                                        },
                                        onDeleteClick = {
                                            categoryToDelete = category
                                            showDeleteConfirmation = true
                                        },
                                        isLastItem = category == categories.last()
                                    )

                                    // Add spacer if there's only one item in the row
                                    if (rowCategories.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Category Dialog
        if (showAddCategoryDialog) {
            AddCategoryDialog(
                onDismiss = { showAddCategoryDialog = false },
                onSave = { categoryName ->
                    // Handle creating new category
                    viewModel.createCategory(categoryName)
                    showAddCategoryDialog = false
                }
            )
        }

        // Edit Category Dialog
        if (showEditCategoryDialog && categoryToEdit != null) {
            EditCategoryDialog(
                category = categoryToEdit!!,
                onDismiss = {
                    showEditCategoryDialog = false
                    categoryToEdit = null
                },
                onSave = { category, newName ->
                    viewModel.updateCategory(category, newName)
                    showEditCategoryDialog = false
                    categoryToEdit = null
                }
            )
        }

        // Delete Confirmation Dialog
        if (showDeleteConfirmation && categoryToDelete != null) {
            DeleteConfirmationDialog(
                categoryName = categoryToDelete!!.name,
                onDismiss = {
                    showDeleteConfirmation = false
                    categoryToDelete = null
                },
                onConfirm = {
                    viewModel.deleteCategory(categoryToDelete!!)
                    showDeleteConfirmation = false
                    categoryToDelete = null
                }
            )
        }
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) NebulaPurple else GlassSurface
        ),
        modifier = Modifier.width(120.dp)
    ) {
        Text(text = text)
    }
}

@Composable
fun CategoryButton(
    category: Category,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isLastItem: Boolean = false
) {
    Box(
        modifier = Modifier
            .width(150.dp)
            .padding(4.dp)
    ) {
        // Main Button
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(16.dp),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = Brush.horizontalGradient(
                    colors = listOf(CyberBlue, NeonPink)
                )
            ),
            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = category.name,
                color = StarWhite,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        // Edit Button - small circular button at top right
        IconButton(
            onClick = onEditClick,
            modifier = Modifier
                .size(28.dp)
                .align(Alignment.TopEnd)
                .offset(x = 8.dp, y = (-8).dp)
                .background(NebulaPurple, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit ${category.name}",
                tint = StarWhite,
                modifier = Modifier.size(16.dp)
            )
        }

        // Delete Button - small circular button at bottom right
        IconButton(
            onClick = onDeleteClick,
            modifier = Modifier
                .size(28.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 8.dp, y = 8.dp)
                .background(NeonPink, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete ${category.name}",
                tint = StarWhite,
                modifier = Modifier.size(16.dp)
            )
        }

        // Add button at the bottom right of the last category
        if (isLastItem) {
            FloatingActionButton(
                onClick = { /* This will be handled by the main FAB */ },
                containerColor = NebulaPurple,
                contentColor = StarWhite,
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Category",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
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
                    style = MaterialTheme.typography.headlineSmall,
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
                                onSave(categoryName.trim())
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCategoryDialog(
    category: Category,
    onDismiss: () -> Unit,
    onSave: (Category, String) -> Unit
) {
    var categoryName by remember { mutableStateOf(category.name) }

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
                    text = "EDIT CATEGORY",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = CyberBlue
                )

                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Category Name") },
                    placeholder = { Text("Enter a name for your category") },
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
                            if (categoryName.isNotBlank()) {
                                onSave(category, categoryName.trim())
                            }
                        },
                        enabled = categoryName.isNotBlank() && categoryName != category.name,
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
fun DeleteConfirmationDialog(
    categoryName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GlassSurface,
        textContentColor = StarWhite,
        titleContentColor = NeonPink,
        title = {
            Text("Delete Category")
        },
        text = {
            Column {
                Text("Are you sure you want to delete category '$categoryName'?")
                Text(
                    "This will permanently delete the category and all associated promises.",
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