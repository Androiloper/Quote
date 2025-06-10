// app/src/main/java/com/example/quotex/ui/categories/CategoryListScreen.kt

package com.example.quotex.ui.categories

import android.util.Log
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

    // The CategoryViewModel's init block already calls loadInitialData(),
    // which includes loading categories, recent, and favorite promises.
    // If a specific refresh action is needed for categories (e.g., on pull-to-refresh),
    // a public method in the ViewModel would be appropriate. For initial load,
    // the init block handles it.
    LaunchedEffect(Unit) {
        // viewModel.loadCategories() // This is also covered by loadInitialData in VM init.
        // Keep if you specifically need to re-load categories here.
        // Otherwise, it's redundant for the initial composition.
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
                    val randomAlpha = (Math.random() * 0.8f + 0.2f).toFloat()
                    drawCircle(
                        color = StarWhite.copy(alpha = randomAlpha),
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
                    .padding(vertical = 0.dp) // i added
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
                val currentPromisesList = if (selectedTab == 0) recentPromises else favoritePromises
                if (isLoading && currentPromisesList.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FuturisticLoadingIndicator()
                    }
                } else if (currentPromisesList.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedTab == 0) "No recent promises" else "No favorite promises",
                            color = StarWhite.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().height(300.dp).padding(vertical = 8.dp)
                    ) {
                        items(currentPromisesList) { promise ->
                            GlassCard(
                                modifier =
                                Modifier
                                    .padding(paddingValues)
                                    .fillMaxSize()
                                    .clickable { onPromiseClick(promise.id) } // Or navigate to a detail screen
                            ) {
                                // Displaying promise.title which is "Category:ActualPromiseTitle"
                                // You might want to parse this to show only "ActualPromiseTitle"
                                val displayTitle = promise.title.substringAfter(com.example.quotex.data.repository.PromisesRepository.CATEGORY_SEPARATOR, promise.title)
                                Text(
                                    text = displayTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(16.dp),
                                    color = StarWhite,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "CATEGORIES",
                    style = MaterialTheme.typography.titleLarge,
                    color = ElectricGreen,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    textAlign = TextAlign.Center
                )

                if (isLoading && categories.isEmpty()) { // Show loading only if categories are truly being fetched for the first time
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f), // Use weight to fill remaining space
                        contentAlignment = Alignment.Center
                    ) {
                        FuturisticLoadingIndicator()
                    }
                } else if (categories.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("No categories yet", color = StarWhite.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyLarge)
                            Button(
                                onClick = { showAddCategoryDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = NebulaPurple)
                            ) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Category")
                            }
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) { // Use LazyColumn for categories if they can exceed screen
                        val chunkedCategories = categories.chunked(1)
                        items(chunkedCategories) { rowCategories ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                horizontalArrangement = if (rowCategories.size < 2) Arrangement.Start else Arrangement.SpaceBetween
                            ) {
                                rowCategories.forEach { category ->
                                    CategoryButton(
                                        category = category,
                                        onClick = { onCategoryClick(category.name) },
                                        onEditClick = { categoryToEdit = category; showEditCategoryDialog = true },
                                        onDeleteClick = { categoryToDelete = category; showDeleteConfirmation = true },
                                        // isLastItem logic might need adjustment if in LazyColumn
                                    )
                                }
                                // If there's only one item in the last row, add a spacer to keep alignment
                                if (rowCategories.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f)) // This assumes CategoryButton has a fixed width or weight
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddCategoryDialog) {
            AddCategoryDialog(
                onDismiss = { showAddCategoryDialog = false },
                onSave = { categoryName -> viewModel.createCategory(categoryName); showAddCategoryDialog = false }
            )
        }

        if (showEditCategoryDialog && categoryToEdit != null) {
            EditCategoryDialog(
                category = categoryToEdit!!,
                onDismiss = { showEditCategoryDialog = false; categoryToEdit = null },
                onSave = { category, newName -> viewModel.updateCategory(category, newName); showEditCategoryDialog = false; categoryToEdit = null }
            )
        }

        if (showDeleteConfirmation && categoryToDelete != null) {
            DeleteConfirmationDialog(
                categoryName = categoryToDelete!!.name,
                onDismiss = { showDeleteConfirmation = false; categoryToDelete = null },
                onConfirm = { viewModel.deleteCategory(categoryToDelete!!); showDeleteConfirmation = false; categoryToDelete = null }
            )
        }
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) NebulaPurple else GlassSurface),
        modifier = Modifier.widthIn(min = 120.dp) // Use widthIn for flexibility
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
    isLastItem: Boolean = false // This might be less relevant if categories are in a LazyColumn
) {
    Box(
        // Use a flexible width, e.g., by applying a weight in the parent Row
        // Or set a specific width if that's the design. For two items per row,
        // a width slightly less than half the screen width (considering padding) is typical.
        // Modifier.width(150.dp) might be too rigid.
        // If used in a Row with Arrangement.SpaceBetween and fixed count (2), it can work.
        modifier = Modifier.padding(4.dp) // Outer padding
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(16.dp),
            border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(colors = listOf(CyberBlue, NeonPink))),
            contentPadding = PaddingValues(vertical = 20.dp, horizontal = 16.dp), // Increased padding
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 60.dp) // Ensure a minimum height
        ) {
            Text(text = category.name, color = StarWhite, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        }

        IconButton(
            onClick = onEditClick,
            modifier = Modifier.size(32.dp).align(Alignment.TopEnd).offset(x = (8).dp, y = (-8).dp).background(NebulaPurple, CircleShape).padding(4.dp)
        ) {
            Icon(Icons.Default.Edit, "Edit ${category.name}", tint = StarWhite, modifier = Modifier.size(16.dp))
        }

        IconButton(
            onClick = onDeleteClick,
            modifier = Modifier.size(32.dp).align(Alignment.BottomEnd).offset(x = (8).dp, y = (8).dp).background(NeonPink, CircleShape).padding(4.dp)
        ) {
            Icon(Icons.Default.Delete, "Delete ${category.name}", tint = StarWhite, modifier = Modifier.size(16.dp))
        }
        // The isLastItem FAB logic might be removed if using a global FAB for adding categories
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var categoryName by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = GlassSurface, contentColor = StarWhite),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("CREATE NEW CATEGORY", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = ElectricGreen)
                OutlinedTextField(
                    value = categoryName, onValueChange = { categoryName = it }, label = { Text("Category Name") },
                    placeholder = { Text("Enter a name for your category") }, modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricGreen, unfocusedBorderColor = StarWhite.copy(alpha = 0.5f),
                        focusedTextColor = StarWhite, unfocusedTextColor = StarWhite, cursorColor = ElectricGreen,
                        focusedLabelColor = ElectricGreen, unfocusedLabelColor = StarWhite.copy(alpha = 0.7f)
                    ), singleLine = true
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss, Modifier.padding(end = 8.dp), colors = ButtonDefaults.textButtonColors(contentColor = StarWhite.copy(alpha = 0.7f))) { Text("CANCEL") }
                    Button(
                        onClick = { if (categoryName.isNotBlank()) onSave(categoryName.trim()) },
                        enabled = categoryName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = NebulaPurple, disabledContainerColor = NebulaPurple.copy(alpha = 0.5f))
                    ) { Text("CREATE") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCategoryDialog(category: Category, onDismiss: () -> Unit, onSave: (Category, String) -> Unit) {
    var categoryName by remember { mutableStateOf(category.name) }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = GlassSurface, contentColor = StarWhite),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("EDIT CATEGORY", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = CyberBlue)
                OutlinedTextField(
                    value = categoryName, onValueChange = { categoryName = it }, label = { Text("Category Name") },
                    placeholder = { Text("Enter a name for your category") }, modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberBlue, unfocusedBorderColor = StarWhite.copy(alpha = 0.5f),
                        focusedTextColor = StarWhite, unfocusedTextColor = StarWhite, cursorColor = CyberBlue,
                        focusedLabelColor = CyberBlue, unfocusedLabelColor = StarWhite.copy(alpha = 0.7f)
                    ), singleLine = true
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss, Modifier.padding(end = 8.dp), colors = ButtonDefaults.textButtonColors(contentColor = StarWhite.copy(alpha = 0.7f))) { Text("CANCEL") }
                    Button(
                        onClick = { if (categoryName.isNotBlank()) onSave(category, categoryName.trim()) },
                        enabled = categoryName.isNotBlank() && categoryName != category.name,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberBlue, disabledContainerColor = CyberBlue.copy(alpha = 0.5f))
                    ) { Text("SAVE") }
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(categoryName: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = GlassSurface, textContentColor = StarWhite, titleContentColor = NeonPink,
        title = { Text("Delete Category") },
        text = { Column {
            Text("Are you sure you want to delete category '$categoryName'?")
            Text("This will permanently delete the category and all associated titles, subtitles, and promises.", color = StarWhite.copy(alpha = 0.7f), modifier = Modifier.padding(top = 8.dp))
        }},
        confirmButton = { Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = NeonPink)) { Text("DELETE") }},
        dismissButton = { TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = StarWhite)) { Text("CANCEL") }}
    )
}