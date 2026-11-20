package com.studyconnect.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.studyconnect.app.data.local.Tutor
import com.studyconnect.app.ui.AppViewModelProvider
import com.studyconnect.app.ui.components.AppTextField
import com.studyconnect.app.ui.components.EmptyState
import com.studyconnect.app.ui.components.GradientHeader
import com.studyconnect.app.ui.components.PrimaryButton
import com.studyconnect.app.ui.components.SecondaryButton
import com.studyconnect.app.ui.components.SectionTitle
import com.studyconnect.app.ui.components.SubjectAvatar
import com.studyconnect.app.ui.theme.Background
import com.studyconnect.app.ui.theme.ErrorRed
import com.studyconnect.app.ui.theme.Green
import com.studyconnect.app.ui.theme.TextMuted
import com.studyconnect.app.util.Subjects

@Composable
fun ProfileScreen(
    onAddTutor: () -> Unit,
    onEditTutor: (Long) -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val tutors by viewModel.myTutors.collectAsStateWithLifecycle()
    val sessionCount by viewModel.sessionCount.collectAsStateWithLifecycle()
    val profileMessage by viewModel.profileMessage.collectAsStateWithLifecycle()

    var toDelete by remember { mutableStateOf<Tutor?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(profileMessage) {
        profileMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearProfileMessage()
            if (it == "Profile updated successfully") {
                showEditDialog = false
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                GradientHeader {
                    Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Box(Modifier.size(64.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                            Text(
                                user?.name?.firstOrNull()?.uppercase() ?: "?",
                                fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Green
                            )
                        }
                        Column {
                            Text(user?.name ?: "", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(user?.email ?: "", color = Color.White.copy(alpha = 0.85f))
                            Text("Adm. no: ${user?.admissionNo ?: ""}", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            item {
                Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Tutor profiles", tutors.size.toString(), Modifier.weight(1f))
                    StatCard("Upcoming sessions", sessionCount.toString(), Modifier.weight(1f))
                }
            }

            item { SectionTitle("My tutor profiles", Modifier.padding(horizontal = 16.dp)) }

            if (tutors.isEmpty()) {
                item { EmptyState("\uD83C\uDF93", "You are not tutoring yet", "Good at a unit? Create a profile and help a classmate.") }
            } else {
                items(tutors, key = { it.id }) { tutor ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SubjectAvatar(tutor.subject, size = 44)
                            Column(Modifier.weight(1f)) {
                                Text("${Subjects.emoji(tutor.subject)} ${tutor.subject}", fontWeight = FontWeight.Bold)
                                Text(if (tutor.available) "Available" else "Busy", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(
                                checked = tutor.available,
                                onCheckedChange = { viewModel.setAvailability(tutor, it) },
                                colors = SwitchDefaults.colors(checkedTrackColor = Green)
                            )
                            IconButton(onClick = { onEditTutor(tutor.id) }) { Icon(Icons.Filled.Edit, "Edit", tint = Green) }
                            IconButton(onClick = { toDelete = tutor }) { Icon(Icons.Filled.Delete, "Delete", tint = ErrorRed) }
                        }
                    }
                }
            }

            item { PrimaryButton("Add tutor profile", onClick = onAddTutor, modifier = Modifier.padding(horizontal = 16.dp)) }
            item { SecondaryButton("Send test reminder", onClick = viewModel::sendTestReminder, modifier = Modifier.padding(horizontal = 16.dp)) }

            item {
                SecondaryButton(
                    text = "Edit Profile",
                    onClick = { showEditDialog = true },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                SecondaryButton(
                    text = "Delete Account",
                    onClick = { showDeleteAccountDialog = true },
                    modifier = Modifier.padding(horizontal = 16.dp),
                    destructive = true
                )
            }

            item {
                SecondaryButton(
                    "Log out",
                    onClick = { viewModel.logout(); onLoggedOut() },
                    modifier = Modifier.padding(horizontal = 16.dp),
                    destructive = true
                )
            }
        }
    }

    toDelete?.let { tutor ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Delete ${tutor.subject} profile?") },
            text = { Text("Sessions booked with this profile will be removed too.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteTutor(tutor); toDelete = null }) { Text("Delete", color = ErrorRed) }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Keep") } }
        )
    }

    if (showEditDialog) {
        var editName by remember { mutableStateOf(user?.name ?: "") }
        var editEmail by remember { mutableStateOf(user?.email ?: "") }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AppTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = "Full Name",
                        leadingIcon = Icons.Filled.Person
                    )
                    AppTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = "Email",
                        leadingIcon = Icons.Filled.Email,
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done,
                        onDone = { viewModel.updateProfile(editName, editEmail) }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.updateProfile(editName, editEmail) }) {
                    Text("Save", color = Green)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete Account?") },
            text = { Text("This will permanently delete your account, your tutor profiles, and all your booked sessions. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteAccountDialog = false
                    viewModel.deleteAccount {
                        onLoggedOut()
                    }
                }) { Text("Delete My Account", color = ErrorRed) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Green)
            Spacer(Modifier.height(2.dp))
            Text(label, color = TextMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}