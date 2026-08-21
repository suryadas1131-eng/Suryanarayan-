package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PhysioDao {
    // Doctor Profile
    @Query("SELECT * FROM doctor_profile WHERE id = 1 LIMIT 1")
    fun getDoctorProfile(): Flow<DoctorProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateDoctorProfile(profile: DoctorProfileEntity)

    // Services
    @Query("SELECT * FROM services ORDER BY displayOrder ASC, id ASC")
    fun getAllServices(): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services WHERE isEnabled = 1 ORDER BY displayOrder ASC, id ASC")
    fun getPublishedServices(): Flow<List<ServiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: ServiceEntity): Long

    @Update
    suspend fun updateService(service: ServiceEntity)

    @Delete
    suspend fun deleteService(service: ServiceEntity)

    // Articles
    @Query("SELECT * FROM articles ORDER BY id DESC")
    fun getAllArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE status = 'PUBLISHED' ORDER BY id DESC")
    fun getPublishedArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE id = :id LIMIT 1")
    fun getArticleById(id: Long): Flow<ArticleEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: ArticleEntity): Long

    @Update
    suspend fun updateArticle(article: ArticleEntity)

    @Delete
    suspend fun deleteArticle(article: ArticleEntity)

    // Exercises
    @Query("SELECT * FROM exercises ORDER BY id DESC")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE status = 'PUBLISHED' ORDER BY id DESC")
    fun getPublishedExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id = :id LIMIT 1")
    fun getExerciseById(id: Long): Flow<ExerciseEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity): Long

    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)

    @Delete
    suspend fun deleteExercise(exercise: ExerciseEntity)

    // Assigned Exercises
    @Query("SELECT * FROM assigned_exercises ORDER BY id DESC")
    fun getAllAssignedExercises(): Flow<List<AssignedExerciseEntity>>

    @Query("SELECT * FROM assigned_exercises WHERE patientEmailOrPhone = :identifier ORDER BY id DESC")
    fun getAssignedExercisesForPatient(identifier: String): Flow<List<AssignedExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignedExercise(assigned: AssignedExerciseEntity): Long

    @Update
    suspend fun updateAssignedExercise(assigned: AssignedExerciseEntity)

    @Delete
    suspend fun deleteAssignedExercise(assigned: AssignedExerciseEntity)

    // Appointments
    @Query("SELECT * FROM appointments ORDER BY id DESC")
    fun getAllAppointments(): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE patientEmail = :email OR patientPhone = :phone ORDER BY id DESC")
    fun getAppointmentsForPatient(email: String, phone: String): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE id = :id LIMIT 1")
    fun getAppointmentById(id: Long): Flow<AppointmentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity): Long

    @Update
    suspend fun updateAppointment(appointment: AppointmentEntity)

    @Delete
    suspend fun deleteAppointment(appointment: AppointmentEntity)

    // Working Hours
    @Query("SELECT * FROM working_hours")
    fun getWorkingHours(): Flow<List<WorkingHoursEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWorkingHours(hours: List<WorkingHoursEntity>)

    @Update
    suspend fun updateSingleDayWorkingHours(day: WorkingHoursEntity)

    // Service Areas
    @Query("SELECT * FROM service_areas ORDER BY areaName ASC")
    fun getAllServiceAreas(): Flow<List<ServiceAreaEntity>>

    @Query("SELECT * FROM service_areas WHERE isEnabled = 1 ORDER BY areaName ASC")
    fun getEnabledServiceAreas(): Flow<List<ServiceAreaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceArea(area: ServiceAreaEntity): Long

    @Update
    suspend fun updateServiceArea(area: ServiceAreaEntity)

    @Delete
    suspend fun deleteServiceArea(area: ServiceAreaEntity)

    // Announcements
    @Query("SELECT * FROM announcements ORDER BY id DESC")
    fun getAllAnnouncements(): Flow<List<AnnouncementEntity>>

    @Query("SELECT * FROM announcements WHERE isPublished = 1 ORDER BY id DESC")
    fun getPublishedAnnouncements(): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity): Long

    @Update
    suspend fun updateAnnouncement(announcement: AnnouncementEntity)

    @Delete
    suspend fun deleteAnnouncement(announcement: AnnouncementEntity)

    // FAQs
    @Query("SELECT * FROM faqs ORDER BY displayOrder ASC, id ASC")
    fun getAllFaqs(): Flow<List<FaqEntity>>

    @Query("SELECT * FROM faqs WHERE isPublished = 1 ORDER BY displayOrder ASC, id ASC")
    fun getPublishedFaqs(): Flow<List<FaqEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaq(faq: FaqEntity): Long

    @Update
    suspend fun updateFaq(faq: FaqEntity)

    @Delete
    suspend fun deleteFaq(faq: FaqEntity)

    // Testimonials
    @Query("SELECT * FROM testimonials ORDER BY id DESC")
    fun getAllTestimonials(): Flow<List<TestimonialEntity>>

    @Query("SELECT * FROM testimonials WHERE isPublished = 1 ORDER BY id DESC")
    fun getPublishedTestimonials(): Flow<List<TestimonialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestimonial(testimonial: TestimonialEntity): Long

    @Update
    suspend fun updateTestimonial(testimonial: TestimonialEntity)

    @Delete
    suspend fun deleteTestimonial(testimonial: TestimonialEntity)

    // Notifications
    @Query("SELECT * FROM notifications WHERE targetUserType = :targetType ORDER BY timestamp DESC")
    fun getNotificationsForTarget(targetType: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1 WHERE targetUserType = :targetType")
    suspend fun markAllNotificationsRead(targetType: String)

    // Activity Logs
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentActivityLogs(): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityLog(log: ActivityLogEntity): Long

    // Home Page Config
    @Query("SELECT * FROM homepage_config WHERE id = 1 LIMIT 1")
    fun getHomePageConfig(): Flow<HomePageConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateHomePageConfig(config: HomePageConfigEntity)
}
