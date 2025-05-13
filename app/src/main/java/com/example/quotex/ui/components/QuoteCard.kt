package com.example.quotex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.quotex.model.Quote
import com.example.quotex.ui.theme.CyberBlue
import com.example.quotex.ui.theme.GlassSurface
import com.example.quotex.ui.theme.GlassSurfaceDark
import com.example.quotex.ui.theme.StarWhite

@Composable
fun QuoteCard(
    quote: Quote,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = GlassSurface.copy(alpha = 0.5f),
            contentColor = StarWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        // Set a higher zIndex for the content to ensure it's above any animations
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .zIndex(10f), // This ensures the column is above any animations
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
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
                    .background(
                        color = GlassSurfaceDark.copy(alpha = 0.25f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}