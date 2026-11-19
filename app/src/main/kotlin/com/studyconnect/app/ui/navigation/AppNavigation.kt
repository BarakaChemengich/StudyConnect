package com.studyconnect.app.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.studyconnect.app.ui.AppViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.studyconnect.app.ui.auth.LoginScreen
import com.studyconnect.app.ui.auth.RegisterScreen
import com.studyconnect.app.ui.auth.SplashScreen
import com.studyconnect.app.ui.booking.BookSessionScreen
import com.studyconnect.app.ui.booking.SessionsScreen
import com.studyconnect.app.ui.home.HomeScreen
import com.studyconnect.app.ui.home.HomeViewModel
import com.studyconnect.app.ui.profile.ProfileScreen
import com.studyconnect.app.ui.resources.ResourcesScreen
import com.studyconnect.app.ui.subjects.SubjectsScreen
import com.studyconnect.app.ui.theme.Background
import com.studyconnect.app.ui.theme.Green

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val SESSIONS = "sessions"
    const val RESOURCES = "resources"
    const val PROFILE = "profile"
    const val SUBJECTS = "subjects"

    const val TUTOR_DETAIL = "tutor/{tutorId}"
    const val TUTOR_FORM = "tutor_form?tutorId={tutorId}"
    const val BOOK = "book/{tutorId}?bookingId={bookingId}"

    fun tutorDetail(tutorId: Long) = "tutor/$tutorId"
    fun tutorForm(tutorId: Long = -1L) = "tutor_form?tutorId=$tutorId"
    fun book(tutorId: Long, bookingId: Long = -1L) = "book/$tutorId?bookingId=$bookingId"
}

private data class BottomTab(val route: String, val label: String, val icon: ImageVector)

// Defines the four main destinations shown in the bottom navigation bar.
private val tabs = listOf(
    BottomTab(Routes.HOME, "Home", Icons.Filled.Home),
    BottomTab(Routes.SESSIONS, "Sessions", Icons.Filled.DateRange),
    BottomTab(Routes.RESOURCES, "Resources", Icons.Filled.Search),
    BottomTab(Routes.PROFILE, "Profile", Icons.Filled.Person)
)

// Controls navigation between the different screens of StudyConnect.
// Also manages the bottom navigation bar and screen transitions.
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        containerColor = Background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            // The bottom bar only exists on the four main screens.
            if (currentRoute in tabs.map { it.route }) {
                NavigationBar(containerColor = Color.White) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = { navController.navigateToTab(tab.route) },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Green,
                                indicatorColor = Green
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        // Defines the navigation graph and connects each route to its corresponding screen.
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(onFinished = { loggedIn ->
                    navController.navigate(if (loggedIn) Routes.HOME else Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                })
            }
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoggedIn = {
                        navController.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
                    },
                    onRegister = { navController.navigate(Routes.REGISTER) }
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    onRegistered = {
                        navController.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.HOME) {
                HomeScreen(
                    onTutorClick = { navController.navigate(Routes.tutorDetail(it)) },
                    onBecomeTutor = { navController.navigate(Routes.tutorForm()) },
                    onOpenSessions = { navController.navigateToTab(Routes.SESSIONS) },
                    onSeeAllSubjects = { navController.navigate(Routes.SUBJECTS) }
                )
            }
            composable(Routes.SUBJECTS) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Routes.HOME)
                }
                val homeViewModel: HomeViewModel = viewModel(
                    parentEntry,
                    factory = AppViewModelProvider.Factory
                )
                SubjectsScreen(
                    onBack = { navController.popBackStack() },
                    onSubjectSelect = { subject ->
                        homeViewModel.onSubjectSelected(subject)
                        navController.popBackStack()
                    }
                )
            }
            composable(Routes.SESSIONS) {
                SessionsScreen(
                    onReschedule = { tutorId, bookingId -> navController.navigate(Routes.book(tutorId, bookingId)) },
                    onFindTutor = { navController.navigateToTab(Routes.HOME) }
                )
            }
            composable(Routes.RESOURCES) { ResourcesScreen() }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onAddTutor = { navController.navigate(Routes.tutorForm()) },
                    onEditTutor = { navController.navigate(Routes.tutorForm(it)) },
                    onLoggedOut = {
                        navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                    }
                )
            }

            composable(
                route = Routes.TUTOR_DETAIL,
                arguments = listOf(navArgument("tutorId") { type = NavType.LongType })
            ) {
                com.studyconnect.app.ui.tutor.TutorDetailScreen(
                    onBack = { navController.popBackStack() },
                    onBook = { navController.navigate(Routes.book(it)) },
                    onEdit = { navController.navigate(Routes.tutorForm(it)) }
                )
            }
            composable(
                route = Routes.TUTOR_FORM,
                arguments = listOf(navArgument("tutorId") { type = NavType.LongType; defaultValue = -1L })
            ) {
                com.studyconnect.app.ui.tutor.TutorFormScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = Routes.BOOK,
                arguments = listOf(
                    navArgument("tutorId") { type = NavType.LongType },
                    navArgument("bookingId") { type = NavType.LongType; defaultValue = -1L }
                )
            ) {
                BookSessionScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(Routes.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
