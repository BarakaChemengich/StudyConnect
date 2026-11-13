package com.studyconnect.app.ui.resources

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.studyconnect.app.ui.AppViewModelProvider
import com.studyconnect.app.ui.components.EmptyState
import com.studyconnect.app.ui.components.ErrorState
import com.studyconnect.app.ui.components.LoadingState
import com.studyconnect.app.ui.components.ScreenHeader
import com.studyconnect.app.ui.theme.Background
import com.studyconnect.app.ui.theme.Green
import com.studyconnect.app.ui.theme.TextMuted
import com.studyconnect.app.util.Subjects

@Composable
fun ResourcesScreen(viewModel: ResourcesViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val subject by viewModel.subject.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    Column(Modifier.fillMaxSize().background(Background)) {
        ScreenHeader(title = "Learning resources", subtitle = "Books and references for $subject")

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(Subjects.all.filter { it.name != "Other" }) { s ->
                FilterChip(
                    selected = subject == s.name,
                    onClick = { viewModel.select(s.name) },
                    label = { Text("${s.emoji} ${s.name}") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Green, selectedLabelColor = Color.White)
                )
            }
        }

        when (val current = state) {
            is ResourcesUiState.Loading -> LoadingState(message = "Fetching resources...")
            is ResourcesUiState.Error -> ErrorState(current.message, onRetry = viewModel::load)
            is ResourcesUiState.Empty -> EmptyState("\uD83D\uDCDA", "Nothing found", "Try another subject.")
            is ResourcesUiState.Success -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(current.items) { resource ->
                    Card(
                        onClick = { if (resource.key.isNotBlank()) uriHandler.openUri("https://openlibrary.org${resource.key}") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(resource.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(2.dp))
                            Text(resource.authors, color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                            resource.year?.let { Text("First published $it", color = TextMuted, style = MaterialTheme.typography.bodySmall) }
                        }
                    }
                }
            }
        }
    }
}
