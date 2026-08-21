package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.repository.PhysioRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppMode {
    PATIENT_APP,
    ADMIN_APP
}

class PhysioViewModel(application: Application) : AndroidViewModel(application) {
    private val database = PhysioDatabase.getDatabase(application)
    private val repository = PhysioRepository(database.physioDao())

    // Active App Mode (Patient vs Admin)
    private val _appMode = MutableStateFlow(AppMode.PATIENT_APP)
    val appMode: StateFlow<AppMode> = _appMode.asStateFlow()

    // Admin Auth State
    private val _isAdminAuthenticated = MutableStateFlow(false)
    val isAdminAuthenticated: StateFlow<Boolean> = _isAdminAuthenticated.asStateFlow()

    private val _adminAuthError = MutableStateFlow<String?>(null)
    val adminAuthError: StateFlow<String?> = _adminAuthError.asStateFlow()

    // Current Patient User session
    val patientEmail = MutableStateFlow("suryadas1131@gmail.com")
    val patientName = MutableStateFlow("Surya Das")
    val patientPhone = MutableStateFlow("+91 9583948448")

    // Reactive Data Streams
    val doctorProfile: StateFlow<DoctorProfileEntity?> = repository.doctorProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val homePageConfig: StateFlow<HomePageConfigEntity?> = repository.homePageConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val publishedServices: StateFlow<List<ServiceEntity>> = repository.publishedServices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allServices: StateFlow<List<ServiceEntity>> = repository.allServices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val publishedArticles: StateFlow<List<ArticleEntity>> = repository.publishedArticles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allArticles: StateFlow<List<ArticleEntity>> = repository.allArticles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val publishedExercises: StateFlow<List<ExerciseEntity>> = repository.publishedExercises
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExercises: StateFlow<List<ExerciseEntity>> = repository.allExercises
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val assignedExercisesForPatient: StateFlow<List<AssignedExerciseEntity>> = patientEmail
        .flatMapLatest { email -> repository.getAssignedExercisesForPatient(email) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAssignedExercises: StateFlow<List<AssignedExerciseEntity>> = repository.allAssignedExercises
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val patientAppointments: StateFlow<List<AppointmentEntity>> = combine(patientEmail, patientPhone) { email, phone ->
        Pair(email, phone)
    }.flatMapLatest { (email, phone) ->
        repository.getAppointmentsForPatient(email, phone)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAppointments: StateFlow<List<AppointmentEntity>> = repository.allAppointments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workingHours: StateFlow<List<WorkingHoursEntity>> = repository.workingHours
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val enabledServiceAreas: StateFlow<List<ServiceAreaEntity>> = repository.enabledServiceAreas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allServiceAreas: StateFlow<List<ServiceAreaEntity>> = repository.allServiceAreas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val publishedAnnouncements: StateFlow<List<AnnouncementEntity>> = repository.publishedAnnouncements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAnnouncements: StateFlow<List<AnnouncementEntity>> = repository.allAnnouncements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val publishedFaqs: StateFlow<List<FaqEntity>> = repository.publishedFaqs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFaqs: StateFlow<List<FaqEntity>> = repository.allFaqs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val publishedTestimonials: StateFlow<List<TestimonialEntity>> = repository.publishedTestimonials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTestimonials: StateFlow<List<TestimonialEntity>> = repository.allTestimonials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val patientNotifications: StateFlow<List<NotificationEntity>> = repository.getNotifications("USER")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminNotifications: StateFlow<List<NotificationEntity>> = repository.getNotifications("ADMIN")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentActivityLogs: StateFlow<List<ActivityLogEntity>> = repository.recentActivityLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Mode Switching & Admin Auth ---
    fun switchToPatientMode() {
        _appMode.value = AppMode.PATIENT_APP
    }

    fun switchToAdminMode() {
        _appMode.value = AppMode.ADMIN_APP
    }

    fun loginAdmin(passOrPin: String): Boolean {
        return if (passOrPin == "narayan@1971") {
            _isAdminAuthenticated.value = true
            _adminAuthError.value = null
            true
        } else {
            _adminAuthError.value = "Invalid Admin Password."
            false
        }
    }

    fun logoutAdmin() {
        _isAdminAuthenticated.value = false
        _appMode.value = AppMode.PATIENT_APP
    }

    // --- Patient Actions ---
    fun submitBooking(
        name: String,
        phone: String,
        email: String,
        serviceName: String,
        date: String,
        timeSlot: String,
        address: String,
        reason: String
    ) {
        viewModelScope.launch {
            val bookingNum = (1000..9999).random()
            val newBooking = AppointmentEntity(
                bookingId = "PHY-2608-$bookingNum",
                patientName = name,
                patientPhone = phone,
                patientEmail = email,
                serviceName = serviceName,
                date = date,
                timeSlot = timeSlot,
                homeAddress = address,
                reason = reason,
                status = "PENDING"
            )
            repository.bookAppointment(newBooking)
        }
    }

    fun markAssignedExerciseCompleted(assigned: AssignedExerciseEntity) {
        viewModelScope.launch {
            repository.updateAssignedExercise(assigned.copy(isCompletedToday = !assigned.isCompletedToday))
        }
    }

    fun markNotificationRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }

    fun markAllNotificationsRead(target: String) {
        viewModelScope.launch {
            repository.markAllNotificationsRead(target)
        }
    }

    // --- Admin Actions (Control Center) ---
    fun updateDoctorProfile(updated: DoctorProfileEntity) {
        viewModelScope.launch {
            repository.updateDoctorProfile(updated)
        }
    }

    fun updateHomePageConfig(config: HomePageConfigEntity) {
        viewModelScope.launch {
            repository.updateHomePageConfig(config)
        }
    }

    // Service Management
    fun saveService(service: ServiceEntity) {
        viewModelScope.launch {
            if (service.id == 0L) {
                repository.insertService(service)
            } else {
                repository.updateService(service)
            }
        }
    }

    fun deleteService(service: ServiceEntity) {
        viewModelScope.launch {
            repository.deleteService(service)
        }
    }

    fun toggleServiceStatus(service: ServiceEntity) {
        viewModelScope.launch {
            repository.updateService(service.copy(isEnabled = !service.isEnabled))
        }
    }

    // Article Management
    fun saveArticle(article: ArticleEntity) {
        viewModelScope.launch {
            if (article.id == 0L) {
                repository.insertArticle(article)
            } else {
                repository.updateArticle(article)
            }
        }
    }

    fun deleteArticle(article: ArticleEntity) {
        viewModelScope.launch {
            repository.deleteArticle(article)
        }
    }

    fun toggleArticlePublish(article: ArticleEntity) {
        viewModelScope.launch {
            val newStatus = if (article.status == "PUBLISHED") "DRAFT" else "PUBLISHED"
            repository.updateArticle(article.copy(status = newStatus))
        }
    }

    // Exercise Management
    fun saveExercise(exercise: ExerciseEntity) {
        viewModelScope.launch {
            if (exercise.id == 0L) {
                repository.insertExercise(exercise)
            } else {
                repository.updateExercise(exercise)
            }
        }
    }

    fun deleteExercise(exercise: ExerciseEntity) {
        viewModelScope.launch {
            repository.deleteExercise(exercise)
        }
    }

    fun toggleExercisePublish(exercise: ExerciseEntity) {
        viewModelScope.launch {
            val newStatus = if (exercise.status == "PUBLISHED") "DRAFT" else "PUBLISHED"
            repository.updateExercise(exercise.copy(status = newStatus))
        }
    }

    // Assign Exercise to Patient
    fun assignExerciseToPatient(
        patientEmailOrPhone: String,
        patientName: String,
        exercise: ExerciseEntity,
        reps: String,
        duration: String,
        frequency: String,
        startDate: String,
        notes: String
    ) {
        viewModelScope.launch {
            val assignment = AssignedExerciseEntity(
                patientEmailOrPhone = patientEmailOrPhone,
                patientName = patientName,
                exerciseId = exercise.id,
                exerciseName = exercise.name,
                bodyPart = exercise.bodyPart,
                repetitions = reps,
                duration = duration,
                frequency = frequency,
                startDate = startDate,
                physiotherapistInstructions = notes
            )
            repository.assignExerciseToPatient(assignment)
        }
    }

    fun removeAssignedExercise(assigned: AssignedExerciseEntity) {
        viewModelScope.launch {
            repository.deleteAssignedExercise(assigned)
        }
    }

    // Appointment Admin Operations
    fun confirmAppointment(appointment: AppointmentEntity) {
        viewModelScope.launch {
            repository.updateAppointmentStatus(appointment, "CONFIRMED")
        }
    }

    fun rescheduleAppointment(appointment: AppointmentEntity, newDate: String, newTime: String) {
        viewModelScope.launch {
            repository.updateAppointmentStatus(appointment, "RESCHEDULED", newDate, newTime)
        }
    }

    fun cancelAppointment(appointment: AppointmentEntity, reason: String) {
        viewModelScope.launch {
            repository.updateAppointmentStatus(appointment, "CANCELLED", cancelReason = reason)
        }
    }

    fun completeAppointment(appointment: AppointmentEntity) {
        viewModelScope.launch {
            repository.updateAppointmentStatus(appointment, "COMPLETED")
        }
    }

    // Working Hours
    fun updateWorkingHours(list: List<WorkingHoursEntity>) {
        viewModelScope.launch {
            repository.updateWorkingHours(list)
        }
    }

    fun updateSingleDayHours(day: WorkingHoursEntity) {
        viewModelScope.launch {
            repository.updateSingleDay(day)
        }
    }

    // Service Areas
    fun saveServiceArea(area: ServiceAreaEntity) {
        viewModelScope.launch {
            if (area.id == 0L) {
                repository.insertServiceArea(area)
            } else {
                repository.updateServiceArea(area)
            }
        }
    }

    fun deleteServiceArea(area: ServiceAreaEntity) {
        viewModelScope.launch {
            repository.deleteServiceArea(area)
        }
    }

    // Announcements
    fun saveAnnouncement(announcement: AnnouncementEntity) {
        viewModelScope.launch {
            if (announcement.id == 0L) {
                repository.insertAnnouncement(announcement)
            } else {
                repository.updateAnnouncement(announcement)
            }
        }
    }

    fun deleteAnnouncement(announcement: AnnouncementEntity) {
        viewModelScope.launch {
            repository.deleteAnnouncement(announcement)
        }
    }

    // FAQs
    fun saveFaq(faq: FaqEntity) {
        viewModelScope.launch {
            if (faq.id == 0L) {
                repository.insertFaq(faq)
            } else {
                repository.updateFaq(faq)
            }
        }
    }

    fun deleteFaq(faq: FaqEntity) {
        viewModelScope.launch {
            repository.deleteFaq(faq)
        }
    }

    // Testimonials
    fun saveTestimonial(testimonial: TestimonialEntity) {
        viewModelScope.launch {
            if (testimonial.id == 0L) {
                repository.insertTestimonial(testimonial)
            } else {
                repository.updateTestimonial(testimonial)
            }
        }
    }

    fun deleteTestimonial(testimonial: TestimonialEntity) {
        viewModelScope.launch {
            repository.deleteTestimonial(testimonial)
        }
    }

    // Broadcast Push Notification
    fun broadcastPushNotification(title: String, message: String) {
        viewModelScope.launch {
            repository.broadcastNotification(title, message)
        }
    }
}
