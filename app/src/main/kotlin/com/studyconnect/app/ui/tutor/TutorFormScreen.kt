package com.studyconnect.app.ui.tutor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.studyconnect.app.ui.AppViewModelProvider
import com.studyconnect.app.ui.components.AppTextField
import com.studyconnect.app.ui.components.ErrorBanner
import com.studyconnect.app.ui.components.PrimaryButton
import com.studyconnect.app.ui.components.ScreenHeader
import com.studyconnect.app.ui.theme.Background
import com.studyconnect.app.ui.theme.ErrorRed
import com.studyconnect.app.ui.theme.Green
import com.studyconnect.app.util.Subjects

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TutorFormScreen(
    onBack: () -> Unit,
    viewModel: TutorFormViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) { if (state.saved) onBack() }

    Column(Modifier.fillMaxSize().background(Background)) {
        ScreenHeader(
            title = if (state.isEdit) "Edit tutor profile" else "Become a tutor",
            subtitle = "Help classmates with a unit you know well",
            onBack = onBack
        )
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(20.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            state.generalError?.let { ErrorBanner(it) }

            AppTextField(state.name, viewModel::onName, "Display name", leadingIcon = Icons.Filled.Person, error = state.nameError)

            Text("Subject", fontWeight = FontWeight.SemiBold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Subjects.all.forEach { subject ->
                    FilterChip(
                        selected = state.subject == subject.name,
                        onClick = { viewModel.onSubject(subject.name) },
                        label = { Text("${subject.emoji} ${subject.name}") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Green, selectedLabelColor = Color.White)
                    )
                }
            }
            state.subjectError?.let { Text(it, color = ErrorRed, style = MaterialTheme.typography.bodySmall) }

            AppTextField(
                state.rating, viewModel::onRating, "Rating (0 - 5)", leadingIcon = Icons.Filled.Star,
                keyboardType = KeyboardType.Decimal, error = state.ratingError
            )
            AppTextField(
                state.bio, viewModel::onBio, "About you (optional)", singleLine = false, minLines = 3,
                error = state.bioError, imeAction = ImeAction.Default
            )

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Available for sessions", fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = state.available,
                    onCheckedChange = viewModel::onAvailable,
                    colors = SwitchDefaults.colors(checkedTrackColor = Green)
                )
            }
            Spacer(Modifier.height(6.dp))
            PrimaryButton(
                text = if (state.isEdit) "Save changes" else "Create profile",
                onClick = viewModel::save,
                loading = state.saving
            )
        }
    }
}
