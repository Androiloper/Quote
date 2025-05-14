package com.example.quotex.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.quotex.model.Quote

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClickableQuotePager(
    quotes: List<Quote>,
    modifier: Modifier = Modifier,
    initialPage: Int = 0,
    onQuoteClick: (Int) -> Unit = {}
) {
    if (quotes.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val pagerState = rememberPagerState(initialPage = initialPage) { quotes.size }

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize()
    ) { page ->
        if (page < quotes.size) {
            val quote = quotes[page]
            val chapterNumber = extractChapterNumber(quote.reference)

            ClickableQuoteCard(
                quote = quote,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                onClick = { onQuoteClick(chapterNumber) }
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No quote available",
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Helper function to extract chapter number from reference (e.g., "Proverbs 3:5")
private fun extractChapterNumber(reference: String): Int {
    return try {
        val parts = reference.split(" ", ":")
        for (i in parts.indices) {
            if (parts[i].equals("Proverbs", ignoreCase = true) && i + 1 < parts.size) {
                return parts[i + 1].toIntOrNull() ?: 1
            }
        }
        1 // Default to chapter 1 if parsing fails
    } catch (e: Exception) {
        1 // Default to chapter 1 if any exception occurs
    }
}