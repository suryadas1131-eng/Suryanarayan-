package com.example.ui.patient

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.PhysioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientExercisesScreen(
    viewModel: PhysioViewModel,
    selectedExerciseToOpen: ExerciseEntity? = null,
    onOpenAdminModeDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val publishedExercises by viewModel.publishedExercises.collectAsState()
    val assignedExercises by viewModel.assignedExercisesForPatient.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Library, 1: My Prescribed
    var selectedBodyPart by remember { mutableStateOf("All") }
    var activeExerciseDetail by remember { mutableStateOf<ExerciseEntity?>(selectedExerciseToOpen) }

    val bodyParts = listOf("All", "Knee", "Back", "Shoulder", "Neck", "Ankle", "Balance")

    val filteredExercises = remember(publishedExercises, selectedBodyPart) {
        if (selectedBodyPart == "All") publishedExercises
        else publishedExercises.filter { it.bodyPart.equals(selectedBodyPart, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Physiotherapy Exercises",
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Main Top Tabs: Library vs My Prescribed Exercises
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PhysioTealPrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "Exercise Library (${publishedExercises.size})",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "My Prescribed",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                            if (assignedExercises.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(PhysioAccentAmber, CircleShape)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${assignedExercises.size}",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                )
            }

            if (selectedTab == 0) {
                // Body Part Chips
                LazyRow(
                    modifier = Modifier.padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(bodyParts) { part ->
                        FilterChip(
                            selected = selectedBodyPart == part,
                            onClick = { selectedBodyPart = part },
                            label = { Text(part) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PhysioTealPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                if (filteredExercises.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No exercises found for $selectedBodyPart.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredExercises) { exercise ->
                            ExerciseCard(
                                exercise = exercise,
                                onClick = { activeExerciseDetail = exercise }
                            )
                        }
                    }
                }
            } else {
                // My Prescribed Exercises Tab
                if (assignedExercises.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.AssignmentLate,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = PhysioTealLight
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Exercises Assigned Yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Dr. Das will prescribe tailored rehabilitation exercises for you during your home visit.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = PhysioTealContainer),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = PhysioTealPrimary)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Personalized routine prescribed by Dr. Satyaprakash Das. Follow instructions carefully.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PhysioTealDark,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        items(assignedExercises) { assigned ->
                            AssignedExerciseCard(
                                assigned = assigned,
                                onToggleComplete = { viewModel.markAssignedExerciseCompleted(assigned) },
                                onOpenFullGuide = {
                                    val match = publishedExercises.find { it.id == assigned.exerciseId }
                                        ?: ExerciseEntity(
                                            name = assigned.exerciseName,
                                            bodyPart = assigned.bodyPart,
                                            category = "Prescription",
                                            description = assigned.physiotherapistInstructions,
                                            instructions = assigned.physiotherapistInstructions,
                                            repetitions = assigned.repetitions,
                                            duration = assigned.duration,
                                            frequency = assigned.frequency,
                                            model3dType = when (assigned.bodyPart.lowercase()) {
                                                "back" -> "SPINE_FLEXION"
                                                "shoulder" -> "SHOULDER_ABDUCTION"
                                                "neck" -> "NECK_ROTATION"
                                                "ankle" -> "ANKLE_DORSIFLEXION"
                                                else -> "KNEE_EXTENSION"
                                            }
                                        )
                                    activeExerciseDetail = match
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Exercise Detail Sheet
    activeExerciseDetail?.let { ex ->
        ModalBottomSheet(
            onDismissRequest = { activeExerciseDetail = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            ExerciseDetailSheetContent(
                exercise = ex,
                onDismiss = { activeExerciseDetail = null }
            )
        }
    }
}

@Composable
fun ExerciseCard(
    exercise: ExerciseEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = exercise.bodyPart,
                            color = PhysioTealDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = exercise.difficulty,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (exercise.mediaType.contains("3D")) {
                        Icon(
                            imageVector = Icons.Default.ViewInAr,
                            contentDescription = "3D Interactive",
                            tint = PhysioTealPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    if (exercise.mediaType.contains("YOUTUBE") || exercise.mediaType.contains("VIDEO")) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = "Video",
                            tint = Color(0xFFFF0000),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = exercise.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${exercise.repetitions} • ${exercise.duration} • ${exercise.frequency}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                TextButton(
                    onClick = onClick,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Open Guide", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun AssignedExerciseCard(
    assigned: AssignedExerciseEntity,
    onToggleComplete: () -> Unit,
    onOpenFullGuide: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (assigned.isCompletedToday) Color(0xFFF1F8E9) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(PhysioSecondaryContainer, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Prescribed: ${assigned.bodyPart}",
                        color = PhysioSecondaryDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onToggleComplete) {
                    Icon(
                        imageVector = if (assigned.isCompletedToday) Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = "Mark done today",
                        tint = if (assigned.isCompletedToday) PhysioAccentGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Text(
                text = assigned.exerciseName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Reps: ${assigned.repetitions}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Sets: ${assigned.duration}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = assigned.frequency,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "Dr. Note: ${assigned.physiotherapistInstructions}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (assigned.isCompletedToday) "✓ Done for today" else "Pending today's session",
                    color = if (assigned.isCompletedToday) PhysioAccentGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Button(
                    onClick = onOpenFullGuide,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PhysioTealPrimary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.ViewInAr, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("3D & Video Guide", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ExerciseDetailSheetContent(
    exercise: ExerciseEntity,
    onDismiss: () -> Unit
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
                Column {
                    Text(
                        text = "${exercise.bodyPart} • ${exercise.category}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        }

        // 3D Interactive Model Section (if enabled)
        if (exercise.mediaType.contains("3D")) {
            item {
                Text(
                    text = "Interactive 3D Biomechanics Viewer (Drag to rotate, pinch to zoom)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Interactive3DExerciseViewer(
                    modelType = exercise.model3dType,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Video Player Embed Section (if enabled)
        if (exercise.mediaType.contains("YOUTUBE") || exercise.mediaType.contains("VIDEO")) {
            item {
                Text(
                    text = "Exercise Video Demonstration",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF0000)
                )
                Spacer(modifier = Modifier.height(6.dp))
                YouTubePlayerEmbed(
                    youtubeUrl = exercise.youtubeUrl,
                    exerciseTitle = exercise.name
                )
            }
        }

        // Prescription Metrics Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard("Repetitions", exercise.repetitions, Modifier.weight(1f))
                MetricCard("Sets / Duration", exercise.duration, Modifier.weight(1f))
                MetricCard("Frequency", exercise.frequency, Modifier.weight(1f))
            }
        }

        // Step-by-Step Instructions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Step-by-Step Instructions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = exercise.instructions,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // Precautions
        if (exercise.precautions.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = PhysioAccentAmber, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Important Precautions",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFBF360C)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = exercise.precautions,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFD84315)
                        )
                    }
                }
            }
        }

        // Common Mistakes
        if (exercise.commonMistakes.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = PhysioAccentRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Common Mistakes to Avoid",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = PhysioAccentRed
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = exercise.commonMistakes,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB71C1C)
                        )
                    }
                }
            }
        }

        // Medical Disclaimer
        item {
            Text(
                text = "Medical Disclaimer: Exercises shown are for clinical educational guidance. Always follow individualized advice prescribed by Dr. Satyaprakash Das.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PhysioTealContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = PhysioTealDark)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = PhysioTealDark,
                maxLines = 1
            )
        }
    }
}
