package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PhysioTealDark
import com.example.ui.theme.PhysioTealLight
import com.example.ui.theme.PhysioTealPrimary
import kotlin.math.*

/**
 * High-performance 3D Interactive Biomechanics & Anatomy Visualizer
 * Supports 360-degree rotation, pinch-zoom, animated joint angle kinematics, and muscle activation indicators.
 */
@Composable
fun Interactive3DExerciseViewer(
    modelType: String, // "KNEE_EXTENSION", "SPINE_FLEXION", "SHOULDER_ABDUCTION", "NECK_ROTATION", "ANKLE_DORSIFLEXION"
    modifier: Modifier = Modifier,
    autoPlay: Boolean = true
) {
    var yaw by remember { mutableFloatStateOf(25f) }
    var pitch by remember { mutableFloatStateOf(-15f) }
    var zoom by remember { mutableFloatStateOf(1.0f) }
    var isPlaying by remember { mutableStateOf(autoPlay) }

    val infiniteTransition = rememberInfiniteTransition(label = "biomechanics")
    val cycleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cycle"
    )

    val currentProgress = if (isPlaying) cycleProgress else 0.5f
    // Triangular wave for smooth back-and-forth joint motion (0.0 -> 1.0 -> 0.0)
    val jointAngleFactor = 0.5f - 0.5f * cos(currentProgress * 2 * Math.PI.toFloat())

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8FAFC),
                        Color(0xFFE2E8F0)
                    )
                )
            )
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, gestureZoom, _ ->
                    zoom = (zoom * gestureZoom).coerceIn(0.6f, 2.5f)
                    yaw += pan.x * 0.4f
                    pitch = (pitch - pan.y * 0.4f).coerceIn(-75f, 75f)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f

            // Draw Subtle 3D Grid on ground plane
            draw3DGrid(cx, cy, zoom, yaw, pitch)

            // Draw specific 3D anatomy model
            when (modelType.uppercase()) {
                "SPINE_FLEXION" -> draw3DSpineModel(cx, cy, zoom, yaw, pitch, jointAngleFactor)
                "SHOULDER_ABDUCTION" -> draw3DShoulderModel(cx, cy, zoom, yaw, pitch, jointAngleFactor)
                "NECK_ROTATION" -> draw3DNeckModel(cx, cy, zoom, yaw, pitch, jointAngleFactor)
                "ANKLE_DORSIFLEXION" -> draw3DAnkleModel(cx, cy, zoom, yaw, pitch, jointAngleFactor)
                else -> draw3DKneeModel(cx, cy, zoom, yaw, pitch, jointAngleFactor)
            }
        }

        // Overlay Badge: Model Name and Live Joint Angle
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .background(Color(0xE6FFFFFF), RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ViewInAr,
                contentDescription = null,
                tint = PhysioTealPrimary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            val angleText = when (modelType.uppercase()) {
                "SPINE_FLEXION" -> "Lumbar Tilt: ${(jointAngleFactor * 25).toInt()}°"
                "SHOULDER_ABDUCTION" -> "Abduction Arc: ${(jointAngleFactor * 110 + 10).toInt()}°"
                "NECK_ROTATION" -> "Cervical Rot: ${(sin(currentProgress * 2 * Math.PI.toFloat()) * 40).toInt()}°"
                "ANKLE_DORSIFLEXION" -> "Dorsiflexion: ${(jointAngleFactor * 35 - 10).toInt()}°"
                else -> "Knee Joint: ${(jointAngleFactor * 85 + 5).toInt()}°"
            }
            Text(
                text = "3D BioEngine: $angleText",
                color = Color(0xFF0F172A),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Movement Phase Indicator
        val phaseText = when {
            jointAngleFactor < 0.25f -> "Starting Alignment"
            jointAngleFactor in 0.25f..0.75f && currentProgress < 0.5f -> "Concentric / Action Phase"
            jointAngleFactor > 0.75f -> "Peak Contraction (Hold)"
            else -> "Eccentric / Return Phase"
        }

        Text(
            text = phaseText,
            color = PhysioTealDark,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
                .background(Color(0xE6E0F2F1), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )

        // Overlay Controls: Play/Pause, Reset View
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = {
                    yaw = 25f
                    pitch = -15f
                    zoom = 1.0f
                },
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xE6FFFFFF), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset View",
                    tint = Color(0xFF334155),
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = { isPlaying = !isPlaying },
                modifier = Modifier
                    .size(36.dp)
                    .background(PhysioTealPrimary, CircleShape)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// 3D Math Helper: Point Projection
private data class Point3D(val x: Float, val y: Float, val z: Float)

private fun project3D(
    p: Point3D,
    cx: Float,
    cy: Float,
    zoom: Float,
    yawDeg: Float,
    pitchDeg: Float
): Offset {
    val yawRad = Math.toRadians(yawDeg.toDouble()).toFloat()
    val pitchRad = Math.toRadians(pitchDeg.toDouble()).toFloat()

    // Rotate around Y axis (Yaw)
    val x1 = p.x * cos(yawRad) + p.z * sin(yawRad)
    val y1 = p.y
    val z1 = -p.x * sin(yawRad) + p.z * cos(yawRad)

    // Rotate around X axis (Pitch)
    val x2 = x1
    val y2 = y1 * cos(pitchRad) - z1 * sin(pitchRad)
    val z2 = y1 * sin(pitchRad) + z1 * cos(pitchRad)

    // Perspective Division
    val cameraDist = 400f
    val fov = cameraDist / (cameraDist + z2)
    val scale = zoom * fov

    return Offset(cx + x2 * scale, cy + y2 * scale)
}

// 3D Ground Grid
private fun DrawScope.draw3DGrid(cx: Float, cy: Float, zoom: Float, yaw: Float, pitch: Float) {
    val gridSize = 140f
    val step = 35f
    val gridY = 90f

    for (i in -4..4) {
        val pStart = project3D(Point3D(-gridSize, gridY, i * step), cx, cy, zoom, yaw, pitch)
        val pEnd = project3D(Point3D(gridSize, gridY, i * step), cx, cy, zoom, yaw, pitch)
        drawLine(
            color = Color(0x3300796B),
            start = pStart,
            end = pEnd,
            strokeWidth = 1.2f
        )

        val pStartCross = project3D(Point3D(i * step, gridY, -gridSize), cx, cy, zoom, yaw, pitch)
        val pEndCross = project3D(Point3D(i * step, gridY, gridSize), cx, cy, zoom, yaw, pitch)
        drawLine(
            color = Color(0x3300796B),
            start = pStartCross,
            end = pEndCross,
            strokeWidth = 1.2f
        )
    }
}

// 3D Knee Anatomy Rendering
private fun DrawScope.draw3DKneeModel(
    cx: Float,
    cy: Float,
    zoom: Float,
    yaw: Float,
    pitch: Float,
    progress: Float
) {
    // Femur (Thigh bone) - fixed top
    val hipJoint = Point3D(0f, -80f, 0f)
    val kneeJoint = Point3D(0f, 0f, 0f)

    // Tibia (Shin bone) rotates with angle
    val flexAngleRad = Math.toRadians((85f * (1f - progress) + 5f).toDouble()).toFloat()
    val tibiaLength = 80f
    val ankleJoint = Point3D(
        kneeJoint.x + sin(flexAngleRad) * tibiaLength,
        kneeJoint.y + cos(flexAngleRad) * tibiaLength,
        kneeJoint.z
    )

    val pHip = project3D(hipJoint, cx, cy, zoom, yaw, pitch)
    val pKnee = project3D(kneeJoint, cx, cy, zoom, yaw, pitch)
    val pAnkle = project3D(ankleJoint, cx, cy, zoom, yaw, pitch)

    // Muscle: Quadriceps (highlighted in active orange/teal)
    val muscleMid = project3D(Point3D(-15f, -40f, 0f), cx, cy, zoom, yaw, pitch)
    val quadPath = Path().apply {
        moveTo(pHip.x, pHip.y)
        quadraticTo(muscleMid.x, muscleMid.y, pKnee.x, pKnee.y)
    }
    drawPath(
        path = quadPath,
        color = Color(0xFFEA580C).copy(alpha = 0.6f + progress * 0.35f),
        style = Stroke(width = 16f * zoom, cap = StrokeCap.Round)
    )

    // Femur Bone
    drawLine(
        color = Color(0xFF334155),
        start = pHip,
        end = pKnee,
        strokeWidth = 14f * zoom,
        cap = StrokeCap.Round
    )

    // Patella (Knee Cap)
    val patellaPos = project3D(Point3D(0f, -4f, -8f), cx, cy, zoom, yaw, pitch)
    drawCircle(
        color = Color(0xFF00796B),
        radius = 9f * zoom,
        center = patellaPos
    )

    // Tibia Bone
    drawLine(
        color = Color(0xFF475569),
        start = pKnee,
        end = pAnkle,
        strokeWidth = 12f * zoom,
        cap = StrokeCap.Round
    )

    // Joint Pivot Markers
    drawCircle(color = Color(0xFF00796B), radius = 7f * zoom, center = pKnee)
    drawCircle(color = Color(0xFF94A3B8), radius = 6f * zoom, center = pHip)
    drawCircle(color = Color(0xFF94A3B8), radius = 6f * zoom, center = pAnkle)
}

// 3D Spine Model
private fun DrawScope.draw3DSpineModel(
    cx: Float,
    cy: Float,
    zoom: Float,
    yaw: Float,
    pitch: Float,
    progress: Float
) {
    val vertebraeCount = 6
    val baseSacrum = Point3D(0f, 60f, 0f)
    var prevPoint = project3D(baseSacrum, cx, cy, zoom, yaw, pitch)

    drawCircle(color = Color(0xFF475569), radius = 10f * zoom, center = prevPoint)

    for (i in 1..vertebraeCount) {
        val tiltOffset = sin(i * 0.4f) * progress * 18f
        val vert3D = Point3D(tiltOffset, 60f - (i * 22f), tiltOffset * 0.5f)
        val currentPoint = project3D(vert3D, cx, cy, zoom, yaw, pitch)

        // Intervertebral Disc
        drawLine(
            color = Color(0xFF00796B),
            start = prevPoint,
            end = currentPoint,
            strokeWidth = 8f * zoom,
            cap = StrokeCap.Round
        )

        // Vertebral Body
        drawCircle(
            color = Color(0xFF334155),
            radius = (9f - i * 0.5f) * zoom,
            center = currentPoint
        )

        prevPoint = currentPoint
    }
}

// 3D Shoulder Model
private fun DrawScope.draw3DShoulderModel(
    cx: Float,
    cy: Float,
    zoom: Float,
    yaw: Float,
    pitch: Float,
    progress: Float
) {
    val spineBase = Point3D(-40f, -40f, 0f)
    val shoulderJoint = Point3D(10f, -40f, 0f)

    val abdAngleRad = Math.toRadians((progress * 110f + 10f).toDouble()).toFloat()
    val humerusLength = 75f
    val elbowJoint = Point3D(
        shoulderJoint.x + sin(abdAngleRad) * humerusLength,
        shoulderJoint.y + cos(abdAngleRad) * humerusLength,
        shoulderJoint.z
    )

    val pSpine = project3D(spineBase, cx, cy, zoom, yaw, pitch)
    val pShoulder = project3D(shoulderJoint, cx, cy, zoom, yaw, pitch)
    val pElbow = project3D(elbowJoint, cx, cy, zoom, yaw, pitch)

    // Clavicle
    drawLine(color = Color(0xFF94A3B8), start = pSpine, end = pShoulder, strokeWidth = 8f * zoom)

    // Deltoid & Rotator Cuff Arc
    drawLine(
        color = Color(0xFFEA580C).copy(alpha = 0.5f + progress * 0.45f),
        start = pShoulder,
        end = pElbow,
        strokeWidth = 16f * zoom
    )

    // Humerus Bone
    drawLine(color = Color(0xFF334155), start = pShoulder, end = pElbow, strokeWidth = 11f * zoom)
    drawCircle(color = Color(0xFF00796B), radius = 8f * zoom, center = pShoulder)
    drawCircle(color = Color(0xFF64748B), radius = 6f * zoom, center = pElbow)
}

// 3D Neck Model
private fun DrawScope.draw3DNeckModel(
    cx: Float,
    cy: Float,
    zoom: Float,
    yaw: Float,
    pitch: Float,
    progress: Float
) {
    val rotAngle = sin(progress * 2 * Math.PI.toFloat()) * 35f
    val baseThoracic = Point3D(0f, 40f, 0f)
    val headCenter = Point3D(sin(Math.toRadians(rotAngle.toDouble()).toFloat()) * 15f, -40f, 0f)

    val pBase = project3D(baseThoracic, cx, cy, zoom, yaw, pitch)
    val pHead = project3D(headCenter, cx, cy, zoom, yaw, pitch)

    drawLine(color = Color(0xFF00796B), start = pBase, end = pHead, strokeWidth = 10f * zoom)
    drawCircle(color = Color(0xFF334155), radius = 22f * zoom, center = pHead)
    drawCircle(color = Color(0xFF004D40), radius = 7f * zoom, center = pBase)
}

// 3D Ankle Model
private fun DrawScope.draw3DAnkleModel(
    cx: Float,
    cy: Float,
    zoom: Float,
    yaw: Float,
    pitch: Float,
    progress: Float
) {
    val shinTop = Point3D(0f, -60f, 0f)
    val ankleJoint = Point3D(0f, 20f, 0f)

    val flexAngleRad = Math.toRadians((progress * 40f - 15f).toDouble()).toFloat()
    val toeTip = Point3D(
        ankleJoint.x + cos(flexAngleRad) * 45f,
        ankleJoint.y + sin(flexAngleRad) * 20f,
        ankleJoint.z
    )

    val pShin = project3D(shinTop, cx, cy, zoom, yaw, pitch)
    val pAnkle = project3D(ankleJoint, cx, cy, zoom, yaw, pitch)
    val pToe = project3D(toeTip, cx, cy, zoom, yaw, pitch)

    // Achilles tendon
    drawLine(color = Color(0xFFF97316), start = pShin, end = pAnkle, strokeWidth = 6f * zoom)
    // Tibia
    drawLine(color = Color(0xFF334155), start = pShin, end = pAnkle, strokeWidth = 12f * zoom)
    // Foot arch
    drawLine(color = Color(0xFF64748B), start = pAnkle, end = pToe, strokeWidth = 10f * zoom)
    drawCircle(color = Color(0xFF00796B), radius = 7f * zoom, center = pAnkle)
}
