package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "doctor_profile")
data class DoctorProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Dr. Satyaprakash Das",
    val title: String = "Senior Consultant Physiotherapist & Home Rehab Specialist",
    val photoUrl: String = "",
    val qualifications: String = "MPT (Orthopaedics & Sports Rehab), BPT, MIAP",
    val certifications: String = "Certified Manual Therapist (CMT), Dry Needling Specialist, Kinesiology Taping Practitioner",
    val experience: String = "9+ Years Clinical & Home Care Experience",
    val specializations: String = "Orthopedic Rehabilitation, Post-Surgical Rehab, Stroke Recovery, Sports Injuries, Geriatric Care",
    val about: String = "Dr. Satyaprakash Das is a dedicated physiotherapist with extensive clinical experience in treating acute musculoskeletal conditions, neurological disorders, and chronic joint pain. Providing evidence-based, compassionate care directly at patients' homes with advanced therapeutic exercise protocols.",
    val treatmentPhilosophy: String = "Patient-centric rehabilitation focusing on pain alleviation, joint mobility restoration, and empowering patients with progressive functional independence.",
    val serviceArea: String = "Home Visits available across City Metro & Suburban Zones",
    val phone: String = "+91 9583948448",
    val whatsapp: String = "+91 9583948448",
    val email: String = "dassatya752@gmail.com",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "services")
data class ServiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val category: String = "Rehabilitation",
    val description: String = "",
    val imageUrl: String = "",
    val isEnabled: Boolean = true,
    val displayOrder: Int = 0,
    val durationMinutes: Int = 45,
    val feeInfo: String = "Personalized Home Session"
)

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String = "",
    val category: String = "General Health",
    val shortDescription: String = "",
    val content: String = "",
    val author: String = "Dr. Satyaprakash Das",
    val featuredImageUrl: String = "",
    val tags: String = "Rehab, Recovery, Wellness",
    val status: String = "PUBLISHED", // "PUBLISHED" or "DRAFT"
    val publishedDate: String = "Aug 20, 2026",
    val readingTime: String = "4 min read",
    val isFeatured: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val bodyPart: String = "Knee", // "Knee", "Back", "Neck", "Shoulder", "Hip", "Ankle", "Full Body", "Balance"
    val category: String = "Strengthening", // "Strengthening", "Mobility", "Post-Op", "Stretching"
    val difficulty: String = "Beginner", // "Beginner", "Intermediate", "Advanced"
    val description: String = "",
    val instructions: String = "",
    val repetitions: String = "10-12 Reps",
    val duration: String = "3 Sets",
    val frequency: String = "2 times daily",
    val precautions: String = "Avoid jerky movements. Stop immediately if acute sharp pain occurs.",
    val commonMistakes: String = "Arching lower back, holding breath, rushing repetitions without controlled contraction.",
    val mediaType: String = "YOUTUBE", // "YOUTUBE", "DIRECT_VIDEO", "3D_INTERACTIVE", "VIDEO_AND_3D"
    val youtubeUrl: String = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
    val directVideoUrl: String = "",
    val model3dType: String = "KNEE_EXTENSION", // "KNEE_EXTENSION", "SPINE_FLEXION", "SHOULDER_ABDUCTION", "NECK_ROTATION", "ANKLE_DORSIFLEXION"
    val status: String = "PUBLISHED", // "PUBLISHED" or "DRAFT"
    val isFeatured: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "assigned_exercises")
data class AssignedExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientEmailOrPhone: String,
    val patientName: String,
    val exerciseId: Long,
    val exerciseName: String,
    val bodyPart: String,
    val repetitions: String,
    val duration: String,
    val frequency: String,
    val startDate: String,
    val endDate: String = "Ongoing",
    val physiotherapistInstructions: String,
    val isCompletedToday: Boolean = false,
    val assignedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookingId: String,
    val patientName: String,
    val patientPhone: String,
    val patientEmail: String,
    val serviceName: String,
    val date: String,
    val timeSlot: String,
    val homeAddress: String,
    val reason: String,
    val status: String = "PENDING", // "PENDING", "CONFIRMED", "RESCHEDULED", "COMPLETED", "CANCELLED"
    val cancelReason: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "working_hours")
data class WorkingHoursEntity(
    @PrimaryKey val dayOfWeek: String, // "Monday", "Tuesday", etc.
    val isOpen: Boolean = true,
    val openTime: String = "08:00 AM",
    val closeTime: String = "07:00 PM"
)

@Entity(tableName = "service_areas")
data class ServiceAreaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val areaName: String,
    val pincode: String,
    val isEnabled: Boolean = true,
    val visitFeeNote: String = "Standard Home Visit Rates"
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val imageUrl: String = "",
    val startDate: String = "Today",
    val endDate: String = "End of Month",
    val isPublished: Boolean = true,
    val isImportant: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "faqs")
data class FaqEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val question: String,
    val answer: String,
    val displayOrder: Int = 0,
    val isPublished: Boolean = true
)

@Entity(tableName = "testimonials")
data class TestimonialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientName: String,
    val review: String,
    val conditionTreated: String,
    val rating: Int = 5,
    val isPublished: Boolean = true
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val targetUserType: String = "USER", // "USER" or "ADMIN"
    val patientEmailOrPhone: String = "",
    val title: String,
    val message: String,
    val type: String = "BOOKING", // "BOOKING", "CONFIRMATION", "RESCHEDULE", "EXERCISE", "ANNOUNCEMENT"
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actionTitle: String,
    val detail: String,
    val performedBy: String = "Admin (Dr. Satya)",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "homepage_config")
data class HomePageConfigEntity(
    @PrimaryKey val id: Int = 1,
    val heroHeading: String = "Expert Physiotherapy & Home Rehabilitation",
    val heroSubtitle: String = "Personalized physical therapy, pain relief, and mobility recovery in the comfort of your home.",
    val bookButtonText: String = "Book Home Visit",
    val showFeaturedArticles: Boolean = true,
    val showFeaturedExercises: Boolean = true,
    val showTestimonials: Boolean = true,
    val showFaq: Boolean = true,
    val showCallButton: Boolean = true,
    val showWhatsappButton: Boolean = true,
    val showEmailButton: Boolean = true
)
