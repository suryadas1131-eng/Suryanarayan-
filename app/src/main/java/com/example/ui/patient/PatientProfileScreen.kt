package com.example.ui.patient

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.PhysioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientProfileScreen(
    viewModel: PhysioViewModel,
    onOpenAdminModeDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val doctor by viewModel.doctorProfile.collectAsState()
    val workingHours by viewModel.workingHours.collectAsState()
    val faqs by viewModel.publishedFaqs.collectAsState()
    val notifications by viewModel.patientNotifications.collectAsState()

    val patientName by viewModel.patientName.collectAsState()
    val patientEmail by viewModel.patientEmail.collectAsState()
    val patientPhone by viewModel.patientPhone.collectAsState()

    val doc = doctor ?: DoctorProfileEntity()

    var showAboutDoctorSheet by remember { mutableStateOf(false) }
    var showNotificationsSheet by remember { mutableStateOf(false) }
    var showContactSheet by remember { mutableStateOf(false) }
    var showFaqSheet by remember { mutableStateOf(false) }
    var showPolicyDialog by remember { mutableStateOf<String?>(null) } // "PRIVACY" | "TERMS" | "DISCLAIMER"
    var showEditProfileDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile & Clinic Info",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onOpenAdminModeDialog) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Portal",
                            tint = PhysioTealPrimary
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Patient Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(PhysioTealContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = patientName.take(2).uppercase(),
                                color = PhysioTealDark,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = patientName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = patientPhone,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = patientEmail,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { showEditProfileDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = PhysioTealPrimary)
                        }
                    }
                }
            }

            // Doctor Highlight Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAboutDoctorSheet = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PhysioTealContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DoctorAvatarImage(
                            photoUrl = doc.photoUrl,
                            contentDescription = doc.name,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "About ${doc.name}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = PhysioTealDark
                            )
                            Text(
                                text = doc.qualifications,
                                style = MaterialTheme.typography.bodySmall,
                                color = PhysioTealPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Tap to view credentials, bio & specializations",
                                style = MaterialTheme.typography.labelSmall,
                                color = PhysioTealDark.copy(alpha = 0.8f)
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = PhysioTealPrimary)
                    }
                }
            }

            // Navigation List Items
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column {
                        ProfileMenuRow(
                            icon = Icons.Outlined.Notifications,
                            title = "Notifications & Reminders",
                            badgeCount = notifications.count { !it.isRead },
                            onClick = { showNotificationsSheet = true }
                        )
                        Divider()
                        ProfileMenuRow(
                            icon = Icons.Outlined.PhoneInTalk,
                            title = "Contact Us (Phone & WhatsApp)",
                            onClick = { showContactSheet = true }
                        )
                        Divider()
                        ProfileMenuRow(
                            icon = Icons.Outlined.QuestionAnswer,
                            title = "Frequently Asked Questions (FAQ)",
                            onClick = { showFaqSheet = true }
                        )
                    }
                }
            }

            // Legal & About Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column {
                        ProfileMenuRow(
                            icon = Icons.Outlined.HealthAndSafety,
                            title = "Medical Disclaimer",
                            onClick = { showPolicyDialog = "DISCLAIMER" }
                        )
                        Divider()
                        ProfileMenuRow(
                            icon = Icons.Outlined.Security,
                            title = "Privacy Policy",
                            onClick = { showPolicyDialog = "PRIVACY" }
                        )
                        Divider()
                        ProfileMenuRow(
                            icon = Icons.Outlined.Description,
                            title = "Terms & Conditions",
                            onClick = { showPolicyDialog = "TERMS" }
                        )
                    }
                }
            }

            // Admin Portal Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenAdminModeDialog),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PhysioTealPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Admin Control Portal",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Manage bookings, edit exercises, publish articles",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.Default.Lock, contentDescription = null, tint = PhysioTealPrimary)
                    }
                }
            }

            // App Version Info
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_physio_logo_v2_1787291462283),
                        contentDescription = "PhysioCare",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, PhysioTealPrimary, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "PhysioCare v2.0 • Live Rehabilitation Platform",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = PhysioTealDark
                    )
                    Text(
                        text = "Real-time Appointments • Shifts & Working Hours Engine",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // About Doctor Sheet
    if (showAboutDoctorSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAboutDoctorSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("About Physiotherapist", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showAboutDoctorSheet = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DoctorAvatarImage(
                            photoUrl = doc.photoUrl,
                            contentDescription = doc.name,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(doc.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(doc.qualifications, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            Text(doc.experience, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                item {
                    Text("Clinical Background & Bio", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(doc.about, style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp)
                }

                item {
                    Text("Specializations & Clinical Focus", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(doc.specializations, style = MaterialTheme.typography.bodyMedium, color = PhysioTealPrimary)
                }

                item {
                    Text("Treatment Philosophy", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(doc.treatmentPhilosophy, style = MaterialTheme.typography.bodyMedium)
                }

                item {
                    Text("Service Coverage", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(doc.serviceArea, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                item {
                    Text("Weekly Consultation & Visit Hours", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            workingHours.forEach { day ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(day.dayOfWeek, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    if (day.isOpen) {
                                        Text(
                                            "${day.openTime} – ${day.closeTime}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PhysioTealDark
                                        )
                                    } else {
                                        Text(
                                            "Closed",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Notifications Sheet
    if (showNotificationsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNotificationsSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Notifications", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { viewModel.markAllNotificationsRead("USER") }) {
                        Text("Mark all read")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No notifications", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notifications) { notif ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.markNotificationRead(notif.id) },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (notif.isRead) MaterialTheme.colorScheme.surfaceVariant else PhysioTealContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when (notif.type) {
                                            "CONFIRMATION" -> Icons.Default.CheckCircle
                                            "EXERCISE" -> Icons.Default.FitnessCenter
                                            else -> Icons.Default.Notifications
                                        },
                                        contentDescription = null,
                                        tint = PhysioTealPrimary
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(notif.message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Contact Sheet
    if (showContactSheet) {
        ModalBottomSheet(
            onDismissRequest = { showContactSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text("Contact ${doc.name}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(14.dp))
                QuickContactBar(
                    phone = doc.phone,
                    whatsapp = doc.whatsapp,
                    email = doc.email,
                    context = context,
                    doctorName = doc.name
                )
            }
        }
    }

    // FAQ Sheet
    if (showFaqSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFaqSheet = false }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("Frequently Asked Questions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                items(faqs) { faq ->
                    var isExpanded by remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = faq.question,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null
                                )
                            }
                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = faq.answer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Legal Policy Dialog
    showPolicyDialog?.let { policyType ->
        AlertDialog(
            onDismissRequest = { showPolicyDialog = null },
            title = {
                Text(
                    text = when (policyType) {
                        "DISCLAIMER" -> "Medical Disclaimer"
                        "PRIVACY" -> "Privacy Policy"
                        else -> "Terms & Conditions"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = when (policyType) {
                        "DISCLAIMER" -> "Exercise and educational information provided in this app is for general informational and rehabilitation purposes. Patients should follow individualized clinical instructions provided by Dr. Satyaprakash Das and seek emergency medical care if experiencing severe chest pain, shortness of breath, or acute paralysis."
                        "PRIVACY" -> "PhysioCare is committed to protecting patient health data. Your appointment details, medical conditions, and prescribed exercise adherence are strictly confidential and shared only with your treating physiotherapist."
                        else -> "By booking home visits and using prescribed exercise guides, you agree to follow the safety precautions and provide accurate medical history prior to home rehabilitation sessions."
                    },
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showPolicyDialog = null }) {
                    Text("Understood")
                }
            }
        )
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(patientName) }
        var editPhone by remember { mutableStateOf(patientPhone) }
        var editEmail by remember { mutableStateOf(patientEmail) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Patient Information", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Full Name") })
                    OutlinedTextField(value = editPhone, onValueChange = { editPhone = it }, label = { Text("Phone Number") })
                    OutlinedTextField(value = editEmail, onValueChange = { editEmail = it }, label = { Text("Email Address") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.patientName.value = editName
                        viewModel.patientPhone.value = editPhone
                        viewModel.patientEmail.value = editEmail
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PhysioTealPrimary)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ProfileMenuRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = PhysioTealPrimary, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        if (badgeCount > 0) {
            Box(
                modifier = Modifier
                    .background(PhysioAccentRed, CircleShape)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(text = "$badgeCount", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
