package com.studyconnect.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.studyconnect.app.data.local.BookingDetail
import com.studyconnect.app.ui.AppViewModelProvider
import com.studyconnect.app.ui.components.EmptyState
import com.studyconnect.app.ui.components.ErrorState
import com.studyconnect.app.ui.components.GradientHeader
import com.studyconnect.app.ui.components.LoadingState
import com.studyconnect.app.ui.components.SearchField
import com.studyconnect.app.ui.components.SectionTitle
import com.studyconnect.app.ui.components.TutorCard
import com.studyconnect.app.ui.theme.Background
import com.studyconnect.app.ui.theme.Green
import com.studyconnect.app.ui.theme.GreenDark
import com.studyconnect.app.ui.theme.GreenLight
import com.studyconnect.app.util.DateUtils
import com.studyconnect.app.util.Subjects
/**
 * Displays the main dashboard.
 * Allows students to explore tutors,
 * search for academic support and view subjects.
 */
@Composable
fun HomeScreen(
    onTutorClick: (Long) -> Unit,
    onBecomeTutor: () -> Unit,
    onOpenSessions: () -> Unit,
    onSeeAllSubjects: () -> Unit = {},
    initialSubject: String? = null,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val subject by viewModel.subject.collectAsStateWithLifecycle()
    val availableOnly by viewModel.availableOnly.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val nextSession by viewModel.nextSession.collectAsStateWithLifecycle()

    LaunchedEffect(initialSubject) {
        if (initialSubject != null) {
            viewModel.onSubjectSelected(if (initialSubject == "ALL") null else initialSubject)
        }
    }

    Column(Modifier.fillMaxSize().background(Background)) {
        GradientHeader {
            Text(
                if (userName.isBlank()) "Hello \uD83D\uDC4B" else "Hi, $userName \uD83D\uDC4B",
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "Find a peer tutor",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(12.dp))
            SearchField(query, viewModel::onQueryChange) // Three ways to narrow the list (search text, subject chips, 'Available now') so a student reaches a suitable tutor quickly.
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                HomeBanner(nextSession, onOpenSessions = onOpenSessions, onBecomeTutor = onBecomeTutor)
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionTitle("Subjects")
                        TextButton(onClick = onSeeAllSubjects) {
                            Text("See All →", color = Green, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            SubjectChip("\uD83D\uDD0E All", selected = subject == null) {
                                onSeeAllSubjects()
                            }
                        }
                        items(Subjects.all) { s ->
                            SubjectChip("${s.emoji} ${s.name}", selected = subject == s.name) {
                                viewModel.onSubjectSelected(if (subject == s.name) null else s.name)
                            }
                        }
                    }
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val count = (state as? HomeUiState.Success)?.tutors?.size
                    SectionTitle(if (count != null) "Tutors ($count)" else "Tutors")
                    SubjectChip("Available now", selected = availableOnly) {
                        viewModel.onAvailableOnlyChange(!availableOnly)
                    }
                }
            }

            when (val current = state) {
                is HomeUiState.Loading -> item(span = { GridItemSpan(maxLineSpan) }) { LoadingState(message = "Loading tutors...") }
                is HomeUiState.Error -> item(span = { GridItemSpan(maxLineSpan) }) {
                    ErrorState(current.message, onRetry = viewModel::retry)
                }
                is HomeUiState.Success -> {
                    if (current.tutors.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            EmptyState(
                                emoji = "\uD83D\uDD0D",
                                title = "No tutors found",
                                message = "Try another subject, clear the search, or offer to tutor this subject yourself.",
                                actionLabel = "Become a tutor",
                                onAction = onBecomeTutor
                            )
                        }
                    } else {
                        items(current.tutors, key = { it.id }) { tutor ->
                            TutorCard(tutor, onClick = { onTutorClick(tutor.id) }) // The card shows subject, rating and availability, so tutors can be compared without opening each one.
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Green,
            selectedLabelColor = Color.White
        )
    )
}

@Composable
private fun HomeBanner(next: BookingDetail?, onOpenSessions: () -> Unit, onBecomeTutor: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.horizontalGradient(listOf(GreenDark, GreenLight)))
            .padding(20.dp)
    ) {
        if (next != null) {
            Text("NEXT SESSION", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelMedium)
            Text(
                "${next.subject} with ${next.tutorName}",
                color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold
            )
            Text(DateUtils.formatDateTime(next.startTime), color = Color.White.copy(alpha = 0.9f))
            Spacer(Modifier.height(12.dp))
            BannerButton("View my sessions", onOpenSessions)
        } else {
            Text(
                "Stuck on a unit?",
                color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Pick a tutor below and book a session in under a minute. Good at a subject? Help a classmate.",
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(Modifier.height(12.dp))
            BannerButton("Become a tutor", onBecomeTutor)
        }
    }
}

@Composable
private fun BannerButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = GreenDark)
    ) { Text(text, fontWeight = FontWeight.Bold) }
}
