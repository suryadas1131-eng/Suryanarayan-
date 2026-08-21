package com.example.ui.patient

import android.widget.Toast
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
fun PatientAppointmentsScreen(
    viewModel: PhysioViewModel,
    openBookingOnLaunch: Boolean = false,
    onOpenAdminModeDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appointments by viewModel.patientAppointments.collectAsState()
    val services by viewModel.publishedServices.collectAsState()
    val workingHours by viewModel.workingHours.collectAsState()
    val serviceAreas by viewModel.enabledServiceAreas.collectAsState()
    val doctor by viewModel.doctorProfile.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("All", "Upcoming", "Pending", "Completed", "Cancelled")

    var showBookingSheet by remember { mutableStateOf(openBookingOnLaunch) }

    val filteredAppointments = remember(appointments, selectedTab) {
        when (selectedTab) {
            1 -> appointments.filter { it.status == "CONFIRMED" || it.status == "RESCHEDULED" }
            2 -> appointments.filter { it.status == "PENDING" }
            3 -> appointments.filter { it.status == "COMPLETED" }
            4 -> appointments.filter { it.status == "CANCELLED" }
            else -> appointments
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Appointments",
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
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showBookingSheet = true },
                containerColor = PhysioTealPrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Book Home Visit", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                divider = {},
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) PhysioTealPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredAppointments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.EventAvailable,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = PhysioTealLight
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No ${tabTitles[selectedTab]} Appointments",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Schedule a personalized in-home physical therapy session with Dr. Das.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showBookingSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PhysioTealPrimary)
                        ) {
                            Text("Book Home Visit Now")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredAppointments) { appt ->
                        AppointmentCard(
                            appointment = appt,
                            doctorPhone = doctor?.phone ?: "+91 9583948448",
                            doctorWhatsapp = doctor?.whatsapp ?: "+91 9583948448"
                        )
                    }
                }
            }
        }
    }

    // Booking Bottom Sheet
    if (showBookingSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBookingSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            BookHomeVisitSheetContent(
                services = services,
                workingHours = workingHours,
                serviceAreas = serviceAreas,
                defaultName = viewModel.patientName.value,
                defaultPhone = viewModel.patientPhone.value,
                defaultEmail = viewModel.patientEmail.value,
                onSubmit = { name, phone, email, service, date, time, address, reason ->
                    viewModel.submitBooking(name, phone, email, service, date, time, address, reason)
                    showBookingSheet = false
                    Toast.makeText(context, "Home visit request submitted successfully!", Toast.LENGTH_LONG).show()
                },
                onDismiss = { showBookingSheet = false }
            )
        }
    }
}

@Composable
fun AppointmentCard(
    appointment: AppointmentEntity,
    doctorPhone: String,
    doctorWhatsapp: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(PhysioTealContainer, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = appointment.bookingId,
                            color = PhysioTealDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                StatusBadge(status = appointment.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = appointment.serviceName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp), tint = PhysioTealPrimary)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${appointment.date} • ${appointment.timeSlot}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = appointment.homeAddress,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (appointment.reason.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Reason: ${appointment.reason}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (appointment.status == "CANCELLED" && appointment.cancelReason.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Cancellation Note: ${appointment.cancelReason}",
                        color = Color(0xFFC62828),
                        fontSize = 12.sp
                    )
                }
            }

            // Quick Contact Buttons for Confirmed Appointments
            if (appointment.status == "CONFIRMED" || appointment.status == "RESCHEDULED") {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { launchPhoneCall(context, doctorPhone) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Call Doctor", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { launchWhatsApp(context, doctorWhatsapp) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WhatsApp", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun BookHomeVisitSheetContent(
    services: List<ServiceEntity>,
    workingHours: List<WorkingHoursEntity>,
    serviceAreas: List<ServiceAreaEntity>,
    defaultName: String,
    defaultPhone: String,
    defaultEmail: String,
    onSubmit: (String, String, String, String, String, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var patientName by remember { mutableStateOf(defaultName) }
    var phone by remember { mutableStateOf(defaultPhone) }
    var email by remember { mutableStateOf(defaultEmail) }
    var selectedService by remember { mutableStateOf(services.firstOrNull()?.name ?: "Home Physiotherapy Visit") }
    var selectedDate by remember { mutableStateOf("Tomorrow (Friday, Aug 21)") }
    var selectedTime by remember { mutableStateOf("10:00 AM - 11:00 AM") }
    var address by remember { mutableStateOf("Flat 402, Green Valley Enclave, Metro Road") }
    var reason by remember { mutableStateOf("Knee pain & walking stiffness after long work hours.") }

    val dateOptions = listOf(
        "Today (Aug 20)",
        "Tomorrow (Aug 21)",
        "Saturday (Aug 22)",
        "Monday (Aug 24)",
        "Tuesday (Aug 25)"
    )

    val timeSlots = listOf(
        "09:00 AM - 10:00 AM",
        "10:00 AM - 11:00 AM",
        "11:30 AM - 12:30 PM",
        "03:00 PM - 04:00 PM",
        "04:30 PM - 05:30 PM",
        "06:00 PM - 07:00 PM"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Book Physiotherapy Home Visit",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Physiotherapist will visit your location with necessary therapy equipment.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (workingHours.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PhysioTealContainer.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = PhysioTealPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        val openDays = workingHours.filter { it.isOpen }
                        val summaryText = if (openDays.isNotEmpty()) {
                            val sample = openDays.first()
                            "${openDays.size} Days Active • ${sample.openTime} – ${sample.closeTime}"
                        } else {
                            "Consultation Hours Available"
                        }
                        Text("Active Working Hours: $summaryText", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PhysioTealDark)
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = patientName,
                onValueChange = { patientName = it },
                label = { Text("Patient Full Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
                )
            }
        }

        item {
            Text(
                text = "Select Service",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                services.take(4).forEach { s ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedService == s.name) PhysioTealContainer else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { selectedService = s.name }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = s.name,
                            fontWeight = if (selectedService == s.name) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedService == s.name) PhysioTealDark else MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp
                        )
                        if (selectedService == s.name) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PhysioTealPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Preferred Date",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            LazyColumn(
                modifier = Modifier.height(110.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(dateOptions) { d ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedDate == d) PhysioSecondaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { selectedDate = d }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(d, fontSize = 13.sp)
                        if (selectedDate == d) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = PhysioSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Preferred Time Slot",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            LazyColumn(
                modifier = Modifier.height(110.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(timeSlots) { t ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedTime == t) PhysioTealContainer else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { selectedTime = t }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(t, fontSize = 13.sp)
                        if (selectedTime == t) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = PhysioTealPrimary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Home Visit Full Address & Landmark") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) }
            )
        }

        item {
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("Describe Pain / Mobility Issue / Surgeries") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                leadingIcon = { Icon(Icons.Default.EditNote, contentDescription = null) }
            )
        }

        item {
            Button(
                onClick = {
                    if (patientName.isNotBlank() && phone.isNotBlank() && address.isNotBlank()) {
                        onSubmit(patientName, phone, email, selectedService, selectedDate, selectedTime, address, reason)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PhysioTealPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Submit Booking Request", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}
