// ui/main/MainScreen.kt
package com.example.quotex.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.quotex.R
import com.example.quotex.model.Quote

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    onPromisesClick: () -> Unit,
    showSnackbar: (String) -> Unit
) {
    val proverbs by viewModel.proverbsForToday.observeAsState(emptyList())
    val displayMode by viewModel.displayMode.observeAsState(0)
    val displayPromises by viewModel.displayPromises.observeAsState(false)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Daily Wisdom",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = { viewModel.toggleDisplayMode() },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = when (displayMode) {
                            0 -> "Enable Lock Screen Quotes"
                            1 -> "Display on Screen On"
                            else -> "Display on Unlock"
                        }
                    )
                }

                Button(
                    onClick = { viewModel.toggleDisplayPromises() },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = if (displayPromises) "Hide Promises" else "Display Promises"
                    )
                }
            }
        }

        if (proverbs.isNotEmpty()) {
            QuotePager(
                quotes = proverbs,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        Button(
            onClick = onPromisesClick,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("View Promises")
        }
    }
}

@Composable
fun QuotePager(
    quotes: List<Quote>,
    modifier: Modifier = Modifier
) {
    // This would be implemented with HorizontalPager
    // For now a simple implementation
    Card(
        modifier = modifier.padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        if (quotes.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = quotes.firstOrNull()?.text ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = quotes.firstOrNull()?.reference ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 16.dp)
                )
            }
        }
    }
}