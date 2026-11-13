package com.studyconnect.app.ui.tutor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.studyconnect.app.ui.AppViewModelProvider
import com.studyconnect.app.ui.components.LoadingState
import com.studyconnect.app.ui.components.PrimaryButton
import com.studyconnect.app.ui.components.ScreenHeader
import com.studyconnect.app.ui.components.SecondaryButton
import com.studyconnect.app.ui.components.StatusChip
import com.studyconnect.app.ui.components.SubjectAvatar
import com.studyconnect.app.ui.theme.Amber
import com.studyconnect.app.ui.theme.Background
import com.studyconnect.app.ui.theme.Green
import com.studyconnect.app.ui.theme.TextMuted

@Composable
fun TutorDetailScreen(
    onBack: () -> Unit,
    onBook: (Long) -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: TutorDetailViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val tutor by viewModel.tutor.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(Background)) {
        ScreenHeader(title = "Tutor profile", onBack = onBack)

        val current = tutor
        if (current == null) {
            LoadingState()
        } else {
            val isOwner = current.ownerId == viewModel.currentUserId
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp).navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        SubjectAvatar(current.subject, size = 84)
                        Spacer(Modifier.height(10.dp))
                        Text(current.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(current.subject, color = TextMuted)
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Filled.Star, null, tint = Amber, modifier = Modifier.size(20.dp))
                            Text("%.1f".format(current.rating), fontWeight = FontWeight.Bold)
                            StatusChip(if (current.available) "Available" else "Busy", if (current.available) Green else TextMuted)
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("About", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            current.bio.ifBlank { "This tutor has not added a description yet." },
                            color = TextMuted
                        )
                    }
                }

                if (isOwner) {
                    Text("This is your own tutor profile.", color = TextMuted)
                    PrimaryButton("Edit profile", onClick = { onEdit(current.id) })
                    SecondaryButton("Delete profile", onClick = { confirmDelete = true }, destructive = true)
                } else {
                    PrimaryButton(
                        text = if (current.available) "Book a session" else "Currently unavailable",
                        onClick = { onBook(current.id) },
                        enabled = current.available
                    )
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete tutor profile?") },
            text = { Text("This also removes every session booked with this profile. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.delete(onDone = onBack)
                }) { Text("Delete", color = Color(0xFFD93025)) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Keep") } }
        )
    }
}
