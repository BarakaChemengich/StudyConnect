package com.studyconnect.app.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.studyconnect.app.data.local.Booking
import com.studyconnect.app.data.local.BookingDetail
import com.studyconnect.app.ui.AppViewModelProvider
import com.studyconnect.app.ui.components.EmptyState
import com.studyconnect.app.ui.components.LoadingState
import com.studyconnect.app.ui.components.ScreenHeader
import com.studyconnect.app.ui.components.StatusChip
import com.studyconnect.app.ui.components.SubjectAvatar
import com.studyconnect.app.ui.theme.Background
import com.studyconnect.app.ui.theme.ErrorRed
import com.studyconnect.app.ui.theme.Green
import com.studyconnect.app.ui.theme.TextMuted
import com.studyconnect.app.util.DateUtils

private enum class SessionMode { UPCOMING, PAST, TEACHING }

@Composable
fun SessionsScreen(
    onReschedule: (tutorId: Long, bookingId: Long) -> Unit,
    onFindTutor: () -> Unit,
    viewModel: SessionsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var toCancel by remember { mutableStateOf<BookingDetail?>(null) }
    var toDelete by remember { mutableStateOf<BookingDetail?>(null) }

    Column(Modifier.fillMaxSize().background(Background)) {
        ScreenHeader(title = "My sessions", subtitle = "Upcoming, past and the ones you teach")

        TabRow(selectedTabIndex = tab, containerColor = Color.Transparent, contentColor = Green) {
            listOf("Upcoming (${state.upcoming.size})", "Past", "Teaching (${state.teaching.size})").forEachIndexed { index, title ->
                Tab(selected = tab == index, onClick = { tab = index }, text = { Text(title, fontWeight = FontWeight.SemiBold) })
            }
        }

        if (state.loading) {
            LoadingState()
        } else {
            val (list, mode) = when (tab) {
                0 -> state.upcoming to SessionMode.UPCOMING
                1 -> state.past to SessionMode.PAST
                else -> state.teaching to SessionMode.TEACHING
            }
            if (list.isEmpty()) {
                when (mode) {
                    SessionMode.UPCOMING -> EmptyState("\uD83D\uDCC5", "No upcoming sessions", "Find a tutor and book your first session.", actionLabel = "Find a tutor", onAction = onFindTutor)
                    SessionMode.PAST -> EmptyState("\uD83D\uDDC2\uFE0F", "No history yet", "Finished and cancelled sessions will show up here.")
                    SessionMode.TEACHING -> EmptyState("\uD83E\uDDD1\u200D\uD83C\uDFEB", "Nobody has booked you yet", "Create a tutor profile and classmates can book you.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(list, key = { it.id }) { booking ->
                        SessionCard(
                            booking = booking,
                            mode = mode,
                            onReschedule = { onReschedule(booking.tutorId, booking.id) },
                            onCancel = { toCancel = booking },
                            onDelete = { toDelete = booking }
                        )
                    }
                }
            }
        }
    }

    toCancel?.let { booking ->
        AlertDialog(
            onDismissRequest = { toCancel = null },
            title = { Text("Cancel this session?") },
            text = { Text("${booking.subject} with ${booking.tutorName} on ${DateUtils.formatDateTime(booking.startTime)}") },
            confirmButton = {
                TextButton(onClick = { viewModel.cancel(booking); toCancel = null }) { Text("Cancel session", color = ErrorRed) }
            },
            dismissButton = { TextButton(onClick = { toCancel = null }) { Text("Keep it") } }
        )
    }
    toDelete?.let { booking ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Delete from history?") },
            text = { Text("This permanently removes the record.") },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(booking); toDelete = null }) { Text("Delete", color = ErrorRed) }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Keep") } }
        )
    }
}

@Composable
private fun SessionCard(
    booking: BookingDetail,
    mode: SessionMode,
    onReschedule: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SubjectAvatar(booking.subject, size = 48)
                Column(Modifier.weight(1f)) {
                    Text(booking.subject, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (mode == SessionMode.TEACHING) "Student: ${booking.studentName}" else "Tutor: ${booking.tutorName}",
                        color = TextMuted
                    )
                }
                when {
                    booking.status == Booking.CANCELLED -> StatusChip("Cancelled", ErrorRed)
                    mode == SessionMode.PAST -> StatusChip("Completed", TextMuted)
                    else -> StatusChip("Booked", Green)
                }
            }
            Text(DateUtils.formatDateTime(booking.startTime), fontWeight = FontWeight.SemiBold)
            if (booking.notes.isNotBlank()) Text("Note: ${booking.notes}", color = TextMuted, style = MaterialTheme.typography.bodySmall)

            when (mode) {
                SessionMode.UPCOMING -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onReschedule) { Text("Reschedule", color = Green, fontWeight = FontWeight.SemiBold) }
                    TextButton(onClick = onCancel) { Text("Cancel", color = ErrorRed, fontWeight = FontWeight.SemiBold) }
                }
                SessionMode.PAST -> TextButton(onClick = onDelete) { Text("Delete record", color = ErrorRed, fontWeight = FontWeight.SemiBold) }
                SessionMode.TEACHING -> Unit
            }
        }
    }
}
