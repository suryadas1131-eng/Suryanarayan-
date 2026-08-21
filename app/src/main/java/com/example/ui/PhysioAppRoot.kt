package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import com.example.data.local.ArticleEntity
import com.example.data.local.ExerciseEntity
import com.example.ui.admin.AdminDashboardScreen
import com.example.ui.patient.*
import com.example.ui.theme.PhysioTealDark
import com.example.ui.theme.PhysioTealPrimary
import com.example.ui.viewmodel.AppMode
import com.example.ui.viewmodel.PhysioViewModel

sealed class PatientNavScreen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : PatientNavScreen("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Appointments : PatientNavScreen("appointments", "Bookings", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth)
    object Exercises : PatientNavScreen("exercises", "Exercises", Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter)
    object Articles : PatientNavScreen("articles", "Articles", Icons.Filled.MenuBook, Icons.Outlined.MenuBook)
    object Profile : PatientNavScreen("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun PhysioAppRoot(viewModel: PhysioViewModel) {
    val appMode by viewModel.appMode.collectAsState()
    val isAdminAuth by viewModel.isAdminAuthenticated.collectAsState()

    var showAdminLoginDialog by remember { mutableStateOf(false) }

    if (appMode == AppMode.ADMIN_APP && isAdminAuth) {
        // Render Admin Control Application
        AdminDashboardScreen(
            viewModel = viewModel,
            onExitAdminMode = {
                viewModel.switchToPatientMode()
            }
        )
    } else {
        // Render Patient Mobile Application
        PatientAppScaffold(
            viewModel = viewModel,
            onOpenAdminModeDialog = { showAdminLoginDialog = true }
        )
    }

    // Admin Access Security Dialog
    if (showAdminLoginDialog) {
        var adminPasscode by remember { mutableStateOf("") }
        var showPassword by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showAdminLoginDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = PhysioTealPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Admin Control Portal", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter Doctor / Administrator password to manage content, bookings, articles and 3D exercises.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = adminPasscode,
                        onValueChange = {
                            adminPasscode = it
                            errorMessage = null
                        },
                        label = { Text("Admin Password") },
                        singleLine = true,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showPassword) "Hide password" else "Show password"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (errorMessage != null) {
                        Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (viewModel.loginAdmin(adminPasscode)) {
                            viewModel.switchToAdminMode()
                            showAdminLoginDialog = false
                        } else {
                            errorMessage = "Invalid admin password. Please try again."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PhysioTealPrimary)
                ) {
                    Text("Enter Portal")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminLoginDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PatientAppScaffold(
    viewModel: PhysioViewModel,
    onOpenAdminModeDialog: () -> Unit
) {
    var currentScreen by remember { mutableStateOf<PatientNavScreen>(PatientNavScreen.Home) }
    var selectedExerciseForDetail by remember { mutableStateOf<ExerciseEntity?>(null) }
    var selectedArticleForDetail by remember { mutableStateOf<ArticleEntity?>(null) }
    var openBookingOnAppointmentsLaunch by remember { mutableStateOf(false) }

    val navItems = listOf(
        PatientNavScreen.Home,
        PatientNavScreen.Appointments,
        PatientNavScreen.Exercises,
        PatientNavScreen.Articles,
        PatientNavScreen.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                navItems.forEach { screen ->
                    val isSelected = currentScreen.route == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            currentScreen = screen
                            openBookingOnAppointmentsLaunch = false
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PhysioTealDark,
                            selectedTextColor = PhysioTealDark,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (currentScreen) {
                PatientNavScreen.Home -> PatientHomeScreen(
                    viewModel = viewModel,
                    onNavigateToBookVisit = {
                        openBookingOnAppointmentsLaunch = true
                        currentScreen = PatientNavScreen.Appointments
                    },
                    onNavigateToExercises = {
                        selectedExerciseForDetail = null
                        currentScreen = PatientNavScreen.Exercises
                    },
                    onNavigateToArticles = {
                        selectedArticleForDetail = null
                        currentScreen = PatientNavScreen.Articles
                    },
                    onNavigateToDoctorProfile = {
                        currentScreen = PatientNavScreen.Profile
                    },
                    onOpenNotifications = {
                        currentScreen = PatientNavScreen.Profile
                    },
                    onOpenAdminModeDialog = onOpenAdminModeDialog,
                    onOpenArticleDetail = { article ->
                        selectedArticleForDetail = article
                        currentScreen = PatientNavScreen.Articles
                    },
                    onOpenExerciseDetail = { exercise ->
                        selectedExerciseForDetail = exercise
                        currentScreen = PatientNavScreen.Exercises
                    }
                )

                PatientNavScreen.Appointments -> PatientAppointmentsScreen(
                    viewModel = viewModel,
                    openBookingOnLaunch = openBookingOnAppointmentsLaunch,
                    onOpenAdminModeDialog = onOpenAdminModeDialog
                )

                PatientNavScreen.Exercises -> PatientExercisesScreen(
                    viewModel = viewModel,
                    selectedExerciseToOpen = selectedExerciseForDetail,
                    onOpenAdminModeDialog = onOpenAdminModeDialog
                )

                PatientNavScreen.Articles -> PatientArticlesScreen(
                    viewModel = viewModel,
                    selectedArticleToOpen = selectedArticleForDetail,
                    onOpenAdminModeDialog = onOpenAdminModeDialog
                )

                PatientNavScreen.Profile -> PatientProfileScreen(
                    viewModel = viewModel,
                    onOpenAdminModeDialog = onOpenAdminModeDialog
                )
            }
        }
    }
}
