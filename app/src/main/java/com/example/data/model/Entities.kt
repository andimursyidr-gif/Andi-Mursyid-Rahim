package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "classes")
data class ClassEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val className: String,
    val academicYear: String = "2024/2025",
    val semester: String = "Ganjil",
    val teacherName: String = "Drs. H. Mursyid, M.Pd.",
    val description: String = ""
)

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nis: String,
    val nisn: String = "",
    val fullName: String,
    val gender: String, // "L" atau "P"
    val birthPlace: String = "",
    val birthDate: String = "",
    val classId: Long,
    val className: String,
    val address: String = "",
    val parentPhone: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val studentName: String,
    val classId: Long,
    val date: String, // YYYY-MM-DD
    val status: String, // "HADIR", "SAKIT", "IZIN", "ALFA"
    val note: String = ""
)

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val subject: String,
    val classId: Long,
    val className: String,
    val topic: String,
    val youtubeUrl: String,
    val durationText: String = "15 Menit",
    val isWatched: Boolean = false,
    val watchedAt: Long = 0L
)

@Entity(tableName = "teaching_modules")
data class TeachingModuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val subject: String,
    val classId: Long,
    val className: String,
    val topic: String,
    val learningObjectives: String,
    val fileType: String = "PDF", // "PDF", "DOCX"
    val fileUri: String = "",
    val fileSize: String = "2.4 MB",
    val contentSummary: String = "",
    val isCompleted: Boolean = false
)

@Entity(tableName = "learning_media")
data class LearningMediaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val category: String, // "GAMBAR", "INFOGRAFIS", "PRESENTASI", "AUDIO", "PDF", "LINK", "INTERAKTIF"
    val mediaUrl: String = "",
    val localUri: String = "",
    val classId: Long,
    val className: String
)

@Entity(tableName = "worksheets")
data class WorksheetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val instructions: String,
    val classId: Long,
    val className: String,
    val subject: String,
    val relatedTopic: String,
    val startDate: String,
    val dueDate: String,
    val answerType: String = "TEXT", // "TEXT", "FILE", "MIXED"
    val maxScore: Int = 100
)

@Entity(tableName = "worksheet_submissions")
data class WorksheetSubmissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val worksheetId: Long,
    val studentId: Long,
    val studentName: String,
    val submissionDate: String = "",
    val answerText: String = "",
    val fileAttachmentUri: String = "",
    val status: String = "BELUM", // "BELUM", "PROSES", "DIKUMPULKAN", "DINILAI"
    val score: Int = 0,
    val feedback: String = ""
)

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val classId: Long,
    val className: String,
    val subject: String,
    val topic: String,
    val durationMinutes: Int = 30,
    val maxAttempts: Int = 1,
    val executionDate: String = "",
    val minimumPassingScore: Int = 75,
    val isActive: Boolean = true
)

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quizId: Long,
    val questionText: String,
    val questionType: String, // "SINGLE_CHOICE", "TRUE_FALSE", "MULTIPLE_SELECT", "SHORT_ANSWER"
    val options: String, // Delimited by "|||"
    val correctAnswer: String,
    val points: Int = 20,
    val explanation: String = ""
)

@Entity(tableName = "quiz_attempts")
data class QuizAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quizId: Long,
    val studentId: Long,
    val studentName: String,
    val attemptNumber: Int = 1,
    val startTime: Long = System.currentTimeMillis(),
    val submitTime: Long = 0L,
    val score: Int = 0,
    val isPassed: Boolean = false,
    val answersJson: String = "" // Question ID -> Answer
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val authorName: String = "Admin Guru",
    val targetClassId: Long = 0, // 0 for all classes
    val targetClassName: String = "Semua Kelas",
    val priority: String = "NORMAL", // "NORMAL", "PENTING", "URGENT"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "school_settings")
data class SchoolSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val schoolName: String = "SMKN 3 TANA TORAJA",
    val schoolNpsn: String = "20240988",
    val academicYear: String = "2024/2025",
    val semester: String = "Ganjil",
    val teacherName: String = "Drs. H. Mursyid, M.Pd.",
    val teacherNip: String = "19750812 200212 1 004",
    val isOfflineMode: Boolean = false,
    val lastSyncTime: String = "Belum disinkronkan"
)
