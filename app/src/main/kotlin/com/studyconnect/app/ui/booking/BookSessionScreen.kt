package com.studyconnect.app.ui.booking

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.studyconnect.app.ui.AppViewModelProvider
import com.studyconnect.app.ui.components.AppTextField
import com.studyconnect.app.ui.components.ErrorBanner
import com.studyconnect.app.ui.components.LoadingState
import com.studyconnect.app.ui.components.PrimaryButton
import com.studyconnect.app.ui.components.ScreenHeader
import com.studyconnect.app.ui.components.SubjectAvatar
import com.studyconnect.app.ui.theme.Background
import com.studyconnect.app.ui.theme.ErrorRed
import com.studyconnect.app.ui.theme.Green
import com.studyconnect.app.ui.theme.TextMuted
import com.studyconnect.app.util.DateUtils
import java.util.Calendar

@Composable
fun BookSessionScreen(
    onBack: () -> Unit,
    viewModel: BookSessionViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(state.done) { if (state.done) onBack() }

    Column(Modifier.fillMaxSize().background(Background)) {
        ScreenHeader(
            title = if (state.isReschedule) "Reschedule session" else "Book a session",
            subtitle = "Sessions last one hour",
            onBack = onBack
        )

        val tutor = state.tutor
        if (tutor == null) {
            LoadingState()
        } else {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(20.dp).navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        SubjectAvatar(tutor.subject)
                        Column {
                            Text(tutor.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(tutor.subject, color = TextMuted)
                        }
                    }
                }

                state.generalError?.let { ErrorBanner(it) }

                Text("When?", fontWeight = FontWeight.SemiBold)
                OutlinedButton(
                    onClick = { showDateTimePicker(context, state.startTime, viewModel::onTimePicked) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.DateRange, contentDescription = null, tint = Green)
                    Spacer(Modifier.padding(horizontal = 6.dp))
                    Text(
                        state.startTime?.let { DateUtils.formatDateTime(it) } ?: "Pick date and time",
                        color = if (state.startTime == null) TextMuted else Color.Unspecified
                    )
                }
                state.timeError?.let { Text(it, color = ErrorRed, style = MaterialTheme.typography.bodySmall) }

                AppTextField(
                    value = state.notes,
                    onValueChange = viewModel::onNotes,
                    label = "What do you need help with? (optional)",
                    singleLine = false,
                    minLines = 3,
                    error = state.notesError,
                    imeAction = ImeAction.Default
                )

                Text(
                    "You will get a reminder notification 30 minutes before the session.",
                    color = TextMuted, style = MaterialTheme.typography.bodySmall
                )
                PrimaryButton(
                    text = if (state.isReschedule) "Save new time" else "Confirm booking",
                    onClick = viewModel::confirm,
                    loading = state.saving
                )
            }
        }
    }
}

/** Date picker, then time picker. Only future dates can be chosen. */
private fun showDateTimePicker(context: Context, initial: Long?, onPicked: (Long) -> Unit) {
    val base = Calendar.getInstance().apply { if (initial != null) timeInMillis = initial }
    val datePicker = DatePickerDialog(
        context,
        { _, year, month, day ->
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    val chosen = Calendar.getInstance().apply {
                        set(year, month, day, hour, minute, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    onPicked(chosen.timeInMillis)
                },
                base.get(Calendar.HOUR_OF_DAY),
                base.get(Calendar.MINUTE),
                true
            ).show()
        },
        base.get(Calendar.YEAR),
        base.get(Calendar.MONTH),
        base.get(Calendar.DAY_OF_MONTH)
    )
    datePicker.datePicker.minDate = System.currentTimeMillis() - 1_000
    datePicker.show()
}
