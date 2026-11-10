package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SmartLearningDatabase
import com.example.data.model.*
import com.example.data.preferences.UserPreferencesManager
import com.example.data.preferences.UserRole
import com.example.data.preferences.UserSession
import com.example.data.repository.SmartLearningRepository
import com.example.data.util.ImportResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

sealed class ExcelImportUiState {
    object Idle : ExcelImportUiState()
    data class Processing(val fileName: String) : ExcelImportUiState()
    data class Success(val result: ImportResult, val savedCount: Int) : ExcelImportUiState()
    data class Error(val message: String) : ExcelImportUiState()
}

class SmartLearningViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SmartLearningDatabase.getDatabase(application)
    private val repository = SmartLearningRepository(application, db.smartLearningDao())
    val preferencesManager = UserPreferencesManager(application)

    val session: StateFlow<UserSession> = preferencesManager.session

    val classes: StateFlow<List<ClassEntity>> = repository.classes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val students: StateFlow<List<StudentEntity>> = repository.students
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val videos: StateFlow<List<VideoEntity>> = repository.videos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val modules: StateFlow<List<TeachingModuleEntity>> = repository.modules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mediaList: StateFlow<List<LearningMediaEntity>> = repository.mediaList
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val worksheets: StateFlow<List<WorksheetEntity>> = repository.worksheets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quizzes: StateFlow<List<QuizEntity>> = repository.quizzes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAttempts: StateFlow<List<QuizAttemptEntity>> = repository.allAttempts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val announcements: StateFlow<List<AnnouncementEntity>> = repository.announcements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<SchoolSettingsEntity?> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // UI State for Filters
    private val _selectedClassId = MutableStateFlow<Long>(0L) // 0 = all
    val selectedClassId: StateFlow<Long> = _selectedClassId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Import State
    private val _excelImportState = MutableStateFlow<ExcelImportUiState>(ExcelImportUiState.Idle)
    val excelImportState: StateFlow<ExcelImportUiState> = _excelImportState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    fun setSelectedClass(classId: Long) {
        _selectedClassId.value = classId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun switchRole(role: UserRole) {
        preferencesManager.switchRole(role)
    }

    fun selectStudent(student: StudentEntity) {
        preferencesManager.selectStudent(
            id = student.id,
            nis = student.nis,
            name = student.fullName,
            classId = student.classId,
            className = student.className
        )
    }

    fun toggleDarkMode(enabled: Boolean) {
        preferencesManager.toggleDarkMode(enabled)
    }

    fun toggleOfflineMode(enabled: Boolean) {
        preferencesManager.toggleOfflineMode(enabled)
    }

    // Classes Actions
    fun addClass(name: String, academicYear: String, semester: String, teacher: String, desc: String) {
        viewModelScope.launch {
            repository.insertClass(
                ClassEntity(
                    className = name,
                    academicYear = academicYear,
                    semester = semester,
                    teacherName = teacher,
                    description = desc
                )
            )
        }
    }

    fun updateClass(c: ClassEntity) {
        viewModelScope.launch { repository.updateClass(c) }
    }

    fun deleteClass(c: ClassEntity) {
        viewModelScope.launch { repository.deleteClass(c) }
    }

    // Students Actions
    fun addStudent(
        nis: String,
        nisn: String,
        name: String,
        gender: String,
        birthPlace: String,
        birthDate: String,
        classId: Long,
        className: String,
        address: String,
        phone: String
    ) {
        viewModelScope.launch {
            repository.insertStudent(
                StudentEntity(
                    nis = nis,
                    nisn = nisn,
                    fullName = name,
                    gender = gender,
                    birthPlace = birthPlace,
                    birthDate = birthDate,
                    classId = classId,
                    className = className,
                    address = address,
                    parentPhone = phone
                )
            )
        }
    }

    fun updateStudent(s: StudentEntity) {
        viewModelScope.launch { repository.updateStudent(s) }
    }

    fun deleteStudent(s: StudentEntity) {
        viewModelScope.launch { repository.deleteStudent(s) }
    }

    // Attendance Actions
    fun saveAttendanceRecords(records: List<AttendanceEntity>) {
        viewModelScope.launch {
            repository.saveAttendance(records)
        }
    }

    fun getAttendanceForClass(classId: Long): Flow<List<AttendanceEntity>> {
        return repository.getAllAttendanceByClass(classId)
    }

    // Videos Actions
    fun addVideo(
        title: String,
        description: String,
        subject: String,
        classId: Long,
        className: String,
        topic: String,
        url: String,
        duration: String
    ) {
        viewModelScope.launch {
            repository.insertVideo(
                VideoEntity(
                    title = title,
                    description = description,
                    subject = subject,
                    classId = classId,
                    className = className,
                    topic = topic,
                    youtubeUrl = url,
                    durationText = duration
                )
            )
        }
    }

    fun deleteVideo(v: VideoEntity) {
        viewModelScope.launch { repository.deleteVideo(v) }
    }

    fun markVideoWatched(id: Long, isWatched: Boolean) {
        viewModelScope.launch { repository.markVideoWatched(id, isWatched) }
    }

    // Modules Actions
    fun addModule(
        title: String,
        description: String,
        subject: String,
        classId: Long,
        className: String,
        topic: String,
        objectives: String,
        fileType: String,
        fileSize: String,
        summary: String
    ) {
        viewModelScope.launch {
            repository.insertModule(
                TeachingModuleEntity(
                    title = title,
                    description = description,
                    subject = subject,
                    classId = classId,
                    className = className,
                    topic = topic,
                    learningObjectives = objectives,
                    fileType = fileType,
                    fileSize = fileSize,
                    contentSummary = summary
                )
            )
        }
    }

    fun deleteModule(m: TeachingModuleEntity) {
        viewModelScope.launch { repository.deleteModule(m) }
    }

    fun markModuleCompleted(id: Long, isCompleted: Boolean) {
        viewModelScope.launch { repository.markModuleCompleted(id, isCompleted) }
    }

    // Media Actions
    fun addMedia(
        title: String,
        description: String,
        category: String,
        mediaUrl: String,
        classId: Long,
        className: String
    ) {
        viewModelScope.launch {
            repository.insertMedia(
                LearningMediaEntity(
                    title = title,
                    description = description,
                    category = category,
                    mediaUrl = mediaUrl,
                    classId = classId,
                    className = className
                )
            )
        }
    }

    fun deleteMedia(m: LearningMediaEntity) {
        viewModelScope.launch { repository.deleteMedia(m) }
    }

    // LKPD Actions
    fun addWorksheet(
        title: String,
        instructions: String,
        classId: Long,
        className: String,
        subject: String,
        topic: String,
        startDate: String,
        dueDate: String,
        answerType: String
    ) {
        viewModelScope.launch {
            repository.insertWorksheet(
                WorksheetEntity(
                    title = title,
                    instructions = instructions,
                    classId = classId,
                    className = className,
                    subject = subject,
                    relatedTopic = topic,
                    startDate = startDate,
                    dueDate = dueDate,
                    answerType = answerType
                )
            )
        }
    }

    fun deleteWorksheet(w: WorksheetEntity) {
        viewModelScope.launch { repository.deleteWorksheet(w) }
    }

    fun submitWorksheetAnswer(
        worksheetId: Long,
        studentId: Long,
        studentName: String,
        answerText: String,
        fileUri: String
    ) {
        viewModelScope.launch {
            val existing = repository.getSubmission(worksheetId, studentId)
            val sub = existing?.copy(
                answerText = answerText,
                fileAttachmentUri = fileUri,
                status = "DIKUMPULKAN"
            ) ?: WorksheetSubmissionEntity(
                worksheetId = worksheetId,
                studentId = studentId,
                studentName = studentName,
                answerText = answerText,
                fileAttachmentUri = fileUri,
                status = "DIKUMPULKAN"
            )
            repository.saveSubmission(sub)
        }
    }

    fun gradeWorksheetSubmission(
        submission: WorksheetSubmissionEntity,
        score: Int,
        feedback: String
    ) {
        viewModelScope.launch {
            val graded = submission.copy(
                score = score,
                feedback = feedback,
                status = "DINILAI"
            )
            repository.saveSubmission(graded)
        }
    }

    fun getSubmissionsForWorksheet(worksheetId: Long): Flow<List<WorksheetSubmissionEntity>> {
        return repository.getSubmissionsForWorksheet(worksheetId)
    }

    fun getSubmissionsForStudent(studentId: Long): Flow<List<WorksheetSubmissionEntity>> {
        return repository.getSubmissionsForStudent(studentId)
    }

    // Quizzes & Questions
    fun createQuizWithQuestions(
        quiz: QuizEntity,
        questions: List<QuestionEntity>
    ) {
        viewModelScope.launch {
            val qId = repository.insertQuiz(quiz)
            val mapped = questions.map { it.copy(quizId = qId) }
            repository.insertQuestions(mapped)
        }
    }

    fun deleteQuiz(quiz: QuizEntity) {
        viewModelScope.launch { repository.deleteQuiz(quiz) }
    }

    fun getQuestionsForQuiz(quizId: Long): Flow<List<QuestionEntity>> {
        return repository.getQuestionsForQuiz(quizId)
    }

    suspend fun getQuestionsListForQuiz(quizId: Long): List<QuestionEntity> {
        return repository.getQuestionsListForQuiz(quizId)
    }

    fun submitQuizAttempt(
        quizId: Long,
        studentId: Long,
        studentName: String,
        score: Int,
        isPassed: Boolean,
        answersJson: String
    ) {
        viewModelScope.launch {
            val attempt = QuizAttemptEntity(
                quizId = quizId,
                studentId = studentId,
                studentName = studentName,
                submitTime = System.currentTimeMillis(),
                score = score,
                isPassed = isPassed,
                answersJson = answersJson
            )
            repository.saveQuizAttempt(attempt)
        }
    }

    fun getAttemptsForQuiz(quizId: Long): Flow<List<QuizAttemptEntity>> {
        return repository.getAttemptsForQuiz(quizId)
    }

    // Announcements
    fun addAnnouncement(
        title: String,
        content: String,
        author: String,
        classId: Long,
        className: String,
        priority: String
    ) {
        viewModelScope.launch {
            repository.insertAnnouncement(
                AnnouncementEntity(
                    title = title,
                    content = content,
                    authorName = author,
                    targetClassId = classId,
                    targetClassName = className,
                    priority = priority
                )
            )
        }
    }

    fun deleteAnnouncement(a: AnnouncementEntity) {
        viewModelScope.launch { repository.deleteAnnouncement(a) }
    }

    // Settings
    fun updateSettings(s: SchoolSettingsEntity) {
        viewModelScope.launch { repository.updateSettings(s) }
    }

    // Excel Import
    fun importExcelFile(
        uri: Uri,
        fileName: String,
        targetClassId: Long,
        targetClassName: String
    ) {
        viewModelScope.launch {
            _excelImportState.value = ExcelImportUiState.Processing(fileName)
            try {
                val (result, savedCount) = repository.importStudentsFromFile(
                    uri = uri,
                    fileName = fileName,
                    targetClassId = targetClassId,
                    targetClassName = targetClassName
                )
                _excelImportState.value = ExcelImportUiState.Success(result, savedCount)
            } catch (e: Exception) {
                _excelImportState.value = ExcelImportUiState.Error(e.message ?: "Gagal memproses file")
            }
        }
    }

    fun resetImportState() {
        _excelImportState.value = ExcelImportUiState.Idle
    }

    // Export Files
    suspend fun exportStudentsCsv(classId: Long?): File {
        return repository.exportStudentsToCsv(classId)
    }

    suspend fun generateStudentListPdf(classId: Long?): File {
        return repository.generateStudentListPdf(classId)
    }

    suspend fun generateAcademicReportPdf(classId: Long?): File {
        return repository.generateAcademicReportPdf(classId)
    }
}
