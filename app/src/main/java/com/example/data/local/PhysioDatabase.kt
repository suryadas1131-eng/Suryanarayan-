package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        DoctorProfileEntity::class,
        ServiceEntity::class,
        ArticleEntity::class,
        ExerciseEntity::class,
        AssignedExerciseEntity::class,
        AppointmentEntity::class,
        WorkingHoursEntity::class,
        ServiceAreaEntity::class,
        AnnouncementEntity::class,
        FaqEntity::class,
        TestimonialEntity::class,
        NotificationEntity::class,
        ActivityLogEntity::class,
        HomePageConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PhysioDatabase : RoomDatabase() {
    abstract fun physioDao(): PhysioDao

    companion object {
        @Volatile
        private var INSTANCE: PhysioDatabase? = null

        fun getDatabase(context: Context): PhysioDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PhysioDatabase::class.java,
                    "physiocare_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.physioDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: PhysioDao) {
            // 1. Doctor Profile
            dao.updateDoctorProfile(
                DoctorProfileEntity(
                    id = 1,
                    name = "Dr. Satyaprakash Das",
                    title = "Senior Consultant Physiotherapist & Home Rehab Specialist",
                    qualifications = "MPT (Orthopaedics & Sports Rehab), BPT, MIAP",
                    certifications = "Certified Manual Therapist (CMT), Dry Needling Specialist, Kinesio-Taping Practitioner",
                    experience = "9+ Years Clinical & Home Care Experience",
                    specializations = "Orthopedic Rehabilitation, Post-Surgical Knee/Hip Rehab, Stroke Recovery, Spinal Alignment, Sports Injuries",
                    about = "Dr. Satyaprakash Das is a dedicated Senior Consultant Physiotherapist committed to delivering hospital-grade rehabilitation directly in the comfort of patients' homes. Specializing in joint mobilization, biomechanical correction, and individualized therapeutic exercise.",
                    treatmentPhilosophy = "Patient-centric rehabilitation focusing on pain alleviation, joint mobility restoration, and empowering patients with progressive functional independence.",
                    serviceArea = "Home Visits available across City Metro & Suburban Zones",
                    phone = "+91 9583948448",
                    whatsapp = "+91 9583948448",
                    email = "dassatya752@gmail.com"
                )
            )

            // 2. Home Page Config
            dao.updateHomePageConfig(
                HomePageConfigEntity(
                    id = 1,
                    heroHeading = "Professional Physiotherapy Care at Your Home",
                    heroSubtitle = "Hospital-grade rehabilitation, spine relief, and mobility recovery in the comfort and safety of your home.",
                    bookButtonText = "Book Home Visit"
                )
            )

            // 3. Services
            val initialServices = listOf(
                ServiceEntity(
                    name = "Home Physiotherapy Visit",
                    category = "Home Care",
                    description = "Comprehensive on-site physical evaluation, manual therapy, and tailored home rehabilitation protocols.",
                    displayOrder = 1,
                    feeInfo = "Standard Home Visit"
                ),
                ServiceEntity(
                    name = "Knee Rehabilitation & Post-Op Care",
                    category = "Orthopedics",
                    description = "Targeted protocols for ACL reconstruction, total knee replacement (TKR), and osteoarthritis management.",
                    displayOrder = 2,
                    feeInfo = "Specialized Knee Session"
                ),
                ServiceEntity(
                    name = "Back & Spine Pain Management",
                    category = "Spine Care",
                    description = "Lumbar decompression, core stabilization, sciatica relief, and postural alignment therapies.",
                    displayOrder = 3,
                    feeInfo = "Spinal Care Session"
                ),
                ServiceEntity(
                    name = "Shoulder Rehabilitation & Frozen Shoulder",
                    category = "Joint Mobility",
                    description = "Capsular release, rotator cuff strengthening, and scapular dyskinesis correction.",
                    displayOrder = 4,
                    feeInfo = "Shoulder Mobility Session"
                ),
                ServiceEntity(
                    name = "Stroke & Neurological Rehabilitation",
                    category = "Neurology",
                    description = "Neuro-developmental therapy (NDT), gait retraining, balance enhancement, and motor recovery.",
                    displayOrder = 5,
                    feeInfo = "Neuro Home Session"
                ),
                ServiceEntity(
                    name = "Elderly Fall Prevention & Mobility",
                    category = "Geriatric",
                    description = "Balance training, joint preservation, proprioceptive exercises, and safe functional mobility.",
                    displayOrder = 6,
                    feeInfo = "Geriatric Care"
                )
            )
            initialServices.forEach { dao.insertService(it) }

            // 4. Working Hours (Monday to Sunday)
            val workingHours = listOf(
                WorkingHoursEntity("Monday", true, "08:00 AM", "07:00 PM"),
                WorkingHoursEntity("Tuesday", true, "08:00 AM", "07:00 PM"),
                WorkingHoursEntity("Wednesday", true, "08:00 AM", "07:00 PM"),
                WorkingHoursEntity("Thursday", true, "08:00 AM", "07:00 PM"),
                WorkingHoursEntity("Friday", true, "08:00 AM", "07:00 PM"),
                WorkingHoursEntity("Saturday", true, "09:00 AM", "06:00 PM"),
                WorkingHoursEntity("Sunday", false, "Closed", "Closed")
            )
            dao.insertOrUpdateWorkingHours(workingHours)

            // 5. Service Areas
            val areas = listOf(
                ServiceAreaEntity(areaName = "Central City & Downtown", pincode = "751001", isEnabled = true),
                ServiceAreaEntity(areaName = "North Metro Zone", pincode = "751012", isEnabled = true),
                ServiceAreaEntity(areaName = "South Tech Park & Suburbs", pincode = "751024", isEnabled = true),
                ServiceAreaEntity(areaName = "East River District", pincode = "751016", isEnabled = true)
            )
            areas.forEach { dao.insertServiceArea(it) }

            // 6. Exercises (with 3D models and YouTube Video links)
            val exercises = listOf(
                ExerciseEntity(
                    name = "Isometric Quadriceps & Knee Extension",
                    bodyPart = "Knee",
                    category = "Strengthening",
                    difficulty = "Beginner",
                    description = "Fundamental exercise for strengthening the vastus medialis and stabilizing the patellofemoral joint without excessive joint stress.",
                    instructions = "1. Sit upright on a firm chair or lie flat with a small rolled towel under your knee.\n2. Tighten your thigh muscles and push the back of your knee down toward the surface.\n3. Straighten your leg fully while pulling your toes back toward your shin.\n4. Hold the contraction firmly for 5 seconds, then slowly lower.",
                    repetitions = "10 Repetitions",
                    duration = "3 Sets",
                    frequency = "2 times daily",
                    precautions = "Do not lock the knee forcefully. Avoid holding your breath during the isometric hold.",
                    commonMistakes = "Lifting the hip off the surface or letting the ankle twist inward.",
                    mediaType = "VIDEO_AND_3D",
                    youtubeUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                    model3dType = "KNEE_EXTENSION",
                    status = "PUBLISHED",
                    isFeatured = true
                ),
                ExerciseEntity(
                    name = "Lumbar Pelvic Tilt & Spine Stabilization",
                    bodyPart = "Back",
                    category = "Mobility",
                    difficulty = "Beginner",
                    description = "Gently mobilizes the lumbar spine and activates deep transversus abdominis muscles to relieve lower back tension.",
                    instructions = "1. Lie flat on your back with knees bent and feet flat on the floor.\n2. Gently flatten your lower back against the bed/mat by tightening abdominal muscles.\n3. Hold this position for 5 seconds while breathing normally.\n4. Return smoothly to the neutral starting posture.",
                    repetitions = "12 Repetitions",
                    duration = "2 Sets",
                    frequency = "Twice daily (Morning & Evening)",
                    precautions = "Keep movements smooth and pain-free. Do not push through sharp radiating back pain.",
                    commonMistakes = "Pressing down through the feet instead of engaging the deep core muscles.",
                    mediaType = "3D_INTERACTIVE",
                    model3dType = "SPINE_FLEXION",
                    status = "PUBLISHED",
                    isFeatured = true
                ),
                ExerciseEntity(
                    name = "Scapular Retraction & Shoulder Abduction Arc",
                    bodyPart = "Shoulder",
                    category = "Mobility",
                    difficulty = "Intermediate",
                    description = "Restores glenohumeral rhythm, reduces rotator cuff impingement, and strengthens lower trapezius muscles.",
                    instructions = "1. Stand or sit tall with shoulders relaxed and arms at your sides.\n2. Squeeze your shoulder blades together and slightly downward.\n3. Slowly elevate arms out to the sides in the scapular plane (30° forward) up to shoulder height.\n4. Lower with controlled speed.",
                    repetitions = "10 Reps",
                    duration = "3 Sets",
                    frequency = "Daily",
                    precautions = "Do not shrug your shoulders toward your ears during elevation.",
                    commonMistakes = "Arching the lower back or leaning backwards.",
                    mediaType = "VIDEO_AND_3D",
                    youtubeUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                    model3dType = "SHOULDER_ABDUCTION",
                    status = "PUBLISHED",
                    isFeatured = true
                ),
                ExerciseEntity(
                    name = "Cervical Chin Tucks & Neck Rotation",
                    bodyPart = "Neck",
                    category = "Post-Op",
                    difficulty = "Beginner",
                    description = "Corrects forward head posture and strengthens deep cervical flexors to alleviate neck stiffness and tension headaches.",
                    instructions = "1. Sit straight with eyes looking directly forward.\n2. Gently draw your chin straight back toward your neck (making a gentle double chin) without tilting head down.\n3. Hold for 5 seconds, then slowly turn head 30 degrees to left and right.",
                    repetitions = "10 Reps",
                    duration = "2 Sets",
                    frequency = "Every 3-4 hours during desk work",
                    precautions = "Do not tilt your head down toward the chest; slide horizontally backward.",
                    commonMistakes = "Bending the neck rather than translating back.",
                    mediaType = "3D_INTERACTIVE",
                    model3dType = "NECK_ROTATION",
                    status = "PUBLISHED",
                    isFeatured = false
                ),
                ExerciseEntity(
                    name = "Ankle Dorsiflexion & Plantar Pumps",
                    bodyPart = "Ankle",
                    category = "Strengthening",
                    difficulty = "Beginner",
                    description = "Promotes circulatory venous return, prevents DVT post-surgery, and improves ankle joint mobility.",
                    instructions = "1. Sit or lie comfortably with leg extended.\n2. Point toes away from you as far as comfortable, then pull toes back toward your shin.\n3. Perform smooth rhythmic pumps.",
                    repetitions = "20 Reps",
                    duration = "3 Sets",
                    frequency = "Hourly after prolonged sitting or surgery",
                    precautions = "Perform within pain-free active range of motion.",
                    commonMistakes = "Moving only the toes instead of flexing the entire ankle joint.",
                    mediaType = "3D_INTERACTIVE",
                    model3dType = "ANKLE_DORSIFLEXION",
                    status = "PUBLISHED",
                    isFeatured = false
                )
            )
            exercises.forEach { dao.insertExercise(it) }

            // 7. Articles
            val articles = listOf(
                ArticleEntity(
                    title = "5 Proven Ways to Relieve Lower Back Pain at Home",
                    category = "Back Pain",
                    shortDescription = "Evidence-based daily habits and gentle movements that decompress the spine and alleviate chronic stiffness.",
                    content = """Lower back pain affects over 80% of adults at some point in life. While resting might feel intuitive, clinical research consistently shows that active recovery and gentle mobilization yield faster, longer-lasting relief.

1. Maintain Neutral Spinal Alignment:
Avoid slumping into soft couches for prolonged hours. Use a lumbar support roll behind your lower back to preserve the natural lordotic curve.

2. Gentle Decompression:
Lying flat on a firm surface with legs elevated at 90 degrees on a chair can immediately reduce intradiscal pressure by up to 70%.

3. Active Glute & Core Activation:
Weak gluteal muscles force lumbar extensor muscles to overcompensate. Performing gentle bridge exercises and pelvic tilts stabilizes the pelvis.

4. Micro-Breaks Every 45 Minutes:
Static sitting restricts blood flow and stiffens spinal ligaments. Stand, stretch, and walk for 60 seconds every 45 minutes.

5. Personalized Assessment:
If pain radiates down the leg, causes numbness, or persists beyond 2 weeks, schedule a comprehensive in-home physiotherapy evaluation.""",
                    author = "Dr. Satyaprakash Das",
                    publishedDate = "Aug 20, 2026",
                    readingTime = "4 min read",
                    status = "PUBLISHED",
                    isFeatured = true
                ),
                ArticleEntity(
                    title = "Knee Replacement Rehab: What to Expect in Weeks 1 to 6",
                    category = "Knee",
                    shortDescription = "A complete step-by-step roadmap for post-operative knee recovery, swelling management, and walking independence.",
                    content = """Undergoing a Total Knee Arthroplasty (TKA) is a transformative step toward pain-free mobility. However, the success of surgery is 50% surgeon skill and 50% disciplined physiotherapy rehabilitation.

Week 1-2: Controlling Swelling & Gaining Extension
The primary focus is full extension (straightening) and managing edema with cryotherapy (ice) and elevation. Daily ankle pumps and isometric quads are paramount.

Week 3-4: Progressive Flexion & Gait Training
We aim for 90 to 110 degrees of knee flexion. Transitioning from walker to single crutch/cane under supervised guidance ensures correct gait pattern without limping.

Week 5-6: Functional Strengthening & Balance
Focus shifts to step-ups, stationary cycling, and dynamic balance drills for independent stair climbing and confident outdoor walking.""",
                    author = "Dr. Satyaprakash Das",
                    publishedDate = "Aug 15, 2026",
                    readingTime = "5 min read",
                    status = "PUBLISHED",
                    isFeatured = true
                ),
                ArticleEntity(
                    title = "Understanding Ergonomics: Preventing 'Tech Neck' & Shoulder Fatigue",
                    category = "Neck & Shoulder",
                    shortDescription = "How modern screen angles strain cervical vertebrae and the simple postural resets you can do at your desk.",
                    content = """For every inch your head moves forward, the effective weight of your head on the cervical spine doubles. At a 45-degree downward tilt while looking at a phone, your neck sustains approximately 49 lbs (22 kg) of continuous compressive load!

Key Postural Guidelines:
- Elevate screen so the top third is at eye level.
- Keep elbows supported at 90 degrees.
- Perform 10 chin tucks every two hours.
- Incorporate scapular pinches to counteract rounded forward shoulders.""",
                    author = "Dr. Satyaprakash Das",
                    publishedDate = "Aug 10, 2026",
                    readingTime = "3 min read",
                    status = "PUBLISHED",
                    isFeatured = false
                )
            )
            articles.forEach { dao.insertArticle(it) }

            // 8. Sample Assigned Exercises (for demo patient)
            val assigned = listOf(
                AssignedExerciseEntity(
                    patientEmailOrPhone = "suryadas1131@gmail.com",
                    patientName = "Surya Das",
                    exerciseId = 1,
                    exerciseName = "Isometric Quadriceps & Knee Extension",
                    bodyPart = "Knee",
                    repetitions = "12 Reps",
                    duration = "3 Sets",
                    frequency = "2 times daily (Morning & Evening)",
                    startDate = "Aug 20, 2026",
                    physiotherapistInstructions = "Maintain 5 second firm hold on each repetition. Keep knee straight. Contact if pain exceeds 3/10.",
                    isCompletedToday = false
                ),
                AssignedExerciseEntity(
                    patientEmailOrPhone = "suryadas1131@gmail.com",
                    patientName = "Surya Das",
                    exerciseId = 2,
                    exerciseName = "Lumbar Pelvic Tilt & Spine Stabilization",
                    bodyPart = "Back",
                    repetitions = "10 Reps",
                    duration = "2 Sets",
                    frequency = "Once before bedtime",
                    startDate = "Aug 20, 2026",
                    physiotherapistInstructions = "Perform gently on firm mattress or yoga mat. Focus on deep diaphragmatic breathing.",
                    isCompletedToday = false
                )
            )
            assigned.forEach { dao.insertAssignedExercise(it) }

            // 9. Sample Appointments
            val sampleAppointments = listOf(
                AppointmentEntity(
                    bookingId = "PHY-2608-01",
                    patientName = "Surya Das",
                    patientPhone = "+91 9583948448",
                    patientEmail = "suryadas1131@gmail.com",
                    serviceName = "Home Physiotherapy Visit",
                    date = "Tomorrow",
                    timeSlot = "10:00 AM - 11:00 AM",
                    homeAddress = "Flat 402, Green Valley Enclave, Metro Road",
                    reason = "Post-work acute knee stiffness and lower back pain assessment.",
                    status = "CONFIRMED"
                ),
                AppointmentEntity(
                    bookingId = "PHY-2608-02",
                    patientName = "Ramesh Sharma",
                    patientPhone = "+91 9876543210",
                    patientEmail = "ramesh.sharma@example.com",
                    serviceName = "Knee Rehabilitation & Post-Op Care",
                    date = "Aug 22, 2026",
                    timeSlot = "02:00 PM - 03:00 PM",
                    homeAddress = "House 12, Sector 4, North Metro Zone",
                    reason = "Post ACL surgery day 14 follow-up and range-of-motion measurement.",
                    status = "PENDING"
                )
            )
            sampleAppointments.forEach { dao.insertAppointment(it) }

            // 10. FAQs
            val faqs = listOf(
                FaqEntity(
                    question = "How does a Home Physiotherapy Visit work?",
                    answer = "The physiotherapist arrives directly at your residence with necessary treatment modalities (portable electrotherapy, mobilization tools, resistance bands). The session includes a full physical assessment, hands-on treatment, and guided exercises.",
                    displayOrder = 1
                ),
                FaqEntity(
                    question = "What should I prepare before the home visit?",
                    answer = "Wear comfortable, loose clothing that allows easy movement and examination of the affected joint. Keep any previous doctor reports, X-rays, or MRI scans ready.",
                    displayOrder = 2
                ),
                FaqEntity(
                    question = "How many sessions will I need?",
                    answer = "The total number of sessions depends on your specific clinical condition, severity, and healing rate. Dr. Das will outline a personalized rehabilitation roadmap after the initial physical assessment.",
                    displayOrder = 3
                ),
                FaqEntity(
                    question = "How do I follow my assigned exercises at home?",
                    answer = "Open the 'Exercises' tab in this app to see your custom prescribed routine with precise repetitions, duration, and interactive 3D / video movement guides.",
                    displayOrder = 4
                )
            )
            faqs.forEach { dao.insertFaq(it) }

            // 11. Testimonials
            val testimonials = listOf(
                TestimonialEntity(
                    patientName = "Anita Mishra",
                    review = "Dr. Satyaprakash is extraordinarily thorough and patient. After my total knee replacement, his home visits got me walking without support in just 3 weeks!",
                    conditionTreated = "Post-TKR Knee Rehab",
                    rating = 5
                ),
                TestimonialEntity(
                    patientName = "Rajesh Verma",
                    review = "Severe sciatica had made sitting at my desk impossible. Dr. Das diagnosed the exact spinal compression and the exercises worked wonders.",
                    conditionTreated = "Lumbar Disc Herniation",
                    rating = 5
                ),
                TestimonialEntity(
                    patientName = "Prakash Patnaik",
                    review = "Prompt, professional, and very knowledgeable. Having quality physiotherapy at home made all the difference for my elderly father.",
                    conditionTreated = "Geriatric Mobility & Balance",
                    rating = 5
                )
            )
            testimonials.forEach { dao.insertTestimonial(it) }

            // 12. Announcements
            dao.insertAnnouncement(
                AnnouncementEntity(
                    title = "Home Visit Booking Available Across All Zones",
                    message = "Slots for morning and evening home physiotherapy sessions are now open for online booking. Book your slot directly from the app.",
                    startDate = "Aug 20",
                    endDate = "Ongoing",
                    isPublished = true,
                    isImportant = true
                )
            )

            // 13. Notifications
            val notifications = listOf(
                NotificationEntity(
                    targetUserType = "USER",
                    patientEmailOrPhone = "suryadas1131@gmail.com",
                    title = "Appointment Confirmed",
                    message = "Your home visit appointment for Tomorrow at 10:00 AM has been confirmed by Dr. Das.",
                    type = "CONFIRMATION"
                ),
                NotificationEntity(
                    targetUserType = "USER",
                    patientEmailOrPhone = "suryadas1131@gmail.com",
                    title = "New Exercises Prescribed",
                    message = "Dr. Das has assigned 2 personalized rehabilitation exercises to your account.",
                    type = "EXERCISE"
                ),
                NotificationEntity(
                    targetUserType = "ADMIN",
                    title = "New Booking Received",
                    message = "Surya Das submitted a booking request for Home Physiotherapy Visit.",
                    type = "BOOKING"
                )
            )
            notifications.forEach { dao.insertNotification(it) }

            // 14. Activity Log
            dao.insertActivityLog(
                ActivityLogEntity(
                    actionTitle = "System Initialized",
                    detail = "Database populated with clinical services, exercises, and doctor profile.",
                    performedBy = "System"
                )
            )
        }
    }
}
