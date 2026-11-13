package com.studyconnect.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.studyconnect.app.ui.AppViewModelProvider
import com.studyconnect.app.ui.components.AppTextField
import com.studyconnect.app.ui.components.ErrorBanner
import com.studyconnect.app.ui.components.GradientHeader
import com.studyconnect.app.ui.components.PrimaryButton
import com.studyconnect.app.ui.components.ScreenHeader
import com.studyconnect.app.ui.theme.Background
import com.studyconnect.app.ui.theme.Green
import com.studyconnect.app.ui.theme.GreenDark
import com.studyconnect.app.ui.theme.GreenLight
import com.studyconnect.app.ui.theme.TextMuted
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinished: (loggedIn: Boolean) -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    LaunchedEffect(Unit) {
        delay(1400)
        onFinished(viewModel.isLoggedIn())
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(GreenLight, GreenDark))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(120.dp).clip(CircleShape).background(Color.White),
                contentAlignment = Alignment.Center
            ) { Text("\uD83C\uDF93", fontSize = 56.sp) }
            Spacer(Modifier.height(20.dp))
            Text("StudyConnect", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
            Text("Learn together. Grow together.", color = Color.White.copy(alpha = 0.85f))
        }
    }
}

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onRegister: () -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var identifier by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(state.success) { if (state.success) onLoggedIn() }

    Column(
        Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {
        GradientHeader {
            Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(84.dp).clip(CircleShape).background(Color.White),
                    contentAlignment = Alignment.Center
                ) { Text("\uD83C\uDF93", fontSize = 40.sp) }
                Spacer(Modifier.height(12.dp))
                Text("StudyConnect", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                Text("Find a peer who gets it.", color = Color.White.copy(alpha = 0.85f))
            }
        }
        Column(Modifier.padding(24.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Welcome Back", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            state.generalError?.let { ErrorBanner(it) }
            AppTextField(
                value = identifier, onValueChange = { identifier = it }, label = "Email or Admission number",
                leadingIcon = Icons.Filled.Person, keyboardType = KeyboardType.Text, error = state.emailError
            )
            AppTextField(
                value = password, onValueChange = { password = it }, label = "Password",
                leadingIcon = Icons.Filled.Lock, isPassword = true, error = state.passwordError,
                imeAction = ImeAction.Done, onDone = { viewModel.login(identifier, password) }
            )
            Spacer(Modifier.height(4.dp))
            PrimaryButton("Log in", onClick = { viewModel.login(identifier, password) }, loading = state.loading)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("Don't have an account?", color = TextMuted)
                TextButton(onClick = onRegister) { Text("Sign up", color = Green, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var name by rememberSaveable { mutableStateOf("") }
    var admission by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(state.success) { if (state.success) onRegistered() }

    Column(
        Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {
        ScreenHeader(title = "Create account", subtitle = "Join your campus study community", onBack = onBack)
        Column(Modifier.padding(24.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            state.generalError?.let { ErrorBanner(it) }
            AppTextField(name, { name = it }, "Full name", leadingIcon = Icons.Filled.Person, error = state.nameError)
            AppTextField(admission, { admission = it }, "Admission number", error = state.admissionError)
            AppTextField(
                email, { email = it }, "Email", leadingIcon = Icons.Filled.Email,
                keyboardType = KeyboardType.Email, error = state.emailError
            )
            AppTextField(password, { password = it }, "Password", leadingIcon = Icons.Filled.Lock, isPassword = true, error = state.passwordError)
            AppTextField(
                confirm, { confirm = it }, "Confirm password", leadingIcon = Icons.Filled.Lock, isPassword = true,
                error = state.confirmError, imeAction = ImeAction.Done,
                onDone = { viewModel.register(name, admission, email, password, confirm) }
            )
            Spacer(Modifier.height(4.dp))
            PrimaryButton(
                "Sign up",
                onClick = { viewModel.register(name, admission, email, password, confirm) },
                loading = state.loading
            )
        }
    }
}
