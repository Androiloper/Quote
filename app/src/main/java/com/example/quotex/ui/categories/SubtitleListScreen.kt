// app/src/main/java/com/example/quotex/ui/categories/SubtitleListScreen.kt
// ... other imports ...
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
import com.example.quotex.data.repository.PromisesRepository // Import for separator
import com.example.quotex.model.Promise
import com.example.quotex.model.Subtitle
import com.example.quotex.ui.components.FuturisticLoadingIndicator
import com.example.quotex.ui.components.GlassCard
import com.example.quotex.ui.theme.*
import kotlinx.coroutines.delay


// ... (SubtitleListScreen composable remains largely the same, ensure it calls the corrected PromiseItem) ...

@Composable
fun PromiseItem(promise: Promise) {
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
            // Promise.title is "CategoryName:ActualPromiseTitle"
            val actualPromiseTitle = promise.title.substringAfter(PromisesRepository.CATEGORY_SEPARATOR, promise.title)
            Text(
                text = actualPromiseTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = ElectricGreen,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Promise.verse is the verse text
            Text(
                text = "\"${promise.verse}\"",
                style = MaterialTheme.typography.bodyLarge,
                color = StarWhite,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Promise.reference is "TitleName|SubtitleName|ScriptureReference"
            val refParts = promise.reference.split(PromisesRepository.TITLE_SEPARATOR)
            val scriptureRefDisplay = if (refParts.size > 2) refParts[2].trim() else "N/A"

            Text(
                text = scriptureRefDisplay,
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = CyberBlue,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

// ... (Rest of SubtitleListScreen.kt, including dialogs, etc.)
// Ensure AddPromiseDialog calls viewModel.createPromise with (actualPromiseTitle, verseText, scriptureReference)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPromiseDialog( // This is in SubtitleListScreen.kt
    subtitleName: String, // To show in the dialog title
    onDismiss: () -> Unit,
    onSave: (actualPromiseTitle: String, verseText: String, scriptureReference: String) -> Unit
) {
    var actualPromiseTitleState by remember { mutableStateOf("") }
    var verseTextState by remember { mutableStateOf("") }
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
                    label = { Text("Promise Title (e.g., Trust in God)") }, // Clarified label
                    placeholder = { Text("Enter the specific title of this promise") },
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
                    label = { Text("Promise Verse Text") },
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
                    label = { Text("Scripture Reference (e.g., John 3:16)") },
                    placeholder = { Text("Enter the Bible reference") },
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
                            if (actualPromiseTitleState.isNotBlank() && verseTextState.isNotBlank() && scriptureReferenceState.isNotBlank()) {
                                onSave(
                                    actualPromiseTitleState.trim(),
                                    verseTextState.trim(),
                                    scriptureReferenceState.trim()
                                )
                            }
                        },
                        enabled = actualPromiseTitleState.isNotBlank() && verseTextState.isNotBlank() && scriptureReferenceState.isNotBlank(),
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
// Make sure the rest of SubtitleListScreen.kt uses these corrected dialogs and items.
// The full SubtitleListScreen is large, so I'm focusing on the PromiseItem and AddPromiseDialog here.
// You would integrate these corrected parts into the existing SubtitleListScreen.kt.
// The original SubtitleListScreen.kt already contains the structure for Scaffold, TopAppBar, FAB,
// and LazyColumns for subtitles and promises. The key is to use the corrected PromiseItem.
// Also, when calling AddPromiseDialog, ensure it's correctly wired to viewModel.createPromise.

// Placeholder for the rest of the SubtitleListScreen, as it's quite long.
// The critical parts for promise display and creation are PromiseItem and AddPromiseDialog.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubtitleListScreen( // This is the main screen function
    viewModel: CategoryViewModel = hiltViewModel(),
    category: String,
    title: String,
    onBackClick: () -> Unit
) {
    var showAddSubtitleDialog by remember { mutableStateOf(false) }
    var showAddPromiseDialog by remember { mutableStateOf(false) }
    var showEditSubtitleDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var subtitleToEdit by remember { mutableStateOf<Subtitle?>(null) }
    var subtitleToDelete by remember { mutableStateOf<Subtitle?>(null) }

    val subtitles by viewModel.subtitles.collectAsState()
    val promisesFromVm by viewModel.promises.collectAsState() // Renamed to avoid conflict
    val selectedSubtitle by viewModel.selectedSubtitle.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    error?.let {
        LaunchedEffect(it) {
            Log.e("SubtitleListScreen", "Error: $it")
            viewModel.clearError()
        }
    }

    LaunchedEffect(category, title) {
        viewModel.loadSubtitlesByTitle(categoryName = category, titleName = title)
    }

    LaunchedEffect(showAddSubtitleDialog, showAddPromiseDialog, showEditSubtitleDialog, showDeleteConfirmation) {
        if (!showAddSubtitleDialog && !showAddPromiseDialog && !showEditSubtitleDialog && !showDeleteConfirmation) {
            delay(200)
            viewModel.loadSubtitlesByTitle(categoryName = category, titleName = title)
            selectedSubtitle?.let { viewModel.selectSubtitle(it.id) } // Reselect to refresh promises
        }
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
                    val alphaRandom = (Math.random() * 0.8f + 0.2f).toFloat()
                    drawCircle(color = StarWhite.copy(alpha = alphaRandom), radius = radius, center = Offset(x, y))
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
            containerColor = Color.Transparent,
            contentColor = StarWhite,
            topBar = {
                TopAppBar(
                    title = { Text(title, style = MaterialTheme.typography.headlineLarge) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, "Back", tint = StarWhite)
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
                        if (selectedSubtitle != null || subtitles.isEmpty()) {
                            showAddPromiseDialog = true
                        } else {
                            // Prompt to select or create subtitle if none selected and list is not empty
                            viewModel.selectSubtitle(subtitles.first().id) // Auto-select first if not selected
                            showAddPromiseDialog = true // Then show dialog
                            Log.d("SubtitleListScreen", "No subtitle selected, auto-selecting first or prompting.")
                        }
                    },
                    containerColor = NebulaPurple,
                    contentColor = StarWhite
                ) { Icon(Icons.Default.Add, "Add Promise") }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("SUBTITLES", style = MaterialTheme.typography.titleLarge, color = StarWhite)
                    IconButton(onClick = { showAddSubtitleDialog = true }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Add, "Add Subtitle", tint = StarWhite)
                    }
                }

                if (isLoading && subtitles.isEmpty()) {
                    Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) { FuturisticLoadingIndicator() }
                } else if (subtitles.isEmpty()) {
                    Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No subtitles yet for this title.", color = StarWhite.copy(alpha = 0.7f))
                            Button(onClick = { showAddSubtitleDialog = true }, modifier = Modifier.padding(top=8.dp)) { Text("Create Subtitle") }
                        }
                    }
                } else {
                    LazyColumn(Modifier.fillMaxWidth().heightIn(min = 100.dp, max = 200.dp)) { // Constrain height
                        items(subtitles) { subtitle ->
                            SubtitleItem(
                                subtitle = subtitle,
                                isSelected = selectedSubtitle?.id == subtitle.id,
                                onClick = { viewModel.selectSubtitle(subtitle.id) },
                                onEditClick = { subtitleToEdit = subtitle; showEditSubtitleDialog = true },
                                onDeleteClick = { subtitleToDelete = subtitle; showDeleteConfirmation = true }
                            )
                        }
                    }
                }
                if (subtitles.size > 1) { // Pagination for subtitles
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.Center) {
                        subtitles.forEach { subtitle ->
                            Box(
                                Modifier.size(8.dp).padding(horizontal = 2.dp)
                                    .background(if (selectedSubtitle?.id == subtitle.id) ElectricGreen else StarWhite.copy(alpha = 0.3f), CircleShape)
                            )
                        }
                    }
                }


                Text("PROMISES", style = MaterialTheme.typography.titleLarge, color = StarWhite, modifier = Modifier.padding(vertical = 8.dp))

                if (selectedSubtitle == null && subtitles.isNotEmpty()) {
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("Select a subtitle to view its promises.", color = StarWhite.copy(alpha = 0.7f))
                    }
                } else if (isLoading && promisesFromVm.isEmpty() && selectedSubtitle != null) {
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { FuturisticLoadingIndicator() }
                } else if (promisesFromVm.isEmpty()) {
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                if (selectedSubtitle == null && subtitles.isEmpty()) "Create a subtitle first to add promises."
                                else "No promises for this subtitle.",
                                color = StarWhite.copy(alpha = 0.7f), textAlign = TextAlign.Center
                            )
                            if (selectedSubtitle != null || subtitles.isEmpty()){ // Show add button if a subtitle could be target or needs creating
                                Button(onClick = { showAddPromiseDialog = true }, modifier = Modifier.padding(top=8.dp)) { Text("Add Promise") }
                            }
                        }
                    }
                } else {
                    LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
                        items(promisesFromVm) { promise ->
                            PromiseItem(promise = promise) // Uses the corrected PromiseItem
                        }
                    }
                }
            }
        }

        if (showAddSubtitleDialog) {
            AddSubtitleDialog(
                onDismiss = { showAddSubtitleDialog = false },
                onSave = { subtitleName ->
                    viewModel.createSubtitle(subtitleName, title, category)
                    showAddSubtitleDialog = false
                }
            )
        }
        if (showEditSubtitleDialog && subtitleToEdit != null) {
            EditSubtitleDialog(subtitleToEdit!!, { showEditSubtitleDialog = false; subtitleToEdit = null }) { sub, newName ->
                viewModel.updateSubtitle(sub, newName)
                showEditSubtitleDialog = false; subtitleToEdit = null
            }
        }
        if (showDeleteConfirmation && subtitleToDelete != null) {
            DeleteSubtitleConfirmationDialog(subtitleToDelete!!.name, { showDeleteConfirmation = false; subtitleToDelete = null }) {
                viewModel.deleteSubtitle(subtitleToDelete!!)
                showDeleteConfirmation = false; subtitleToDelete = null
            }
        }
        if (showAddPromiseDialog) {
            val targetSubtitleName = selectedSubtitle?.name ?: if (subtitles.isNotEmpty() && selectedSubtitle == null) {
                // If no subtitle is selected but some exist, auto-select the first one before adding a promise
                viewModel.selectSubtitle(subtitles.first().id)
                subtitles.first().name
            } else "New Subtitle" // Fallback, or handle by creating subtitle first

            // Only show AddPromiseDialog if a subtitle is selected or can be determined
            if (selectedSubtitle != null || (subtitles.isEmpty() && targetSubtitleName == "New Subtitle") ) {
                AddPromiseDialog( // This is the corrected one
                    subtitleName = selectedSubtitle?.name ?: "Selected Subtitle", // Pass current/target subtitle name
                    onDismiss = { showAddPromiseDialog = false },
                    onSave = { actualPromiseTitle, verseText, scriptureRef ->
                        viewModel.createPromise(actualPromiseTitle, verseText, scriptureRef)
                        showAddPromiseDialog = false
                    }
                )
            } else if (subtitles.isNotEmpty() && selectedSubtitle == null) {
                // This case means subtitles exist, but none selected. AddPromiseDialog needs a target.
                // Could show a message or auto-select first subtitle.
                // For now, FAB click logic handles auto-selection.
                // To prevent dialog showing without target:
                // showAddPromiseDialog = false (and maybe show a Toast)
                Log.w("SubtitleListScreen", "Add Promise clicked but no subtitle target determined.")
            }


        }
    }
}

// Definitions for AddSubtitleDialog, EditSubtitleDialog, DeleteSubtitleConfirmationDialog
// are assumed to be in this file or imported correctly as per the previous structure of SubtitleListScreen.kt
// For brevity, their full code isn't repeated here if unchanged from the original file structure.
// Ensure they match the definitions provided in the thought process or previous file structures.

// Make sure SubtitleItem is also defined as in previous examples:
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
        border = if (isSelected) BorderStroke(1.dp, Brush.horizontalGradient(listOf(CyberBlue, NeonPink))) else null
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
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
                IconButton(onClick = onEditClick, Modifier.size(36.dp).padding(4.dp)) {
                    Icon(Icons.Default.Edit, "Edit ${subtitle.name}", tint = CyberBlue)
                }
                IconButton(onClick = onDeleteClick, Modifier.size(36.dp).padding(4.dp)) {
                    Icon(Icons.Default.Delete, "Delete ${subtitle.name}", tint = NeonPink)
                }
            }
        }
    }
}
// Definitions for AddSubtitleDialog, EditSubtitleDialog, DeleteSubtitleConfirmationDialog
// should be present in this file as they were in the original SubtitleListScreen.kt structure.
// Adding them here for completeness, assuming they are the same as previously defined:

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubtitleDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var subtitleName by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) { /* ... (rest of dialog from original file) ... */
        Card(
            colors = CardDefaults.cardColors(containerColor = GlassSurface, contentColor = StarWhite),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("CREATE NEW SUBTITLE", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = ElectricGreen)
                OutlinedTextField(
                    value = subtitleName, onValueChange = { subtitleName = it }, label = { Text("Subtitle Name") },
                    placeholder = { Text("Enter a name for the subtitle") }, modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricGreen, unfocusedBorderColor = StarWhite.copy(alpha = 0.5f),
                        focusedTextColor = StarWhite, unfocusedTextColor = StarWhite, cursorColor = ElectricGreen,
                        focusedLabelColor = ElectricGreen, unfocusedLabelColor = StarWhite.copy(alpha = 0.7f)
                    ), singleLine = true
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss, Modifier.padding(end = 8.dp), colors = ButtonDefaults.textButtonColors(contentColor = StarWhite.copy(alpha = 0.7f))) { Text("CANCEL") }
                    Button(
                        onClick = { if (subtitleName.isNotBlank()) onSave(subtitleName.trim()) },
                        enabled = subtitleName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = NebulaPurple, disabledContainerColor = NebulaPurple.copy(alpha = 0.5f))
                    ) { Text("CREATE") }
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
    Dialog(onDismissRequest = onDismiss) { /* ... (rest of dialog from original file) ... */
        Card(
            colors = CardDefaults.cardColors(containerColor = GlassSurface, contentColor = StarWhite),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("EDIT SUBTITLE", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = CyberBlue)
                OutlinedTextField(
                    value = subtitleName, onValueChange = { subtitleName = it }, label = { Text("Subtitle Name") },
                    placeholder = { Text("Enter a name for the subtitle") }, modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberBlue, unfocusedBorderColor = StarWhite.copy(alpha = 0.5f),
                        focusedTextColor = StarWhite, unfocusedTextColor = StarWhite, cursorColor = CyberBlue,
                        focusedLabelColor = CyberBlue, unfocusedLabelColor = StarWhite.copy(alpha = 0.7f)
                    ), singleLine = true
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss, Modifier.padding(end = 8.dp), colors = ButtonDefaults.textButtonColors(contentColor = StarWhite.copy(alpha = 0.7f))) { Text("CANCEL") }
                    Button(
                        onClick = { if (subtitleName.isNotBlank()) onSave(subtitle, subtitleName.trim()) },
                        enabled = subtitleName.isNotBlank() && subtitleName != subtitle.name,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberBlue, disabledContainerColor = CyberBlue.copy(alpha = 0.5f))
                    ) { Text("SAVE") }
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
        onDismissRequest = onDismiss, containerColor = GlassSurface, textContentColor = StarWhite, titleContentColor = NeonPink,
        title = { Text("Delete Subtitle") },
        text = { Column {
            Text("Are you sure you want to delete subtitle '$subtitleName'?")
            Text("This will permanently delete the subtitle and all associated promises.", color = StarWhite.copy(alpha = 0.7f), modifier = Modifier.padding(top = 8.dp))
        }},
        confirmButton = { Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = NeonPink)) { Text("DELETE") }},
        dismissButton = { TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = StarWhite)) { Text("CANCEL") }}
    )
}