// app/src/main/java/com/example/quotex/ui/categories/SubtitleListScreen.kt

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
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubtitleListScreen(
    viewModel: CategoryViewModel = hiltViewModel(),
    category: String, // This is the categoryName
    title: String,    // This is the titleName
    onBackClick: () -> Unit
) {
    var showAddSubtitleDialog by remember { mutableStateOf(false) }
    var showAddPromiseDialog by remember { mutableStateOf(false) }
    var showEditSubtitleDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var subtitleToEdit by remember { mutableStateOf<Subtitle?>(null) }
    var subtitleToDelete by remember { mutableStateOf<Subtitle?>(null) }

    // Collect data from ViewModel
    val subtitles by viewModel.subtitles.collectAsState()
    val promises by viewModel.promises.collectAsState()
    val selectedSubtitle by viewModel.selectedSubtitle.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Show error if needed
    error?.let {
        LaunchedEffect(it) {
            // You would display a snackbar here
            Log.e("SubtitleListScreen", "Error: $it")
            // Clear error after displaying
            viewModel.clearError()
        }
    }

    // Load data when the screen is first composed
    // Pass both category (categoryName) and title (titleName)
    LaunchedEffect(category, title) {
        viewModel.loadSubtitlesByTitle(categoryName = category, titleName = title)
    }

    // Add this extra LaunchedEffect to refresh subtitles when dialogs change state
    LaunchedEffect(showAddSubtitleDialog, showAddPromiseDialog, showEditSubtitleDialog, showDeleteConfirmation) {
        if (!showAddSubtitleDialog && !showAddPromiseDialog && !showEditSubtitleDialog && !showDeleteConfirmation) {
            // Refresh when all dialogs are closed
            delay(200)
            viewModel.loadSubtitlesByTitle(categoryName = category, titleName = title)
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
                            title, // Displaying the titleName from screen params
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
                        text = "SUB TITLES", // Changed "Sub TITLE" to "SUB TITLES" for clarity
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

                if (isLoading && subtitles.isEmpty()) { // Show loading only if subtitles are not yet loaded
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FuturisticLoadingIndicator()
                    }
                } else if (subtitles.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No subtitles found for this title",
                                style = MaterialTheme.typography.bodyLarge,
                                color = StarWhite.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )

                            Button(
                                onClick = { showAddSubtitleDialog = true },
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
                                Text("Add Subtitle")
                            }
                        }
                    }
                } else {
                    // Subtitle list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp) // Consider making this dynamic or larger if needed
                    ) {
                        items(subtitles) { subtitle ->
                            SubtitleItem(
                                subtitle = subtitle,
                                isSelected = selectedSubtitle?.id == subtitle.id,
                                onClick = { viewModel.selectSubtitle(subtitle.id) },
                                onEditClick = {
                                    subtitleToEdit = subtitle
                                    showEditSubtitleDialog = true
                                },
                                onDeleteClick = {
                                    subtitleToDelete = subtitle
                                    showDeleteConfirmation = true
                                }
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
                        text = "ALL PROMISES",
                        style = MaterialTheme.typography.titleLarge,
                        color = StarWhite,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Show promises for selected subtitle
                if (selectedSubtitle == null) {
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
                } else if (isLoading && promises.isEmpty()) { // Show loading only if promises for selected subtitle are not yet loaded
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        FuturisticLoadingIndicator()
                    }
                } else if (promises.isEmpty()) {
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
                    // Create new subtitle for this title and category
                    viewModel.createSubtitle(
                        name = subtitleName,
                        titleName = title,       // title parameter from SubtitleListScreen
                        categoryName = category  // category parameter from SubtitleListScreen
                    )
                    showAddSubtitleDialog = false
                }
            )
        }

        // Edit Subtitle Dialog
        if (showEditSubtitleDialog && subtitleToEdit != null) {
            EditSubtitleDialog(
                subtitle = subtitleToEdit!!,
                onDismiss = {
                    showEditSubtitleDialog = false
                    subtitleToEdit = null
                },
                onSave = { subtitle, newName ->
                    viewModel.updateSubtitle(subtitle, newName)
                    showEditSubtitleDialog = false
                    subtitleToEdit = null
                }
            )
        }

        // Delete Subtitle Confirmation Dialog
        if (showDeleteConfirmation && subtitleToDelete != null) {
            DeleteSubtitleConfirmationDialog(
                subtitleName = subtitleToDelete!!.name,
                onDismiss = {
                    showDeleteConfirmation = false
                    subtitleToDelete = null
                },
                onConfirm = {
                    viewModel.deleteSubtitle(subtitleToDelete!!)
                    showDeleteConfirmation = false
                    subtitleToDelete = null
                }
            )
        }

        // Add Promise Dialog
        if (showAddPromiseDialog) {
            if (selectedSubtitle != null) {
                AddPromiseDialog(
                    subtitleName = selectedSubtitle?.name ?: "Default", // Pass subtitle name for dialog title
                    onDismiss = { showAddPromiseDialog = false },
                    onSave = { actualPromiseTitle, verseText, scriptureReference ->
                        // Create new promise for this subtitle
                        viewModel.createPromise(
                            actualPromiseTitle = actualPromiseTitle,
                            verseText = verseText,
                            scriptureReference = scriptureReference
                        )
                        showAddPromiseDialog = false
                    }
                )
            } else if (subtitles.isNotEmpty()) {
                viewModel.selectSubtitle(subtitles.first().id)
            } else {
                showAddSubtitleDialog = true
                showAddPromiseDialog = false
            }
        }
    }
}

@Composable
fun SubtitleItem(
    subtitle: Subtitle,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = subtitle.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) StarWhite else StarWhite.copy(alpha = 0.7f),
                modifier = Modifier.weight(1f)
            )
            Row {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .size(36.dp)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit ${subtitle.name}",
                        tint = CyberBlue
                    )
                }
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(36.dp)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete ${subtitle.name}",
                        tint = NeonPink
                    )
                }
            }
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
            // Promise.title from DB is "CategoryName:ActualPromiseTitle"
            // We should probably parse and display only ActualPromiseTitle here
            val actualPromiseTitle = promise.title.substringAfterLast(':', promise.title)
            Text(
                text = actualPromiseTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = ElectricGreen,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Promise.verse from DB might be "ScriptureRef - VerseText"
            // We should parse this too if that's the case.
            // For now, assume promise.verse is just the verse text as per current AddPromiseDialog inputs
            Text(
                text = "\"${promise.verse}\"", // This will show "ScriptureRef - VerseText" if combined
                style = MaterialTheme.typography.bodyLarge,
                color = StarWhite,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Promise.reference from DB is "TitleName|SubtitleName"
            // If scripture reference is stored in promise.verse, we might not need to display this here,
            // or display it differently. Assuming it's meant to be the hierarchy path.
            // For clarity, let's display the scripture reference part if it's combined in verse.
            // If the `scriptureReference` is stored separately, this `promise.reference`
            // (which is Title|Subtitle) might not be what you want to show as "reference" here.
            // Let's assume for now promise.verse is "Scripture Ref - Actual Verse"
            // And promise.reference is "TitleName|SubtitleName"
            // The UI for AddPromiseDialog takes "verse" and "reference (e.g. John 3:16)"
            // ViewModel's createPromise takes "actualPromiseTitle", "verseText", "scriptureReference"
            // ViewModel stores:
            //  Promise.title = "Category:ActualPromiseTitle"
            //  Promise.verse = "ScriptureReference - VerseText"
            //  Promise.reference = "TitleName|SubtitleName"

            val scriptureAndVerse = promise.verse.split(" - ", limit = 2)
            val scriptureRefText = if (scriptureAndVerse.size > 1) scriptureAndVerse[0] else promise.reference // Fallback if parsing fails
            // val actualVerseText = if (scriptureAndVerse.size > 1) scriptureAndVerse[1] else promise.verse // already displayed above as promise.verse

            Text(
                text = scriptureRefText, // Display the scripture reference from the parsed promise.verse
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
fun EditSubtitleDialog(
    subtitle: Subtitle,
    onDismiss: () -> Unit,
    onSave: (Subtitle, String) -> Unit
) {
    var subtitleName by remember { mutableStateOf(subtitle.name) }

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
                    text = "EDIT SUBTITLE",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = CyberBlue
                )
                OutlinedTextField(
                    value = subtitleName,
                    onValueChange = { subtitleName = it },
                    label = { Text("Subtitle Name") },
                    placeholder = { Text("Enter a name for the subtitle") },
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
                            if (subtitleName.isNotBlank()) {
                                onSave(subtitle, subtitleName.trim())
                            }
                        },
                        enabled = subtitleName.isNotBlank() && subtitleName != subtitle.name,
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
fun DeleteSubtitleConfirmationDialog(
    subtitleName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GlassSurface,
        textContentColor = StarWhite,
        titleContentColor = NeonPink,
        title = {
            Text("Delete Subtitle")
        },
        text = {
            Column {
                Text("Are you sure you want to delete subtitle '$subtitleName'?")
                Text(
                    "This will permanently delete the subtitle and all associated promises.",
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

// Corrected AddPromiseDialog function
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPromiseDialog(
    subtitleName: String,
    onDismiss: () -> Unit,
    onSave: (actualPromiseTitle: String, verseText: String, scriptureReference: String) -> Unit
) {
    var actualPromiseTitleState by remember { mutableStateOf("") }
    var verseTextState by remember { mutableStateOf("") } // Corrected: mutableStateOf
    var scriptureReferenceState by remember { mutableStateOf("") }

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
                    text = "ADD PROMISE TO '$subtitleName'",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = ElectricGreen
                )

                OutlinedTextField(
                    value = actualPromiseTitleState,
                    onValueChange = { actualPromiseTitleState = it },
                    label = { Text("Promise Title") },
                    placeholder = { Text("E.g., Trust in God") },
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

                OutlinedTextField(
                    value = verseTextState,
                    onValueChange = { verseTextState = it },
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
                    value = scriptureReferenceState,
                    onValueChange = { scriptureReferenceState = it },
                    label = { Text("Scripture Reference") },
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
                            if (actualPromiseTitleState.isNotBlank() && verseTextState.isNotBlank()) {
                                onSave(
                                    actualPromiseTitleState.trim(),
                                    verseTextState.trim(),
                                    scriptureReferenceState.trim()
                                )
                            }
                        },
                        enabled = actualPromiseTitleState.isNotBlank() && verseTextState.isNotBlank(),
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