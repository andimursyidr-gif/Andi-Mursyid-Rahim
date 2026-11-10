package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.local.SmartLearningDao
import com.example.data.model.*
import com.example.data.util.ExcelParser
import com.example.data.util.ImportResult
import com.example.data.util.PdfReportGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SmartLearningRepository(
    private val context: Context,
    private val dao: SmartLearningDao
) {
    // Classes
    val classes: Flow<List<ClassEntity>> = dao.getAllClasses()
    suspend fun insertClass(c: ClassEntity): Long = dao.insertClass(c)
    suspend fun updateClass(c: ClassEntity) = dao.updateClass(c)
    suspend fun deleteClass(c: ClassEntity) = dao.deleteClass(c)

    // Students
    val students: Flow<List<StudentEntity>> = dao.getAllStudents()
    fun getStudentsByClass(classId: Long): Flow<List<StudentEntity>> = dao.getStudentsByClass(classId)
    suspend fun insertStudent(s: StudentEntity): Long = dao.insertStudent(s)
    suspend fun updateStudent(s: StudentEntity) = dao.updateStudent(s)
    suspend fun deleteStudent(s: StudentEntity) = dao.deleteStudent(s)
    suspend fun deleteStudentById(id: Long) = dao.deleteStudentById(id)

    // Attendance
    fun getAttendanceByClassAndDate(classId: Long, date: String): Flow<List<AttendanceEntity>> =
        dao.getAttendanceByClassAndDate(classId, date)
    fun getAllAttendanceByClass(classId: Long): Flow<List<AttendanceEntity>> =
        dao.getAllAttendanceByClass(classId)
    fun getAttendanceForStudent(studentId: Long): Flow<List<AttendanceEntity>> =
        dao.getAttendanceForStudent(studentId)
    suspend fun saveAttendance(list: List<AttendanceEntity>) = dao.insertAttendance(list)

    // Videos
    val videos: Flow<List<VideoEntity>> = dao.getAllVideos()
    fun getVideosByClass(classId: Long): Flow<List<VideoEntity>> = dao.getVideosByClass(classId)
    suspend fun insertVideo(v: VideoEntity): Long = dao.insertVideo(v)
    suspend fun updateVideo(v: VideoEntity) = dao.updateVideo(v)
    suspend fun deleteVideo(v: VideoEntity) = dao.deleteVideo(v)
    suspend fun markVideoWatched(id: Long, isWatched: Boolean) = dao.markVideoWatched(id, isWatched)

    // Modules
    val modules: Flow<List<TeachingModuleEntity>> = dao.getAllModules()
    fun getModulesByClass(classId: Long): Flow<List<TeachingModuleEntity>> = dao.getModulesByClass(classId)
    suspend fun insertModule(m: TeachingModuleEntity): Long = dao.insertModule(m)
    suspend fun updateModule(m: TeachingModuleEntity) = dao.updateModule(m)
    suspend fun deleteModule(m: TeachingModuleEntity) = dao.deleteModule(m)
    suspend fun markModuleCompleted(id: Long, completed: Boolean) = dao.markModuleCompleted(id, completed)

    // Media
    val mediaList: Flow<List<LearningMediaEntity>> = dao.getAllMedia()
    fun getMediaByClass(classId: Long): Flow<List<LearningMediaEntity>> = dao.getMediaByClass(classId)
    suspend fun insertMedia(m: LearningMediaEntity): Long = dao.insertMedia(m)
    suspend fun deleteMedia(m: LearningMediaEntity) = dao.deleteMedia(m)

    // Worksheets (LKPD)
    val worksheets: Flow<List<WorksheetEntity>> = dao.getAllWorksheets()
    fun getWorksheetsByClass(classId: Long): Flow<List<WorksheetEntity>> = dao.getWorksheetsByClass(classId)
    suspend fun insertWorksheet(w: WorksheetEntity): Long = dao.insertWorksheet(w)
    suspend fun updateWorksheet(w: WorksheetEntity) = dao.updateWorksheet(w)
    suspend fun deleteWorksheet(w: WorksheetEntity) = dao.deleteWorksheet(w)

    // Submissions
    fun getSubmissionsForWorksheet(wId: Long): Flow<List<WorksheetSubmissionEntity>> =
        dao.getSubmissionsForWorksheet(wId)
    fun getSubmissionsForStudent(studentId: Long): Flow<List<WorksheetSubmissionEntity>> =
        dao.getSubmissionsForStudent(studentId)
    suspend fun getSubmission(wId: Long, studentId: Long): WorksheetSubmissionEntity? =
        dao.getSubmission(wId, studentId)
    suspend fun saveSubmission(sub: WorksheetSubmissionEntity): Long =
        dao.insertOrUpdateSubmission(sub)

    // Quizzes & Questions
    val quizzes: Flow<List<QuizEntity>> = dao.getAllQuizzes()
    fun getQuizzesByClass(classId: Long): Flow<List<QuizEntity>> = dao.getQuizzesByClass(classId)
    suspend fun getQuizById(id: Long): QuizEntity? = dao.getQuizById(id)
    suspend fun insertQuiz(q: QuizEntity): Long = dao.insertQuiz(q)
    suspend fun updateQuiz(q: QuizEntity) = dao.updateQuiz(q)
    suspend fun deleteQuiz(q: QuizEntity) = dao.deleteQuiz(q)

    fun getQuestionsForQuiz(quizId: Long): Flow<List<QuestionEntity>> = dao.getQuestionsForQuiz(quizId)
    suspend fun getQuestionsListForQuiz(quizId: Long): List<QuestionEntity> = dao.getQuestionsListForQuiz(quizId)
    suspend fun insertQuestions(list: List<QuestionEntity>) = dao.insertQuestions(list)

    // Quiz Attempts
    val allAttempts: Flow<List<QuizAttemptEntity>> = dao.getAllAttempts()
    fun getAttemptsForQuiz(quizId: Long): Flow<List<QuizAttemptEntity>> = dao.getAttemptsForQuiz(quizId)
    fun getAttemptsForStudent(studentId: Long): Flow<List<QuizAttemptEntity>> = dao.getAttemptsForStudent(studentId)
    suspend fun saveQuizAttempt(attempt: QuizAttemptEntity): Long = dao.insertAttempt(attempt)

    // Announcements
    val announcements: Flow<List<AnnouncementEntity>> = dao.getAllAnnouncements()
    fun getAnnouncementsForClass(classId: Long): Flow<List<AnnouncementEntity>> = dao.getAnnouncementsForClass(classId)
    suspend fun insertAnnouncement(a: AnnouncementEntity): Long = dao.insertAnnouncement(a)
    suspend fun deleteAnnouncement(a: AnnouncementEntity) = dao.deleteAnnouncement(a)

    // Settings
    val settings: Flow<SchoolSettingsEntity?> = dao.getSettings()
    suspend fun updateSettings(s: SchoolSettingsEntity) = dao.insertOrUpdateSettings(s)

    /**
     * Parse and import students from an Excel / CSV file
     */
    suspend fun importStudentsFromFile(
        uri: Uri,
        fileName: String,
        targetClassId: Long,
        targetClassName: String
    ): Pair<ImportResult, Int> = withContext(Dispatchers.IO) {
        val currentStudents = dao.getAllStudents().firstOrNull() ?: emptyList()
        val existingNis = currentStudents.map { it.nis }.toSet()

        val parsed = ExcelParser.parseFile(context, uri, fileName, existingNis)
        var savedCount = 0

        val entitiesToSave = parsed.rows
            .filter { it.isValid && !it.isDuplicate }
            .map { row ->
                StudentEntity(
                    nis = row.nis,
                    nisn = row.nisn,
                    fullName = row.fullName,
                    gender = row.gender,
                    birthPlace = row.birthPlace,
                    birthDate = row.birthDate,
                    classId = targetClassId,
                    className = targetClassName,
                    address = row.address,
                    parentPhone = row.parentPhone
                )
            }

        if (entitiesToSave.isNotEmpty()) {
            dao.insertStudents(entitiesToSave)
            savedCount = entitiesToSave.size
        }

        Pair(parsed, savedCount)
    }

    /**
     * Export students to CSV
     */
    suspend fun exportStudentsToCsv(classId: Long?): File = withContext(Dispatchers.IO) {
        val all = dao.getAllStudents().firstOrNull() ?: emptyList()
        val filtered = if (classId != null && classId > 0) all.filter { it.classId == classId } else all

        val outputDir = context.getExternalFilesDir(null) ?: context.filesDir
        val file = File(outputDir, "Daftar_Siswa_${System.currentTimeMillis()}.csv")

        FileOutputStream(file).use { fos ->
            fos.write("No,NIS,NISN,Nama Lengkap,Jenis Kelamin,Tempat Lahir,Tanggal Lahir,Kelas,Alamat,No HP Ortu\n".toByteArray())
            filtered.forEachIndexed { idx, s ->
                val line = "${idx + 1},\"${s.nis}\",\"${s.nisn}\",\"${s.fullName}\",\"${s.gender}\",\"${s.birthPlace}\",\"${s.birthDate}\",\"${s.className}\",\"${s.address}\",\"${s.parentPhone}\"\n"
                fos.write(line.toByteArray())
            }
        }
        file
    }

    /**
     * Generate PDF Student List
     */
    suspend fun generateStudentListPdf(classId: Long?): File = withContext(Dispatchers.IO) {
        val classInfo = if (classId != null && classId > 0) dao.getClassById(classId) else null
        val all = dao.getAllStudents().firstOrNull() ?: emptyList()
        val filtered = if (classId != null && classId > 0) all.filter { it.classId == classId } else all
        PdfReportGenerator.generateStudentListPdf(context, classInfo, filtered)
    }

    /**
     * Generate PDF Academic Report
     */
    suspend fun generateAcademicReportPdf(classId: Long?): File = withContext(Dispatchers.IO) {
        val classInfo = if (classId != null && classId > 0) dao.getClassById(classId) else null
        val students = if (classId != null && classId > 0) {
            dao.getStudentsByClass(classId).firstOrNull() ?: emptyList()
        } else {
            dao.getAllStudents().firstOrNull() ?: emptyList()
        }

        val attempts = dao.getAllAttempts().firstOrNull() ?: emptyList()
        val avgQuiz = if (attempts.isNotEmpty()) attempts.map { it.score }.average() else 85.0

        val attendance = if (classId != null && classId > 0) {
            dao.getAllAttendanceByClass(classId).firstOrNull() ?: emptyList()
        } else emptyList()

        PdfReportGenerator.generateAcademicReportPdf(
            context = context,
            classInfo = classInfo,
            studentCount = students.size,
            quizAverage = avgQuiz,
            worksheetAverage = 88.5,
            completionRate = 92,
            attendanceList = attendance
        )
    }

    /**
     * Pre-populates the database with rich educational data if it's currently empty
     */
    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val existingClasses = dao.getAllClasses().firstOrNull()
        if (!existingClasses.isNullOrEmpty()) return@withContext

        // 1. Classes
        val c1Id = dao.insertClass(
            ClassEntity(
                className = "X RPL 1",
                academicYear = "2024/2025",
                semester = "Ganjil",
                teacherName = "Drs. H. Mursyid, M.Pd.",
                description = "Kelas Rekayasa Perangkat Lunak 1 - Konsentrasi Mobile & Web"
            )
        )
        val c2Id = dao.insertClass(
            ClassEntity(
                className = "XI RPL 2",
                academicYear = "2024/2025",
                semester = "Ganjil",
                teacherName = "Drs. H. Mursyid, M.Pd.",
                description = "Kelas Rekayasa Perangkat Lunak 2 - Pemrograman Lanjutan"
            )
        )
        val c3Id = dao.insertClass(
            ClassEntity(
                className = "X DKV 1",
                academicYear = "2024/2025",
                semester = "Ganjil",
                teacherName = "Siti Rahmawati, S.Kom.",
                description = "Kelas Desain Komunikasi Visual & Multimedia"
            )
        )

        // 2. Students
        val sampleStudents = listOf(
            StudentEntity(nis = "002101", nisn = "0081234567", fullName = "Ahmad Fauzi", gender = "L", birthPlace = "Jakarta", birthDate = "2008-05-12", classId = c1Id, className = "X RPL 1", address = "Jl. Merdeka No. 12", parentPhone = "081234567890"),
            StudentEntity(nis = "002102", nisn = "0081234568", fullName = "Dewi Rahmawati", gender = "P", birthPlace = "Bandung", birthDate = "2008-08-20", classId = c1Id, className = "X RPL 1", address = "Jl. Mawar Indah No. 5", parentPhone = "081298765432"),
            StudentEntity(nis = "002103", nisn = "0081234569", fullName = "Budi Santoso", gender = "L", birthPlace = "Surabaya", birthDate = "2008-02-17", classId = c1Id, className = "X RPL 1", address = "Jl. Diponegoro No. 88", parentPhone = "081377889900"),
            StudentEntity(nis = "002104", nisn = "0081234570", fullName = "Siti Nurhaliza", gender = "P", birthPlace = "Semarang", birthDate = "2008-11-03", classId = c1Id, className = "X RPL 1", address = "Jl. Melati No. 45", parentPhone = "081922334455"),
            StudentEntity(nis = "002105", nisn = "0081234571", fullName = "Rizky Pratama", gender = "L", birthPlace = "Makassar", birthDate = "2008-09-25", classId = c1Id, className = "X RPL 1", address = "Jl. Hasanuddin No. 10", parentPhone = "081566778899"),
            StudentEntity(nis = "002106", nisn = "0081234572", fullName = "Anisa Putri", gender = "P", birthPlace = "Yogyakarta", birthDate = "2008-04-14", classId = c1Id, className = "X RPL 1", address = "Jl. Malioboro No. 3", parentPhone = "081800112233"),
            StudentEntity(nis = "002201", nisn = "0071234560", fullName = "Dimas Anggara", gender = "L", birthPlace = "Solo", birthDate = "2007-03-10", classId = c2Id, className = "XI RPL 2", address = "Jl. Slamet Riyadi No. 7", parentPhone = "081211223344"),
            StudentEntity(nis = "002202", nisn = "0071234561", fullName = "Nadia Safitri", gender = "P", birthPlace = "Malang", birthDate = "2007-06-22", classId = c2Id, className = "XI RPL 2", address = "Jl. Ijen No. 19", parentPhone = "081322334455")
        )
        dao.insertStudents(sampleStudents)

        // 3. Attendance
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val attRecords = sampleStudents.map { s ->
            AttendanceEntity(
                studentId = s.id,
                studentName = s.fullName,
                classId = s.classId,
                date = today,
                status = if (s.fullName.contains("Budi")) "SAKIT" else "HADIR",
                note = if (s.fullName.contains("Budi")) "Surat dokter terlampir" else "Tepat waktu"
            )
        }
        dao.insertAttendance(attRecords)

        // 4. Videos
        dao.insertVideo(
            VideoEntity(
                title = "Pengenalan Arsitektur Mobile & Jetpack Compose",
                description = "Pelajari konsep declarative UI, state management, dan arsitektur MVVM di Android modern.",
                subject = "Pemrograman Perangkat Bergerak",
                classId = c1Id,
                className = "X RPL 1",
                topic = "Mobile Development",
                youtubeUrl = "https://www.youtube.com/watch?v=kYJzXJ6yBfU",
                durationText = "24 Menit",
                isWatched = true
            )
        )
        dao.insertVideo(
            VideoEntity(
                title = "Dasar Algoritma & Struktur Data Bersih",
                description = "Memahami efisiensi algoritma Big-O, array, stack, queue, dan implementasi rekursif.",
                subject = "Dasar-Dasar Kejuruan RPL",
                classId = c1Id,
                className = "X RPL 1",
                topic = "Algoritma & Logika",
                youtubeUrl = "https://www.youtube.com/watch?v=RBSGKlAvoiM",
                durationText = "18 Menit",
                isWatched = false
            )
        )
        dao.insertVideo(
            VideoEntity(
                title = "Manajemen Database Relasional dengan SQLite & Room",
                description = "Tutorial lengkap membuat Entity, DAO, Repository, dan observasi data Flow di Android.",
                subject = "Basis Data",
                classId = c2Id,
                className = "XI RPL 2",
                topic = "Basis Data",
                youtubeUrl = "https://www.youtube.com/watch?v=lwAvI3WDXBM",
                durationText = "32 Menit",
                isWatched = false
            )
        )

        // 5. Teaching Modules
        dao.insertModule(
            TeachingModuleEntity(
                title = "Modul Ajar 01: Fondasi Pemrograman Kotlin & Compose",
                description = "Panduan terstruktur belajar sintaks Kotlin, Coroutines, Lambda, dan Komposisi UI.",
                subject = "Pemrograman Bergerak",
                classId = c1Id,
                className = "X RPL 1",
                topic = "Kotlin Core",
                learningObjectives = "1. Siswa memahami variabel, control flow, dan OOP di Kotlin.\n2. Siswa mampu membangun screen pertama menggunakan Jetpack Compose.\n3. Siswa memahami konsep declarative UI dan State.",
                fileType = "PDF",
                fileSize = "3.8 MB",
                contentSummary = "BAB 1: Pengenalan Bahasa Kotlin\nBAB 2: Fungsi, Class, dan Null Safety\nBAB 3: State & Recomposition di Jetpack Compose\nBAB 4: Studi Kasus Aplikasi Edukasi",
                isCompleted = true
            )
        )
        dao.insertModule(
            TeachingModuleEntity(
                title = "Modul Ajar 02: Desain Sistem UI/UX & Material Design 3",
                description = "Standar pedoman desain M3, hierarki tipografi, color scheme, dan aksesibilitas ramah pengguna.",
                subject = "Desain Antarmuka",
                classId = c1Id,
                className = "X RPL 1",
                topic = "UI/UX Design",
                learningObjectives = "1. Menerapkan skema warna dinamis dan kontras yang ramah aksesibilitas.\n2. Memahami touch target minimal 48dp.\n3. Mampu membuat wireframe dan prototipe interaktif.",
                fileType = "PDF",
                fileSize = "5.1 MB",
                contentSummary = "Materi panduan teknis penerapan Material You dan perancangan antarmuka digital untuk pendidikan.",
                isCompleted = false
            )
        )

        // 6. Learning Media
        dao.insertMedia(
            LearningMediaEntity(
                title = "Infografis Arsitektur Komputer & Siklus CPU",
                description = "Diagram visual alur instruksi Fetch-Decode-Execute dan hierarki memori.",
                category = "INFOGRAFIS",
                mediaUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475",
                classId = c1Id,
                className = "X RPL 1"
            )
        )
        dao.insertMedia(
            LearningMediaEntity(
                title = "Slide Presentasi Pemodelan Basis Data ERD",
                description = "Dokumen presentasi pengenalan Entitas, Atribut, Kardinalitas Relasi, dan Normalisasi 3NF.",
                category = "PRESENTASI",
                mediaUrl = "https://drive.google.com/sample_erd_presentation",
                classId = c1Id,
                className = "X RPL 1"
            )
        )
        dao.insertMedia(
            LearningMediaEntity(
                title = "Simulasi Interaktif Gerbang Logika Digital",
                description = "Laboratorium virtual untuk menguji gerbang AND, OR, NOT, NAND, XOR secara real-time.",
                category = "INTERAKTIF",
                mediaUrl = "https://circuitverse.org/simulator",
                classId = c1Id,
                className = "X RPL 1"
            )
        )

        // 7. Worksheets (LKPD)
        val lkpdId = dao.insertWorksheet(
            WorksheetEntity(
                title = "LKPD 01: Perancangan Antarmuka Berbasis Komponen",
                instructions = "1. Bacalah modul ajar bab 2 tentang Layout & Modifiers.\n2. Buatlah rancangan layout antarmuka halaman profil siswa.\n3. Tuliskan kode deklarasi composable dan penjelasan hierarki tata letaknya.\n4. Kumpulkan sebelum batas waktu.",
                classId = c1Id,
                className = "X RPL 1",
                subject = "Pemrograman Bergerak",
                relatedTopic = "Jetpack Compose Basics",
                startDate = "2024-11-01",
                dueDate = "2024-11-20",
                answerType = "TEXT",
                maxScore = 100
            )
        )

        // Submissions for LKPD
        dao.insertOrUpdateSubmission(
            WorksheetSubmissionEntity(
                worksheetId = lkpdId,
                studentId = 1L,
                studentName = "Ahmad Fauzi",
                submissionDate = "2024-11-08 14:30",
                answerText = "Saya telah menyelesaikan rancangan antarmuka profil menggunakan Column, Card dengan RoundedCornerShape(16.dp), Image dengan CircleShape, serta tombol aksi dengan modifier testTag.",
                status = "DINILAI",
                score = 95,
                feedback = "Sangat baik! Struktur kode rapi dan sudah menerapkan Material Design 3 dengan tepat."
            )
        )

        // 8. Quizzes & Questions
        val quizId = dao.insertQuiz(
            QuizEntity(
                title = "Kuis Harian 01: Dasar Pemrograman Kotlin & Compose",
                description = "Evaluasi pemahaman konsep pemrograman deklaratif, variabel, dan alur aplikasi Android.",
                classId = c1Id,
                className = "X RPL 1",
                subject = "Pemrograman Bergerak",
                topic = "Kotlin & Compose",
                durationMinutes = 15,
                maxAttempts = 2,
                executionDate = "2024-11-15",
                minimumPassingScore = 75,
                isActive = true
            )
        )

        val questions = listOf(
            QuestionEntity(
                quizId = quizId,
                questionText = "Apa fungsi utama dari anotasi @Composable dalam Jetpack Compose?",
                questionType = "SINGLE_CHOICE",
                options = "Menandai fungsi untuk mendefinisikan UI secara deklaratif|||Menjalankan fungsi di latar belakang|||Menghubungkan aplikasi ke database SQLite|||Mengatur izin perangkat secara otomatis",
                correctAnswer = "Menandai fungsi untuk mendefinisikan UI secara deklaratif",
                points = 25,
                explanation = "Anotasi @Composable memberitahu compiler Compose bahwa fungsi tersebut bertugas mengubah data menjadi representasi antarmuka pengguna."
            ),
            QuestionEntity(
                quizId = quizId,
                questionText = "Jetpack Compose menggunakan paradigma imperatif dalam pembaruan tampilan.",
                questionType = "TRUE_FALSE",
                options = "Benar|||Salah",
                correctAnswer = "Salah",
                points = 25,
                explanation = "Jetpack Compose menggunakan paradigma deklaratif, bukan imperatif seperti sistem View XML tradisional."
            ),
            QuestionEntity(
                quizId = quizId,
                questionText = "Manakah dari elemen berikut yang merupakan komponen tata letak dasar di Compose? (Pilih semua yang benar)",
                questionType = "MULTIPLE_SELECT",
                options = "Column|||Row|||Box|||Fragment",
                correctAnswer = "Column|||Row|||Box",
                points = 25,
                explanation = "Column, Row, dan Box adalah tiga komponen tata letak dasar utama dalam Jetpack Compose."
            ),
            QuestionEntity(
                quizId = quizId,
                questionText = "Sebutkan nama fungsi yang digunakan untuk mengingat status state saat terjadi rekomposisi di Compose!",
                questionType = "SHORT_ANSWER",
                options = "",
                correctAnswer = "remember",
                points = 25,
                explanation = "Fungsi remember menyimpan objek di memori selama siklus rekomposisi berlangsung."
            )
        )
        dao.insertQuestions(questions)

        // Sample Quiz Attempt
        dao.insertAttempt(
            QuizAttemptEntity(
                quizId = quizId,
                studentId = 1L,
                studentName = "Ahmad Fauzi",
                attemptNumber = 1,
                submitTime = System.currentTimeMillis() - 86400000L,
                score = 100,
                isPassed = true,
                answersJson = "{\"1\":\"Menandai fungsi untuk mendefinisikan UI secara deklaratif\",\"2\":\"Salah\",\"3\":\"Column|||Row|||Box\",\"4\":\"remember\"}"
            )
        )

        // 9. Announcements
        dao.insertAnnouncement(
            AnnouncementEntity(
                title = "Jadwal Evaluasi Tengah Semester (PTS) Digital",
                content = "Diberitahukan kepada seluruh siswa kelas X RPL 1 bahwa evaluasi tengah semester akan dilaksanakan berbasis aplikasi SMKN 3 TANA TORAJA pada hari Senin pukul 08.00 WITA. Pastikan perangkat memiliki daya baterai penuh dan kuota aktif.",
                authorName = "Drs. H. Mursyid, M.Pd.",
                targetClassId = c1Id,
                targetClassName = "X RPL 1",
                priority = "PENTING"
            )
        )
        dao.insertAnnouncement(
            AnnouncementEntity(
                title = "Pengumpulan LKPD 01 Segera Ditutup",
                content = "Batas waktu pengumpulan Lembar Kerja Peserta Didik (LKPD) 01 adalah tanggal 20 November 2024. Siswa yang belum menyerahkan diharapkan segera mengirimkan jawabannya.",
                authorName = "Admin Guru",
                targetClassId = 0,
                targetClassName = "Semua Kelas",
                priority = "NORMAL"
            )
        )

        // 10. School Settings
        dao.insertOrUpdateSettings(
            SchoolSettingsEntity(
                id = 1,
                schoolName = "SMKN 3 TANA TORAJA",
                schoolNpsn = "20240988",
                academicYear = "2024/2025",
                semester = "Ganjil",
                teacherName = "Drs. H. Mursyid, M.Pd.",
                teacherNip = "19750812 200212 1 004",
                isOfflineMode = false,
                lastSyncTime = "Tersinkronisasi lokal (Otomatis)"
            )
        )
    }
}
