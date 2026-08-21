package com.example.data.repository

import com.example.data.local.*
import kotlinx.coroutines.flow.Flow

class PhysioRepository(private val dao: PhysioDao) {

    // Doctor Profile
    val doctorProfile: Flow<DoctorProfileEntity?> = dao.getDoctorProfile()
    suspend fun updateDoctorProfile(profile: DoctorProfileEntity) {
        dao.updateDoctorProfile(profile)
        dao.insertActivityLog(
            ActivityLogEntity(
                actionTitle = "Doctor Profile Updated",
                detail = "Updated bio, qualifications, and specializations.",
                performedBy = "Admin"
            )
        )
    }

    // Home Page Config
    val homePageConfig: Flow<HomePageConfigEntity?> = dao.getHomePageConfig()
    suspend fun updateHomePageConfig(config: HomePageConfigEntity) {
        dao.updateHomePageConfig(config)
        dao.insertActivityLog(
            ActivityLogEntity(
                actionTitle = "Home Layout Updated",
                detail = "Modified homepage hero banner & section visibility.",
                performedBy = "Admin"
            )
        )
    }

    // Services
    val allServices: Flow<List<ServiceEntity>> = dao.getAllServices()
    val publishedServices: Flow<List<ServiceEntity>> = dao.getPublishedServices()
    suspend fun insertService(service: ServiceEntity) {
        dao.insertService(service)
        dao.insertActivityLog(
            ActivityLogEntity(
                actionTitle = "New Service Added",
                detail = "Created service: ${service.name}",
                performedBy = "Admin"
            )
        )
    }
    suspend fun updateService(service: ServiceEntity) {
        dao.updateService(service)
        dao.insertActivityLog(
            ActivityLogEntity(
                actionTitle = "Service Updated",
                detail = "Modified: ${service.name} (Active=${service.isEnabled})",
                performedBy = "Admin"
            )
        )
    }
    suspend fun deleteService(service: ServiceEntity) {
        dao.deleteService(service)
        dao.insertActivityLog(
            ActivityLogEntity(
                actionTitle = "Service Deleted",
                detail = "Removed: ${service.name}",
                performedBy = "Admin"
            )
        )
    }

    // Articles
    val allArticles: Flow<List<ArticleEntity>> = dao.getAllArticles()
    val publishedArticles: Flow<List<ArticleEntity>> = dao.getPublishedArticles()
    fun getArticleById(id: Long): Flow<ArticleEntity?> = dao.getArticleById(id)
    suspend fun insertArticle(article: ArticleEntity) {
        dao.insertArticle(article)
        dao.insertActivityLog(
            ActivityLogEntity(
                actionTitle = if (article.status == "PUBLISHED") "Article Published" else "Article Draft Saved",
                detail = "Title: ${article.title}",
                performedBy = "Admin"
            )
        )
    }
    suspend fun updateArticle(article: ArticleEntity) {
        dao.updateArticle(article)
        dao.insertActivityLog(
            ActivityLogEntity(
                actionTitle = "Article Updated",
                detail = "Title: ${article.title} (Status=${article.status})",
                performedBy = "Admin"
            )
        )
    }
    suspend fun deleteArticle(article: ArticleEntity) {
        dao.deleteArticle(article)
        dao.insertActivityLog(
            ActivityLogEntity(
                actionTitle = "Article Deleted",
                detail = "Removed article: ${article.title}",
                performedBy = "Admin"
            )
        )
    }

    // Exercises
    val allExercises: Flow<List<ExerciseEntity>> = dao.getAllExercises()
    val publishedExercises: Flow<List<ExerciseEntity>> = dao.getPublishedExercises()
    fun getExerciseById(id: Long): Flow<ExerciseEntity?> = dao.getExerciseById(id)
    suspend fun insertExercise(exercise: ExerciseEntity) {
        dao.insertExercise(exercise)
        dao.insertActivityLog(
            ActivityLogEntity(
                actionTitle = "Exercise Created",
                detail = "Added ${exercise.name} (${exercise.bodyPart})",
                performedBy = "Admin"
            )
        )
    }
    suspend fun updateExercise(exercise: ExerciseEntity) {
        dao.updateExercise(exercise)
        dao.insertActivityLog(
            ActivityLogEntity(
                actionTitle = "Exercise Updated",
                detail = "Modified: ${exercise.name}",
                performedBy = "Admin"
            )
        )
    }
    suspend fun deleteExercise(exercise: ExerciseEntity) {
        dao.deleteExercise(exercise)
        dao.insertActivityLog(
            ActivityLogEntity(
                actionTitle = "Exercise Deleted",
                detail = "Removed: ${exercise.name}",
                performedBy = "Admin"
            )
        )
    }

    // Assigned Exercises
    val allAssignedExercises: Flow<List<AssignedExerciseEntity>> = dao.getAllAssignedExercises()
    fun getAssignedExercisesForPatient(identifier: String): Flow<List<AssignedExerciseEntity>> =
        dao.getAssignedExercisesForPatient(identifier)

    suspend fun assignExerciseToPatient(assigned: AssignedExerciseEntity) {
        dao.insertAssignedExercise(assigned)
        dao.insertNotification(
            NotificationEntity(
                targetUserType = "USER",
                patientEmailOrPhone = assigned.patientEmailOrPhone,
                title = "New Exercise Assigned",
                message = "Dr. Das has assigned you: ${assigned.exerciseName} (${assigned.repetitions}, ${assigned.duration}).",
                type = "EXERCISE"
            )
        )
        dao.insertActivityLog(
            ActivityLogEntity(
                actionTitle = "Exercise Prescribed",
                detail = "Assigned ${assigned.exerciseName} to ${assigned.patientName}",
                performedBy = "Admin"
            )
        )
    }
    suspend fun updateAssignedExercise(assigned: AssignedExerciseEntity) = dao.updateAssignedExercise(assigned)
    suspend fun deleteAssignedExercise(assigned: AssignedExerciseEntity) = dao.deleteAssignedExercise(assigned)

    // Appointments
    val allAppointments: Flow<List<AppointmentEntity>> = dao.getAllAppointments()
    fun getAppointmentsForPatient(email: String, phone: String): Flow<List<AppointmentEntity>> =
        dao.getAppointmentsForPatient(email, phone)
    fun getAppointmentById(id: Long): Flow<AppointmentEntity?> = dao.getAppointmentById(id)

    suspend fun bookAppointment(appointment: AppointmentEntity) {
        dao.insertAppointment(appointment)
        // Notify admin
        dao.insertNotification(
            NotificationEntity(
                targetUserType = "ADMIN",
                title = "New Home Visit Booking",
                message = "${appointment.patientName} booked ${appointment.serviceName} for ${appointment.date} (${appointment.timeSlot}).",
                type = "BOOKING"
            )
        )
        // Notify patient
        dao.insertNotification(
            NotificationEntity(
                targetUserType = "USER",
                patientEmailOrPhone = appointment.patientEmail,
                title = "Booking Request Submitted",
                message = "Your request (ID: ${appointment.bookingId}) is pending confirmation by Dr. Das.",
                type = "BOOKING"
            )
        )
        dao.insertActivityLog(
            ActivityLogEntity(
                actionTitle = "New Booking Submitted",
                detail = "Patient: ${appointment.patientName} - ${appointment.serviceName}",
                performedBy = "Patient"
            )
        )
    }

    suspend fun updateAppointmentStatus(
        appointment: AppointmentEntity,
        newStatus: String,
        newDate: String = appointment.date,
        newTime: String = appointment.timeSlot,
        cancelReason: String = appointment.cancelReason
    ) {
        val updated = appointment.copy(
            status = newStatus,
            date = newDate,
            timeSlot = newTime,
            cancelReason = cancelReason
        )
        dao.updateAppointment(updated)

        val notificationMsg = when (newStatus) {
            "CONFIRMED" -> "Your appointment for $newDate at $newTime has been CONFIRMED by Dr. Das."
            "RESCHEDULED" -> "Your appointment has been RESCHEDULED to $newDate at $newTime."
            "CANCELLED" -> "Your appointment has been CANCELLED: $cancelReason"
            "COMPLETED" -> "Your appointment on $newDate has been marked as COMPLETED."
            else -> "Appointment status updated to $newStatus."
        }

        dao.insertNotification(
            NotificationEntity(
                targetUserType = "USER",
                patientEmailOrPhone = appointment.patientEmail,
                title = "Appointment $newStatus",
                message = notificationMsg,
                type = when (newStatus) {
                    "CONFIRMED" -> "CONFIRMATION"
                    "RESCHEDULED" -> "RESCHEDULE"
                    else -> "BOOKING"
                }
            )
        )
        dao.insertActivityLog(
            ActivityLogEntity(
                actionTitle = "Appointment Status Changed",
                detail = "Booking ${appointment.bookingId} changed to $newStatus",
                performedBy = "Admin"
            )
        )
    }

    // Working Hours
    val workingHours: Flow<List<WorkingHoursEntity>> = dao.getWorkingHours()
    suspend fun updateWorkingHours(hours: List<WorkingHoursEntity>) {
        dao.insertOrUpdateWorkingHours(hours)
        dao.insertActivityLog(
            ActivityLogEntity(
                actionTitle = "Working Hours Updated",
                detail = "Adjusted daily clinical availability.",
                performedBy = "Admin"
            )
        )
    }
    suspend fun updateSingleDay(day: WorkingHoursEntity) = dao.updateSingleDayWorkingHours(day)

    // Service Areas
    val allServiceAreas: Flow<List<ServiceAreaEntity>> = dao.getAllServiceAreas()
    val enabledServiceAreas: Flow<List<ServiceAreaEntity>> = dao.getEnabledServiceAreas()
    suspend fun insertServiceArea(area: ServiceAreaEntity) = dao.insertServiceArea(area)
    suspend fun updateServiceArea(area: ServiceAreaEntity) = dao.updateServiceArea(area)
    suspend fun deleteServiceArea(area: ServiceAreaEntity) = dao.deleteServiceArea(area)

    // Announcements
    val allAnnouncements: Flow<List<AnnouncementEntity>> = dao.getAllAnnouncements()
    val publishedAnnouncements: Flow<List<AnnouncementEntity>> = dao.getPublishedAnnouncements()
    suspend fun insertAnnouncement(announcement: AnnouncementEntity) {
        dao.insertAnnouncement(announcement)
        if (announcement.isPublished) {
            dao.insertNotification(
                NotificationEntity(
                    targetUserType = "USER",
                    title = "Clinic Announcement",
                    message = announcement.title,
                    type = "ANNOUNCEMENT"
                )
            )
        }
        dao.insertActivityLog(
            ActivityLogEntity(
                actionTitle = "Announcement Published",
                detail = announcement.title,
                performedBy = "Admin"
            )
        )
    }
    suspend fun updateAnnouncement(announcement: AnnouncementEntity) = dao.updateAnnouncement(announcement)
    suspend fun deleteAnnouncement(announcement: AnnouncementEntity) = dao.deleteAnnouncement(announcement)

    // FAQs
    val allFaqs: Flow<List<FaqEntity>> = dao.getAllFaqs()
    val publishedFaqs: Flow<List<FaqEntity>> = dao.getPublishedFaqs()
    suspend fun insertFaq(faq: FaqEntity) = dao.insertFaq(faq)
    suspend fun updateFaq(faq: FaqEntity) = dao.updateFaq(faq)
    suspend fun deleteFaq(faq: FaqEntity) = dao.deleteFaq(faq)

    // Testimonials
    val allTestimonials: Flow<List<TestimonialEntity>> = dao.getAllTestimonials()
    val publishedTestimonials: Flow<List<TestimonialEntity>> = dao.getPublishedTestimonials()
    suspend fun insertTestimonial(testimonial: TestimonialEntity) = dao.insertTestimonial(testimonial)
    suspend fun updateTestimonial(testimonial: TestimonialEntity) = dao.updateTestimonial(testimonial)
    suspend fun deleteTestimonial(testimonial: TestimonialEntity) = dao.deleteTestimonial(testimonial)

    // Notifications
    fun getNotifications(target: String): Flow<List<NotificationEntity>> = dao.getNotificationsForTarget(target)
    suspend fun markNotificationRead(id: Long) = dao.markNotificationRead(id)
    suspend fun markAllNotificationsRead(target: String) = dao.markAllNotificationsRead(target)
    suspend fun broadcastNotification(title: String, message: String) {
        dao.insertNotification(
            NotificationEntity(
                targetUserType = "USER",
                title = title,
                message = message,
                type = "ANNOUNCEMENT"
            )
        )
        dao.insertActivityLog(
            ActivityLogEntity(
                actionTitle = "Push Notification Broadcast",
                detail = title,
                performedBy = "Admin"
            )
        )
    }

    // Activity Logs
    val recentActivityLogs: Flow<List<ActivityLogEntity>> = dao.getRecentActivityLogs()
}
