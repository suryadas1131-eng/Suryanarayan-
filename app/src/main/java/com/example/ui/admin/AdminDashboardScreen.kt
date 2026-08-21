package com.example.ui.admin

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.PhysioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: PhysioViewModel,
    onExitAdminMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appointments by viewModel.allAppointments.collectAsState()
    val articles by viewModel.allArticles.collectAsState()
    val exercises by viewModel.allExercises.collectAsState()
    val services by viewModel.allServices.collectAsState()
    val assignedExercises by viewModel.allAssignedExercises.collectAsState()
    val doctor by viewModel.doctorProfile.collectAsState()
    val homeConfig by viewModel.homePageConfig.collectAsState()
    val workingHours by viewModel.workingHours.collectAsState()
    val activityLogs by viewModel.recentActivityLogs.collectAsState()

    var selectedAdminTab by remember { mutableIntStateOf(0) }
    val adminTabs = listOf("Bookings", "Prescribe", "Exercises", "Articles", "Services", "Doctor Profile", "Audit Logs")

    // State for modals & editors
    var editingArticle by remember { mutableStateOf<ArticleEntity?>(null) }
    var isNewArticle by remember { mutableStateOf(false) }

    var editingExercise by remember { mutableStateOf<ExerciseEntity?>(null) }
    var isNewExercise by remember { mutableStateOf(false) }

    var editingService by remember { mutableStateOf<ServiceEntity?>(null) }
    var isNewService by remember { mutableStateOf(false) }

    var showAssignExerciseSheet by remember { mutableStateOf(false) }
    var showBroadcastNotifDialog by remember { mutableStateOf(false) }
    var showEditDoctorProfileSheet by remember { mutableStateOf(false) }
    var showEditHomeConfigSheet by remember { mutableStateOf(false) }

    // Appointment Action Dialog
    var rescheduleAppointmentTarget by remember { mutableStateOf<AppointmentEntity?>(null) }
    var cancelAppointmentTarget by remember { mutableStateOf<AppointmentEntity?>(null) }

    val pendingCount = appointments.count { it.status == "PENDING" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE53935)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("PhysioCare Admin Portal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Live Control Center", style = MaterialTheme.typography.labelSmall, color = PhysioAccentGreen)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showBroadcastNotifDialog = true }) {
                        Icon(Icons.Default.Campaign, contentDescription = "Broadcast Notification", tint = Color(0xFFFFB300))
                    }
                    TextButton(onClick = onExitAdminMode) {
                        Text("Exit Admin", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Live Status Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = PhysioTealContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(PhysioAccentGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Connected & Live Syncing", color = PhysioTealDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text("${appointments.size} Total Bookings", color = PhysioTealPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Scrollable Admin Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedAdminTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                adminTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedAdminTab == index,
                        onClick = { selectedAdminTab = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedAdminTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                                if (title == "Bookings" && pendingCount > 0) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFE53935), CircleShape)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("$pendingCount", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    )
                }
            }

            // Tab Content
            when (selectedAdminTab) {
                0 -> AdminBookingsTab(
                    appointments = appointments,
                    onConfirm = { viewModel.confirmAppointment(it) },
                    onRescheduleClick = { rescheduleAppointmentTarget = it },
                    onCancelClick = { cancelAppointmentTarget = it },
                    onComplete = { viewModel.completeAppointment(it) }
                )
                1 -> AdminPrescriptionsTab(
                    assignedExercises = assignedExercises,
                    onAddNewPrescription = { showAssignExerciseSheet = true },
                    onDeletePrescription = { viewModel.removeAssignedExercise(it) }
                )
                2 -> AdminExercisesTab(
                    exercises = exercises,
                    onAddNewExercise = {
                        editingExercise = ExerciseEntity(name = "", bodyPart = "Knee", model3dType = "KNEE_EXTENSION")
                        isNewExercise = true
                    },
                    onEditExercise = {
                        editingExercise = it
                        isNewExercise = false
                    },
                    onTogglePublish = { viewModel.toggleExercisePublish(it) },
                    onDeleteExercise = { viewModel.deleteExercise(it) }
                )
                3 -> AdminArticlesTab(
                    articles = articles,
                    onAddNewArticle = {
                        editingArticle = ArticleEntity(title = "", category = "Back Pain", content = "")
                        isNewArticle = true
                    },
                    onEditArticle = {
                        editingArticle = it
                        isNewArticle = false
                    },
                    onTogglePublish = { viewModel.toggleArticlePublish(it) },
                    onDeleteArticle = { viewModel.deleteArticle(it) }
                )
                4 -> AdminServicesTab(
                    services = services,
                    homeConfig = homeConfig ?: HomePageConfigEntity(),
                    onAddNewService = {
                        editingService = ServiceEntity(name = "", description = "")
                        isNewService = true
                    },
                    onEditService = {
                        editingService = it
                        isNewService = false
                    },
                    onToggleService = { viewModel.toggleServiceStatus(it) },
                    onDeleteService = { viewModel.deleteService(it) },
                    onEditHomeConfig = { showEditHomeConfigSheet = true }
                )
                5 -> AdminDoctorProfileTab(
                    doctor = doctor ?: DoctorProfileEntity(),
                    workingHours = workingHours,
                    onEditProfile = { showEditDoctorProfileSheet = true },
                    onUpdateWorkingHours = { viewModel.updateWorkingHours(it) }
                )
                6 -> AdminAuditLogsTab(activityLogs = activityLogs)
            }
        }
    }

    // Modal: Reschedule Appointment Dialog
    rescheduleAppointmentTarget?.let { appt ->
        var newDate by remember { mutableStateOf("Saturday (Aug 22)") }
        var newTime by remember { mutableStateOf("11:30 AM - 12:30 PM") }

        AlertDialog(
            onDismissRequest = { rescheduleAppointmentTarget = null },
            title = { Text("Reschedule Booking (${appt.bookingId})", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Patient: ${appt.patientName}", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(value = newDate, onValueChange = { newDate = it }, label = { Text("New Date") })
                    OutlinedTextField(value = newTime, onValueChange = { newTime = it }, label = { Text("New Time Slot") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rescheduleAppointment(appt, newDate, newTime)
                        rescheduleAppointmentTarget = null
                        Toast.makeText(context, "Appointment rescheduled & patient notified!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PhysioTealPrimary)
                ) {
                    Text("Save & Notify Patient")
                }
            },
            dismissButton = {
                TextButton(onClick = { rescheduleAppointmentTarget = null }) { Text("Cancel") }
            }
        )
    }

    // Modal: Cancel Appointment Dialog
    cancelAppointmentTarget?.let { appt ->
        var cancelReason by remember { mutableStateOf("Doctor unavailable due to emergency hospital duty.") }

        AlertDialog(
            onDismissRequest = { cancelAppointmentTarget = null },
            title = { Text("Cancel Appointment (${appt.bookingId})", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Patient: ${appt.patientName}")
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = { Text("Reason for Cancellation") },
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelAppointment(appt, cancelReason)
                        cancelAppointmentTarget = null
                        Toast.makeText(context, "Appointment cancelled & patient notified.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirm Cancellation")
                }
            },
            dismissButton = {
                TextButton(onClick = { cancelAppointmentTarget = null }) { Text("Back") }
            }
        )
    }

    // Modal: Broadcast Notification Dialog
    if (showBroadcastNotifDialog) {
        var notifTitle by remember { mutableStateOf("Holiday Notice / Clinic Update") }
        var notifMessage by remember { mutableStateOf("Dr. Satyaprakash Das will be available for home visits this weekend. Book early to secure your slot.") }

        AlertDialog(
            onDismissRequest = { showBroadcastNotifDialog = false },
            title = { Text("Broadcast Push Notification to All Patients", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = notifTitle, onValueChange = { notifTitle = it }, label = { Text("Notification Title") })
                    OutlinedTextField(value = notifMessage, onValueChange = { notifMessage = it }, label = { Text("Message Body") }, minLines = 2)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (notifTitle.isNotBlank() && notifMessage.isNotBlank()) {
                            viewModel.broadcastPushNotification(notifTitle, notifMessage)
                            showBroadcastNotifDialog = false
                            Toast.makeText(context, "Broadcast sent to all patient apps!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PhysioTealPrimary)
                ) {
                    Text("Broadcast Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBroadcastNotifDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Modal: Assign Exercise Sheet
    if (showAssignExerciseSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAssignExerciseSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            AssignExerciseSheetContent(
                exercises = exercises,
                onAssign = { patientEmail, patientName, exercise, reps, sets, freq, date, notes ->
                    viewModel.assignExerciseToPatient(patientEmail, patientName, exercise, reps, sets, freq, date, notes)
                    showAssignExerciseSheet = false
                    Toast.makeText(context, "Exercise prescribed to $patientName!", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { showAssignExerciseSheet = false }
            )
        }
    }

    // Modal: Article Editor
    editingArticle?.let { article ->
        ModalBottomSheet(
            onDismissRequest = { editingArticle = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            ArticleEditorSheetContent(
                article = article,
                isNew = isNewArticle,
                onSave = { saved ->
                    viewModel.saveArticle(saved)
                    editingArticle = null
                    Toast.makeText(context, "Article saved successfully!", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { editingArticle = null }
            )
        }
    }

    // Modal: Exercise Editor
    editingExercise?.let { exercise ->
        ModalBottomSheet(
            onDismissRequest = { editingExercise = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            ExerciseEditorSheetContent(
                exercise = exercise,
                isNew = isNewExercise,
                onSave = { saved ->
                    viewModel.saveExercise(saved)
                    editingExercise = null
                    Toast.makeText(context, "Exercise saved successfully!", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { editingExercise = null }
            )
        }
    }

    // Modal: Service Editor
    editingService?.let { service ->
        ModalBottomSheet(
            onDismissRequest = { editingService = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            ServiceEditorSheetContent(
                service = service,
                isNew = isNewService,
                onSave = { saved ->
                    viewModel.saveService(saved)
                    editingService = null
                    Toast.makeText(context, "Service saved successfully!", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { editingService = null }
            )
        }
    }

    // Modal: Doctor Profile Editor
    if (showEditDoctorProfileSheet) {
        doctor?.let { doc ->
            ModalBottomSheet(
                onDismissRequest = { showEditDoctorProfileSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                DoctorProfileEditorSheetContent(
                    doctor = doc,
                    onSave = { updated ->
                        viewModel.updateDoctorProfile(updated)
                        showEditDoctorProfileSheet = false
                        Toast.makeText(context, "Doctor profile updated!", Toast.LENGTH_SHORT).show()
                    },
                    onDismiss = { showEditDoctorProfileSheet = false }
                )
            }
        }
    }

    // Modal: Homepage Layout Config Editor
    if (showEditHomeConfigSheet) {
        homeConfig?.let { config ->
            ModalBottomSheet(
                onDismissRequest = { showEditHomeConfigSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                HomeConfigEditorSheetContent(
                    config = config,
                    onSave = { updated ->
                        viewModel.updateHomePageConfig(updated)
                        showEditHomeConfigSheet = false
                        Toast.makeText(context, "Homepage layout updated!", Toast.LENGTH_SHORT).show()
                    },
                    onDismiss = { showEditHomeConfigSheet = false }
                )
            }
        }
    }
}

// Sub-Tab 1: Bookings Management
@Composable
private fun AdminBookingsTab(
    appointments: List<AppointmentEntity>,
    onConfirm: (AppointmentEntity) -> Unit,
    onRescheduleClick: (AppointmentEntity) -> Unit,
    onCancelClick: (AppointmentEntity) -> Unit,
    onComplete: (AppointmentEntity) -> Unit
) {
    if (appointments.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text("No appointment requests yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(appointments) { appt ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(appt.bookingId, fontWeight = FontWeight.Bold, color = PhysioTealDark)
                            StatusBadge(status = appt.status)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(appt.patientName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("${appt.serviceName} • ${appt.date} (${appt.timeSlot})", style = MaterialTheme.typography.bodyMedium, color = PhysioTealPrimary)
                        Text("Phone: ${appt.patientPhone} | Email: ${appt.patientEmail}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Address: ${appt.homeAddress}", style = MaterialTheme.typography.bodySmall)
                        if (appt.reason.isNotEmpty()) {
                            Text("Reason: ${appt.reason}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (appt.status == "PENDING") {
                                Button(
                                    onClick = { onConfirm(appt) },
                                    colors = ButtonDefaults.buttonColors(containerColor = PhysioAccentGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    Text("Confirm", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (appt.status == "CONFIRMED" || appt.status == "RESCHEDULED") {
                                Button(
                                    onClick = { onComplete(appt) },
                                    colors = ButtonDefaults.buttonColors(containerColor = PhysioTealDark),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    Text("Complete", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (appt.status != "COMPLETED" && appt.status != "CANCELLED") {
                                OutlinedButton(
                                    onClick = { onRescheduleClick(appt) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    Text("Reschedule", fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = { onCancelClick(appt) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    Text("Cancel", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Sub-Tab 2: Prescriptions
@Composable
private fun AdminPrescriptionsTab(
    assignedExercises: List<AssignedExerciseEntity>,
    onAddNewPrescription: () -> Unit,
    onDeletePrescription: (AssignedExerciseEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Assigned Patient Routines", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Button(
                onClick = onAddNewPrescription,
                colors = ButtonDefaults.buttonColors(containerColor = PhysioTealPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Prescribe New")
            }
        }

        if (assignedExercises.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("No prescriptions assigned yet. Tap 'Prescribe New' to assign exercises to a patient.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(assignedExercises) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.patientName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                IconButton(onClick = { onDeletePrescription(item) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                            Text("Exercise: ${item.exerciseName} (${item.bodyPart})", color = PhysioTealPrimary, fontWeight = FontWeight.SemiBold)
                            Text("Reps: ${item.repetitions} | Sets: ${item.duration} | Freq: ${item.frequency}", style = MaterialTheme.typography.bodySmall)
                            Text("Patient ID/Email: ${item.patientEmailOrPhone}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Doctor Note: ${item.physiotherapistInstructions}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

// Sub-Tab 3: Exercises Control
@Composable
private fun AdminExercisesTab(
    exercises: List<ExerciseEntity>,
    onAddNewExercise: () -> Unit,
    onEditExercise: (ExerciseEntity) -> Unit,
    onTogglePublish: (ExerciseEntity) -> Unit,
    onDeleteExercise: (ExerciseEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Exercises Library (${exercises.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Button(
                onClick = onAddNewExercise,
                colors = ButtonDefaults.buttonColors(containerColor = PhysioTealPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Exercise")
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(exercises) { ex ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                StatusBadge(status = ex.status)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(ex.bodyPart, style = MaterialTheme.typography.labelSmall, color = PhysioTealPrimary, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(ex.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("${ex.repetitions} • ${ex.duration} • 3D: ${ex.model3dType}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        IconButton(onClick = { onTogglePublish(ex) }) {
                            Icon(
                                imageVector = if (ex.status == "PUBLISHED") Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Publish",
                                tint = if (ex.status == "PUBLISHED") PhysioAccentGreen else MaterialTheme.colorScheme.outline
                            )
                        }

                        IconButton(onClick = { onEditExercise(ex) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PhysioTealPrimary)
                        }

                        IconButton(onClick = { onDeleteExercise(ex) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

// Sub-Tab 4: Articles Control
@Composable
private fun AdminArticlesTab(
    articles: List<ArticleEntity>,
    onAddNewArticle: () -> Unit,
    onEditArticle: (ArticleEntity) -> Unit,
    onTogglePublish: (ArticleEntity) -> Unit,
    onDeleteArticle: (ArticleEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Articles Manager (${articles.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Button(
                onClick = onAddNewArticle,
                colors = ButtonDefaults.buttonColors(containerColor = PhysioSecondary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Write Article")
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(articles) { art ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                StatusBadge(status = art.status)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(art.category, style = MaterialTheme.typography.labelSmall, color = PhysioSecondary, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(art.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text("By ${art.author} • ${art.readingTime}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        IconButton(onClick = { onTogglePublish(art) }) {
                            Icon(
                                imageVector = if (art.status == "PUBLISHED") Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle",
                                tint = if (art.status == "PUBLISHED") PhysioAccentGreen else MaterialTheme.colorScheme.outline
                            )
                        }

                        IconButton(onClick = { onEditArticle(art) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PhysioSecondary)
                        }

                        IconButton(onClick = { onDeleteArticle(art) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

// Sub-Tab 5: Services & Homepage Control
@Composable
private fun AdminServicesTab(
    services: List<ServiceEntity>,
    homeConfig: HomePageConfigEntity,
    onAddNewService: () -> Unit,
    onEditService: (ServiceEntity) -> Unit,
    onToggleService: (ServiceEntity) -> Unit,
    onDeleteService: (ServiceEntity) -> Unit,
    onEditHomeConfig: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PhysioTealContainer),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Homepage Layout & Visibility", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PhysioTealDark)
                        IconButton(onClick = onEditHomeConfig) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Homepage", tint = PhysioTealDark)
                        }
                    }
                    Text("Hero Title: ${homeConfig.heroHeading}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text("Hero Subtitle: ${homeConfig.heroSubtitle}", style = MaterialTheme.typography.bodySmall)
                    Text("Features Visible: Articles=${homeConfig.showFeaturedArticles}, Exercises=${homeConfig.showFeaturedExercises}, Testimonials=${homeConfig.showTestimonials}", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Clinical Services List (${services.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Button(
                    onClick = onAddNewService,
                    colors = ButtonDefaults.buttonColors(containerColor = PhysioTealPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Service")
                }
            }
        }

        items(services) { s ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(s.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(s.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                        Text("Duration: ${s.durationMinutes} mins • Status: ${if (s.isEnabled) "ACTIVE" else "DISABLED"}", style = MaterialTheme.typography.labelSmall, color = if (s.isEnabled) PhysioAccentGreen else MaterialTheme.colorScheme.error)
                    }

                    Switch(
                        checked = s.isEnabled,
                        onCheckedChange = { onToggleService(s) }
                    )

                    IconButton(onClick = { onEditService(s) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PhysioTealPrimary)
                    }

                    IconButton(onClick = { onDeleteService(s) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

// Sub-Tab 6: Doctor Profile & Working Hours
@Composable
private fun AdminDoctorProfileTab(
    doctor: DoctorProfileEntity,
    workingHours: List<WorkingHoursEntity>,
    onEditProfile: () -> Unit,
    onUpdateWorkingHours: (List<WorkingHoursEntity>) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Doctor Public Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = onEditProfile,
                            colors = ButtonDefaults.buttonColors(containerColor = PhysioTealPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Info")
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(doctor.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(doctor.qualifications, color = PhysioTealPrimary, fontWeight = FontWeight.SemiBold)
                    Text(doctor.experience, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Phone: ${doctor.phone} | WhatsApp: ${doctor.whatsapp} | Email: ${doctor.email}", style = MaterialTheme.typography.bodySmall)
                    Text("Coverage Area: ${doctor.serviceArea}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            Text("Weekly Working Hours & Availability", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(workingHours) { day ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(day.dayOfWeek, fontWeight = FontWeight.Bold)
                        Text(if (day.isOpen) "${day.openTime} - ${day.closeTime}" else "Closed", fontSize = 12.sp, color = if (day.isOpen) PhysioTealDark else MaterialTheme.colorScheme.error)
                    }
                    Switch(
                        checked = day.isOpen,
                        onCheckedChange = { isOpen ->
                            val updated = workingHours.map { if (it.dayOfWeek == day.dayOfWeek) it.copy(isOpen = isOpen) else it }
                            onUpdateWorkingHours(updated)
                        }
                    )
                }
            }
        }
    }
}

// Sub-Tab 7: Audit Logs
@Composable
private fun AdminAuditLogsTab(activityLogs: List<ActivityLogEntity>) {
    if (activityLogs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text("No activity logs recorded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(activityLogs) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PhysioTealContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, tint = PhysioTealDark, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(log.actionTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("By ${log.performedBy}", fontSize = 11.sp, color = PhysioTealPrimary, fontWeight = FontWeight.Medium)
                            }
                            Text(log.detail, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

// Editor Sheets:
@Composable
fun AssignExerciseSheetContent(
    exercises: List<ExerciseEntity>,
    onAssign: (String, String, ExerciseEntity, String, String, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var patientEmail by remember { mutableStateOf("suryadas1131@gmail.com") }
    var patientName by remember { mutableStateOf("Surya Das") }
    var selectedExercise by remember { mutableStateOf(exercises.firstOrNull() ?: ExerciseEntity()) }
    var repetitions by remember { mutableStateOf("12 Repetitions") }
    var duration by remember { mutableStateOf("3 Sets / 10 Mins") }
    var frequency by remember { mutableStateOf("Twice Daily") }
    var startDate by remember { mutableStateOf("Today (Aug 20)") }
    var notes by remember { mutableStateOf("Perform seated with straight posture. Stop if sharp pain occurs.") }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Prescribe Rehabilitation Routine", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        item { OutlinedTextField(value = patientName, onValueChange = { patientName = it }, label = { Text("Patient Name") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = patientEmail, onValueChange = { patientEmail = it }, label = { Text("Patient Email or Phone Identifier") }, modifier = Modifier.fillMaxWidth()) }
        item {
            Text("Select Exercise from Library", fontWeight = FontWeight.SemiBold)
            LazyColumn(modifier = Modifier.height(130.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(exercises) { ex ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedExercise.id == ex.id) PhysioTealContainer else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { selectedExercise = ex }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${ex.name} (${ex.bodyPart})", fontSize = 13.sp)
                        if (selectedExercise.id == ex.id) Icon(Icons.Default.Check, contentDescription = null, tint = PhysioTealPrimary)
                    }
                }
            }
        }
        item { OutlinedTextField(value = repetitions, onValueChange = { repetitions = it }, label = { Text("Repetitions (e.g. 15 reps)") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Sets / Duration (e.g. 3 sets)") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = frequency, onValueChange = { frequency = it }, label = { Text("Frequency (e.g. Daily morning & evening)") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Doctor's Clinical Instructions & Precautions") }, modifier = Modifier.fillMaxWidth(), minLines = 2) }

        item {
            Button(
                onClick = {
                    if (patientName.isNotBlank() && patientEmail.isNotBlank()) {
                        onAssign(patientEmail, patientName, selectedExercise, repetitions, duration, frequency, startDate, notes)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PhysioTealPrimary)
            ) {
                Text("Prescribe to Patient App")
            }
        }
    }
}

@Composable
fun ArticleEditorSheetContent(
    article: ArticleEntity,
    isNew: Boolean,
    onSave: (ArticleEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(article.title) }
    var category by remember { mutableStateOf(article.category) }
    var shortDesc by remember { mutableStateOf(article.shortDescription) }
    var content by remember { mutableStateOf(article.content) }
    var readingTime by remember { mutableStateOf(article.readingTime) }
    var status by remember { mutableStateOf(article.status) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(if (isNew) "Create New Article" else "Edit Article", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        item { OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Article Title") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category (e.g. Back Pain, Knee Rehab)") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = readingTime, onValueChange = { readingTime = it }, label = { Text("Reading Time (e.g. 4 min read)") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = shortDesc, onValueChange = { shortDesc = it }, label = { Text("Short Summary") }, modifier = Modifier.fillMaxWidth(), minLines = 2) }
        item { OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Full Article Content") }, modifier = Modifier.fillMaxWidth(), minLines = 6) }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Publish to Patient App immediately?", fontWeight = FontWeight.SemiBold)
                Switch(checked = status == "PUBLISHED", onCheckedChange = { status = if (it) "PUBLISHED" else "DRAFT" })
            }
        }
        item {
            Button(
                onClick = {
                    onSave(article.copy(title = title, category = category, shortDescription = shortDesc, content = content, readingTime = readingTime, status = status))
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PhysioTealPrimary)
            ) {
                Text("Save Article")
            }
        }
    }
}

@Composable
fun ExerciseEditorSheetContent(
    exercise: ExerciseEntity,
    isNew: Boolean,
    onSave: (ExerciseEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(exercise.name) }
    var bodyPart by remember { mutableStateOf(exercise.bodyPart) }
    var reps by remember { mutableStateOf(exercise.repetitions) }
    var duration by remember { mutableStateOf(exercise.duration) }
    var freq by remember { mutableStateOf(exercise.frequency) }
    var modelType by remember { mutableStateOf(exercise.model3dType) }
    var ytUrl by remember { mutableStateOf(exercise.youtubeUrl) }
    var instructions by remember { mutableStateOf(exercise.instructions) }
    var precautions by remember { mutableStateOf(exercise.precautions) }
    var mistakes by remember { mutableStateOf(exercise.commonMistakes) }
    var status by remember { mutableStateOf(exercise.status) }

    val modelOptions = listOf("KNEE_EXTENSION", "SPINE_FLEXION", "SHOULDER_ABDUCTION", "NECK_ROTATION", "ANKLE_DORSIFLEXION")

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(if (isNew) "Add Exercise to Library" else "Edit Exercise", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        item { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Exercise Name") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = bodyPart, onValueChange = { bodyPart = it }, label = { Text("Body Part (Knee, Back, Shoulder, Neck, Ankle)") }, modifier = Modifier.fillMaxWidth()) }
        item {
            Text("3D Biomechanics Anatomy Model Type:", fontWeight = FontWeight.SemiBold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                modelOptions.take(3).forEach { m ->
                    FilterChip(
                        selected = modelType == m,
                        onClick = { modelType = m },
                        label = { Text(m.replace("_", " "), fontSize = 11.sp) }
                    )
                }
            }
        }
        item { OutlinedTextField(value = reps, onValueChange = { reps = it }, label = { Text("Default Repetitions") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Default Sets / Hold Duration") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = ytUrl, onValueChange = { ytUrl = it }, label = { Text("YouTube Demonstration Video URL") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = instructions, onValueChange = { instructions = it }, label = { Text("Step-by-Step Instructions") }, modifier = Modifier.fillMaxWidth(), minLines = 3) }
        item { OutlinedTextField(value = precautions, onValueChange = { precautions = it }, label = { Text("Precautions") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = mistakes, onValueChange = { mistakes = it }, label = { Text("Common Mistakes") }, modifier = Modifier.fillMaxWidth()) }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Publish to Public App?", fontWeight = FontWeight.SemiBold)
                Switch(checked = status == "PUBLISHED", onCheckedChange = { status = if (it) "PUBLISHED" else "DRAFT" })
            }
        }
        item {
            Button(
                onClick = {
                    onSave(exercise.copy(
                        name = name,
                        bodyPart = bodyPart,
                        repetitions = reps,
                        duration = duration,
                        frequency = freq,
                        model3dType = modelType,
                        youtubeUrl = ytUrl,
                        instructions = instructions,
                        precautions = precautions,
                        commonMistakes = mistakes,
                        status = status
                    ))
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PhysioTealPrimary)
            ) {
                Text("Save Exercise")
            }
        }
    }
}

@Composable
fun ServiceEditorSheetContent(
    service: ServiceEntity,
    isNew: Boolean,
    onSave: (ServiceEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(service.name) }
    var desc by remember { mutableStateOf(service.description) }
    var duration by remember { mutableStateOf(service.durationMinutes.toString()) }
    var enabled by remember { mutableStateOf(service.isEnabled) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(if (isNew) "Add New Clinical Service" else "Edit Service", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Service Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Service Description") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
        OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Duration (Minutes)") }, modifier = Modifier.fillMaxWidth())
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Service Available for Booking?", fontWeight = FontWeight.SemiBold)
            Switch(checked = enabled, onCheckedChange = { enabled = it })
        }
        Button(
            onClick = {
                onSave(service.copy(name = name, description = desc, durationMinutes = duration.toIntOrNull() ?: 45, isEnabled = enabled))
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PhysioTealPrimary)
        ) {
            Text("Save Service")
        }
    }
}

@Composable
fun DoctorProfileEditorSheetContent(
    doctor: DoctorProfileEntity,
    onSave: (DoctorProfileEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(doctor.name) }
    var qual by remember { mutableStateOf(doctor.qualifications) }
    var exp by remember { mutableStateOf(doctor.experience) }
    var phone by remember { mutableStateOf(doctor.phone) }
    var wa by remember { mutableStateOf(doctor.whatsapp) }
    var email by remember { mutableStateOf(doctor.email) }
    var about by remember { mutableStateOf(doctor.about) }
    var specs by remember { mutableStateOf(doctor.specializations) }
    var phil by remember { mutableStateOf(doctor.treatmentPhilosophy) }
    var area by remember { mutableStateOf(doctor.serviceArea) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Edit Doctor Profile & Contact", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Doctor Name") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = qual, onValueChange = { qual = it }, label = { Text("Qualifications") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = exp, onValueChange = { exp = it }, label = { Text("Experience") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Direct Calling Phone") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = wa, onValueChange = { wa = it }, label = { Text("WhatsApp Number") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = about, onValueChange = { about = it }, label = { Text("About Bio") }, modifier = Modifier.fillMaxWidth(), minLines = 3) }
        item { OutlinedTextField(value = specs, onValueChange = { specs = it }, label = { Text("Specializations") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = phil, onValueChange = { phil = it }, label = { Text("Treatment Philosophy") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = area, onValueChange = { area = it }, label = { Text("Home Visit Coverage Areas") }, modifier = Modifier.fillMaxWidth()) }
        item {
            Button(
                onClick = {
                    onSave(doctor.copy(name = name, qualifications = qual, experience = exp, phone = phone, whatsapp = wa, email = email, about = about, specializations = specs, treatmentPhilosophy = phil, serviceArea = area))
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PhysioTealPrimary)
            ) {
                Text("Update Public Doctor Profile")
            }
        }
    }
}

@Composable
fun HomeConfigEditorSheetContent(
    config: HomePageConfigEntity,
    onSave: (HomePageConfigEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var heroHead by remember { mutableStateOf(config.heroHeading) }
    var heroSub by remember { mutableStateOf(config.heroSubtitle) }
    var bookBtnText by remember { mutableStateOf(config.bookButtonText) }
    var showArticles by remember { mutableStateOf(config.showFeaturedArticles) }
    var showExercises by remember { mutableStateOf(config.showFeaturedExercises) }
    var showTestimonials by remember { mutableStateOf(config.showTestimonials) }
    var showCall by remember { mutableStateOf(config.showCallButton) }
    var showWa by remember { mutableStateOf(config.showWhatsappButton) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Configure Homepage Layout", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item { OutlinedTextField(value = heroHead, onValueChange = { heroHead = it }, label = { Text("Hero Banner Heading") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = heroSub, onValueChange = { heroSub = it }, label = { Text("Hero Banner Subtitle") }, modifier = Modifier.fillMaxWidth(), minLines = 2) }
        item { OutlinedTextField(value = bookBtnText, onValueChange = { bookBtnText = it }, label = { Text("Primary Action Button Text") }, modifier = Modifier.fillMaxWidth()) }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Show Featured Articles on Home?")
                Switch(checked = showArticles, onCheckedChange = { showArticles = it })
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Show 3D Exercises Section on Home?")
                Switch(checked = showExercises, onCheckedChange = { showExercises = it })
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Show Patient Reviews on Home?")
                Switch(checked = showTestimonials, onCheckedChange = { showTestimonials = it })
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Show Instant Call Button?")
                Switch(checked = showCall, onCheckedChange = { showCall = it })
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Show WhatsApp Direct Button?")
                Switch(checked = showWa, onCheckedChange = { showWa = it })
            }
        }

        item {
            Button(
                onClick = {
                    onSave(config.copy(
                        heroHeading = heroHead,
                        heroSubtitle = heroSub,
                        bookButtonText = bookBtnText,
                        showFeaturedArticles = showArticles,
                        showFeaturedExercises = showExercises,
                        showTestimonials = showTestimonials,
                        showCallButton = showCall,
                        showWhatsappButton = showWa
                    ))
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PhysioTealPrimary)
            ) {
                Text("Apply to Public User App")
            }
        }
    }
}
