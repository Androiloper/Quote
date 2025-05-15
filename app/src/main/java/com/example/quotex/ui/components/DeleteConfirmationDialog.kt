package com.example.quotex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.quotex.ui.theme.GlassSurface
import com.example.quotex.ui.theme.NeonPink
import com.example.quotex.ui.theme.StarWhite

/**
 * A common confirmation dialog for delete operations
 */
@Composable
fun DeleteConfirmationDialog(
    title: String,
    itemName: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GlassSurface,
        textContentColor = StarWhite,
        titleContentColor = NeonPink,
        title = {
            Text(title)
        },
        text = {
            Column {
                Text("Are you sure you want to delete $itemName '$itemName'?")
                Text(
                    message,
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

/**
 * A customizable snackbar for showing success messages or errors
 */
@Composable
fun CosmicSnackbar(
    message: String,
    isError: Boolean = false,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val backgroundColor = if (isError) NeonPink.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(16.dp)
        ) {
            Text(
                text = message,
                color = StarWhite,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = if (actionLabel != null) 8.dp else 0.dp)
            )

            if (actionLabel != null && onActionClick != null) {
                TextButton(
                    onClick = {
                        onActionClick()
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = StarWhite
                    ),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}