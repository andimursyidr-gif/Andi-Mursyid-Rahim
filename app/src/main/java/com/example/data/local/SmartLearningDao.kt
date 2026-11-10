package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SmartLearningDao {

    // === CLASSES ===
    @Query("SELECT * FROM classes ORDER BY className ASC")
    fun getAllClasses(): Flow<List<ClassEntity>>

    @Query("SELECT * FROM classes WHERE id = :id LIMIT 1")
    suspend fun getClassById(id: Long): ClassEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(classEntity: ClassEntity): Long

    @Update
    suspend fun updateClass(classEntity: ClassEntity)

    @Delete
    suspend fun deleteClass(classEntity: ClassEntity)

    // === STUDENTS ===
    @Query("SELECT * FROM students ORDER BY fullName ASC")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE classId = :classId ORDER BY fullName ASC")
    fun getStudentsByClass(classId: Long): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE nis = :nis LIMIT 1")
    suspend fun getStudentByNis(nis: String): StudentEntity?

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getStudentById(id: Long): StudentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentEntity>)

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteStudentById(id: Long)

    // === ATTENDANCE ===
    @Query("SELECT * FROM attendance WHERE classId = :classId AND date = :date")
    fun getAttendanceByClassAndDate(classId: Long, date: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY date DESC")
    fun getAttendanceForStudent(studentId: Long): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE classId = :classId ORDER BY date DESC")
    fun getAllAttendanceByClass(classId: Long): Flow<List<AttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: List<AttendanceEntity>)

    // === VIDEOS ===
    @Query("SELECT * FROM videos ORDER BY id DESC")
    fun getAllVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE classId = :classId OR classId = 0 ORDER BY id DESC")
    fun getVideosByClass(classId: Long): Flow<List<VideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity): Long

    @Update
    suspend fun updateVideo(video: VideoEntity)

    @Delete
    suspend fun deleteVideo(video: VideoEntity)

    @Query("UPDATE videos SET isWatched = :isWatched, watchedAt = :watchedAt WHERE id = :id")
    suspend fun markVideoWatched(id: Long, isWatched: Boolean, watchedAt: Long = System.currentTimeMillis())

    // === MODULES ===
    @Query("SELECT * FROM teaching_modules ORDER BY id DESC")
    fun getAllModules(): Flow<List<TeachingModuleEntity>>

    @Query("SELECT * FROM teaching_modules WHERE classId = :classId OR classId = 0 ORDER BY id DESC")
    fun getModulesByClass(classId: Long): Flow<List<TeachingModuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModule(module: TeachingModuleEntity): Long

    @Update
    suspend fun updateModule(module: TeachingModuleEntity)

    @Delete
    suspend fun deleteModule(module: TeachingModuleEntity)

    @Query("UPDATE teaching_modules SET isCompleted = :completed WHERE id = :id")
    suspend fun markModuleCompleted(id: Long, completed: Boolean)

    // === LEARNING MEDIA ===
    @Query("SELECT * FROM learning_media ORDER BY id DESC")
    fun getAllMedia(): Flow<List<LearningMediaEntity>>

    @Query("SELECT * FROM learning_media WHERE classId = :classId OR classId = 0 ORDER BY id DESC")
    fun getMediaByClass(classId: Long): Flow<List<LearningMediaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: LearningMediaEntity): Long

    @Delete
    suspend fun deleteMedia(media: LearningMediaEntity)

    // === WORKSHEETS (LKPD) ===
    @Query("SELECT * FROM worksheets ORDER BY id DESC")
    fun getAllWorksheets(): Flow<List<WorksheetEntity>>

    @Query("SELECT * FROM worksheets WHERE classId = :classId OR classId = 0 ORDER BY id DESC")
    fun getWorksheetsByClass(classId: Long): Flow<List<WorksheetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorksheet(worksheet: WorksheetEntity): Long

    @Update
    suspend fun updateWorksheet(worksheet: WorksheetEntity)

    @Delete
    suspend fun deleteWorksheet(worksheet: WorksheetEntity)

    // === WORKSHEET SUBMISSIONS ===
    @Query("SELECT * FROM worksheet_submissions WHERE worksheetId = :worksheetId")
    fun getSubmissionsForWorksheet(worksheetId: Long): Flow<List<WorksheetSubmissionEntity>>

    @Query("SELECT * FROM worksheet_submissions WHERE studentId = :studentId")
    fun getSubmissionsForStudent(studentId: Long): Flow<List<WorksheetSubmissionEntity>>

    @Query("SELECT * FROM worksheet_submissions WHERE worksheetId = :worksheetId AND studentId = :studentId LIMIT 1")
    suspend fun getSubmission(worksheetId: Long, studentId: Long): WorksheetSubmissionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSubmission(submission: WorksheetSubmissionEntity): Long

    // === QUIZZES ===
    @Query("SELECT * FROM quizzes ORDER BY id DESC")
    fun getAllQuizzes(): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quizzes WHERE classId = :classId OR classId = 0 ORDER BY id DESC")
    fun getQuizzesByClass(classId: Long): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quizzes WHERE id = :id LIMIT 1")
    suspend fun getQuizById(id: Long): QuizEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity): Long

    @Update
    suspend fun updateQuiz(quiz: QuizEntity)

    @Delete
    suspend fun deleteQuiz(quiz: QuizEntity)

    // === QUESTIONS ===
    @Query("SELECT * FROM questions WHERE quizId = :quizId")
    fun getQuestionsForQuiz(quizId: Long): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE quizId = :quizId")
    suspend fun getQuestionsListForQuiz(quizId: Long): List<QuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Query("DELETE FROM questions WHERE quizId = :quizId")
    suspend fun deleteQuestionsForQuiz(quizId: Long)

    // === QUIZ ATTEMPTS ===
    @Query("SELECT * FROM quiz_attempts WHERE quizId = :quizId ORDER BY score DESC")
    fun getAttemptsForQuiz(quizId: Long): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts WHERE studentId = :studentId ORDER BY id DESC")
    fun getAttemptsForStudent(studentId: Long): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts ORDER BY id DESC")
    fun getAllAttempts(): Flow<List<QuizAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: QuizAttemptEntity): Long

    // === ANNOUNCEMENTS ===
    @Query("SELECT * FROM announcements ORDER BY createdAt DESC")
    fun getAllAnnouncements(): Flow<List<AnnouncementEntity>>

    @Query("SELECT * FROM announcements WHERE targetClassId = :classId OR targetClassId = 0 ORDER BY createdAt DESC")
    fun getAnnouncementsForClass(classId: Long): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity): Long

    @Delete
    suspend fun deleteAnnouncement(announcement: AnnouncementEntity)

    // === SCHOOL SETTINGS ===
    @Query("SELECT * FROM school_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<SchoolSettingsEntity?>

    @Query("SELECT * FROM school_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsOnce(): SchoolSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: SchoolSettingsEntity)
}
