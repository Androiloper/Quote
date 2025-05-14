package com.example.quotex.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.quotex.model.Quote
import com.example.quotex.ui.theme.CyberBlue
import com.example.quotex.ui.theme.GlassSurface
import com.example.quotex.ui.theme.StarWhite
import com.example.quotex.util.ProverbsUtil

/**
 * A clickable card that displays a quote and navigates to its chapter when clicked
 */
@Composable
fun ClickableQuoteCard(
    quote: Quote,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = GlassSurface.copy(alpha = 0.5f),
            contentColor = StarWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "\"${quote.text}\"",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp),
                color = StarWhite
            )

            Text(
                text = quote.reference,
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = CyberBlue,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

/**
 * A horizontal pager that displays clickable quotes
 * When a quote is clicked, it navigates to the chapter screen for that chapter
 */
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

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Quote pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            if (page < quotes.size) {
                val quote = quotes[page]
                val chapterNumber = ProverbsUtil.extractChapterNumber(quote.reference)

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

        // Pager indicators
        if (quotes.size > 1) {
            PagerIndicator(
                pageCount = quotes.size,
                currentPage = pagerState.currentPage,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}