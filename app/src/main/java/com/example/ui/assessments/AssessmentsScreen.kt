package com.example.ui.assessments

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.preferences.UserRole
import com.example.data.preferences.UserSession
import com.example.ui.SmartLearningViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun AssessmentsScreen(
    viewModel: SmartLearningViewModel,
    session: UserSession,
    classes: List<ClassEntity>,
    worksheets: List<WorksheetEntity>,
    quizzes: List<QuizEntity>
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: LKPD, 1: Kuis Interaktif
    val tabs = listOf("Lembar Kerja (LKPD)", "Kuis Interaktif")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("assessments_screen")
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = NavyPrimary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = (selectedTab == index),
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("assessment_tab_$index")
                )
            }
        }

        when (selectedTab) {
            0 -> WorksheetTab(
                viewModel = viewModel,
                session = session,
                classes = classes,
                worksheets = worksheets
            )
            1 -> QuizTab(
                viewModel = viewModel,
                session = session,
                classes = classes,
                quizzes = quizzes
            )
        }
    }
}

// ==================== TAB 1: LKPD ====================
@Composable
fun WorksheetTab(
    viewModel: SmartLearningViewModel,
    session: UserSession,
    classes: List<ClassEntity>,
    worksheets: List<WorksheetEntity>
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var showStudentWorkDialog by remember { mutableStateOf<WorksheetEntity?>(null) }
    var showSubmissionsListDialog by remember { mutableStateOf<WorksheetEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        if (session.role == UserRole.GURU) {
            Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Kelola LKPD Pembelajaran (${worksheets.size})", fontWeight = FontWeight.Bold)
                    Button(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        modifier = Modifier.testTag("btn_create_worksheet")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Buat LKPD", fontSize = 12.sp)
                    }
                }
            }
        }

        if (worksheets.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Assignment,
                title = "Belum Ada LKPD",
                message = "Buat lembar kerja siswa dengan petunjuk, kriteria jawaban, dan tenggat waktu."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(worksheets, key = { it.id }) { w ->
                    WorksheetItemCard(
                        worksheet = w,
                        isTeacher = (session.role == UserRole.GURU),
                        onOpen = {
                            if (session.role == UserRole.GURU) {
                                showSubmissionsListDialog = w
                            } else {
                                showStudentWorkDialog = w
                            }
                        },
                        onDelete = { viewModel.deleteWorksheet(w) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddWorksheetDialog(
            classes = classes,
            onDismiss = { showAddDialog = false },
            onSave = { title, inst, cId, cName, subj, topic, start, due, type ->
                viewModel.addWorksheet(title, inst, cId, cName, subj, topic, start, due, type)
                showAddDialog = false
            }
        )
    }

    if (showStudentWorkDialog != null) {
        val w = showStudentWorkDialog!!
        StudentWorksheetSubmissionDialog(
            worksheet = w,
            session = session,
            onDismiss = { showStudentWorkDialog = null },
            onSubmit = { text, fileUri ->
                viewModel.submitWorksheetAnswer(
                    worksheetId = w.id,
                    studentId = session.currentStudentId,
                    studentName = session.currentStudentName,
                    answerText = text,
                    fileUri = fileUri
                )
                Toast.makeText(context, "Jawaban LKPD berhasil dikirim!", Toast.LENGTH_SHORT).show()
                showStudentWorkDialog = null
            }
        )
    }

    if (showSubmissionsListDialog != null) {
        val w = showSubmissionsListDialog!!
        TeacherGradingDialog(
            worksheet = w,
            viewModel = viewModel,
            onDismiss = { showSubmissionsListDialog = null }
        )
    }
}

@Composable
fun WorksheetItemCard(
    worksheet: WorksheetEntity,
    isTeacher: Boolean,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .testTag("worksheet_card_${worksheet.id}"),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEDE9FE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Assignment, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(worksheet.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${worksheet.subject} • ${worksheet.className}", fontSize = 12.sp, color = GrayText)
                }
                StatusBadge(text = "Tenggat: ${worksheet.dueDate}", statusType = "WARNING")
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(worksheet.instructions, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onOpen,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isTeacher) NavyPrimary else BlueModern)
                ) {
                    Icon(
                        imageVector = if (isTeacher) Icons.Default.FactCheck else Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isTeacher) "Lihat & Nilai Tugas" else "Kerjakan LKPD", fontSize = 12.sp)
                }
                if (isTeacher) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = ErrorRed, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StudentWorksheetSubmissionDialog(
    worksheet: WorksheetEntity,
    session: UserSession,
    onDismiss: () -> Unit,
    onSubmit: (answerText: String, fileUri: String) -> Unit
) {
    var answerText by remember { mutableStateOf("") }
    var fileUri by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Assignment, contentDescription = null, tint = NavyPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pengerjaan LKPD", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
            ) {
                Text(worksheet.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("Batas Waktu: ${worksheet.dueDate} | Nilai Maks: ${worksheet.maxScore}", fontSize = 12.sp, color = GrayText)
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = GraySurface,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Petunjuk Pengerjaan:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(worksheet.instructions, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("Tuliskan Jawaban Anda:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                OutlinedTextField(
                    value = answerText,
                    onValueChange = { answerText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("worksheet_answer_field"),
                    placeholder = { Text("Tuliskan solusi, kode program, atau ringkasan hasil kerja...") },
                    maxLines = 8
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = fileUri,
                    onValueChange = { fileUri = it },
                    label = { Text("Lampiran Link / Dokumen Pendukung (Opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (answerText.isNotBlank() || fileUri.isNotBlank()) {
                        onSubmit(answerText, fileUri)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                modifier = Modifier.testTag("submit_worksheet_button")
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Kirim Jawaban")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun TeacherGradingDialog(
    worksheet: WorksheetEntity,
    viewModel: SmartLearningViewModel,
    onDismiss: () -> Unit
) {
    val submissions by viewModel.getSubmissionsForWorksheet(worksheet.id).collectAsState(emptyList())
    var gradingTarget by remember { mutableStateOf<WorksheetSubmissionEntity?>(null) }
    var scoreInput by remember { mutableStateOf("90") }
    var feedbackInput by remember { mutableStateOf("Hasil pengerjaan sangat baik dan sistematis.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Penilaian LKPD Siswa", fontWeight = FontWeight.Bold)
                Text(worksheet.title, fontSize = 12.sp, color = GrayText)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                if (gradingTarget == null) {
                    Text("Pengumpulan Siswa (${submissions.size}):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (submissions.isEmpty()) {
                        Text("Belum ada siswa yang mengumpulkan tugas ini.", color = GrayText, fontSize = 12.sp)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(submissions) { sub ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = GraySurface,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(sub.studentName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            StatusBadge(
                                                text = if (sub.status == "DINILAI") "Nilai: ${sub.score}" else sub.status,
                                                statusType = if (sub.status == "DINILAI") "SUCCESS" else "WARNING"
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(sub.answerText, fontSize = 12.sp, maxLines = 2, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        if (sub.feedback.isNotBlank()) {
                                            Text("Feedback: ${sub.feedback}", fontSize = 11.sp, color = NavyPrimary)
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Button(
                                            onClick = {
                                                gradingTarget = sub
                                                scoreInput = if (sub.score > 0) "${sub.score}" else "85"
                                                feedbackInput = sub.feedback.ifBlank { "Bagus! Terus tingkatkan." }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(if (sub.status == "DINILAI") "Ubah Nilai" else "Beri Nilai", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    val sub = gradingTarget!!
                    Text("Menilai: ${sub.studentName}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Jawaban Siswa:", fontSize = 11.sp, color = GrayText)
                    Surface(shape = RoundedCornerShape(8.dp), color = GraySurface, modifier = Modifier.fillMaxWidth()) {
                        Text(sub.answerText, modifier = Modifier.padding(8.dp), fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = scoreInput,
                        onValueChange = { scoreInput = it },
                        label = { Text("Nilai (0 - 100)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = feedbackInput,
                        onValueChange = { feedbackInput = it },
                        label = { Text("Komentar & Umpan Balik") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { gradingTarget = null }) {
                            Text("Batal")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val s = scoreInput.toIntOrNull() ?: 80
                                viewModel.gradeWorksheetSubmission(sub, s, feedbackInput)
                                gradingTarget = null
                            }
                        ) {
                            Text("Simpan Nilai")
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (gradingTarget == null) {
                TextButton(onClick = onDismiss) { Text("Tutup") }
            }
        }
    )
}

// ==================== TAB 2: KUIS INTERAKTIF ====================
@Composable
fun QuizTab(
    viewModel: SmartLearningViewModel,
    session: UserSession,
    classes: List<ClassEntity>,
    quizzes: List<QuizEntity>
) {
    var showCreateQuizDialog by remember { mutableStateOf(false) }
    var activeQuizForTaking by remember { mutableStateOf<QuizEntity?>(null) }
    var activeQuizForRecap by remember { mutableStateOf<QuizEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        if (session.role == UserRole.GURU) {
            Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Kuis & Evaluasi (${quizzes.size})", fontWeight = FontWeight.Bold)
                    Button(
                        onClick = { showCreateQuizDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        modifier = Modifier.testTag("btn_create_quiz")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Buat Kuis Baru", fontSize = 12.sp)
                    }
                }
            }
        }

        if (quizzes.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Quiz,
                title = "Belum Ada Kuis",
                message = "Buat evaluasi formatif dengan soal pilihan ganda, benar/salah, pilihan jamak, dan isian singkat."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(quizzes, key = { it.id }) { q ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quiz_card_${q.id}"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(GoldLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Quiz, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(q.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("${q.subject} • ${q.className} • ${q.durationMinutes} Menit", fontSize = 12.sp, color = GrayText)
                                }
                                StatusBadge(text = "KKM ${q.minimumPassingScore}", statusType = "INFO")
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(q.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { activeQuizForTaking = q },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                    modifier = Modifier.testTag("btn_take_quiz_${q.id}")
                                ) {
                                    Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Mulai Kuis", fontSize = 12.sp)
                                }

                                if (session.role == UserRole.GURU) {
                                    Row {
                                        TextButton(onClick = { activeQuizForRecap = q }) {
                                            Text("Hasil Siswa", fontSize = 12.sp)
                                        }
                                        IconButton(onClick = { viewModel.deleteQuiz(q) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = ErrorRed, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateQuizDialog) {
        CreateQuizDialog(
            classes = classes,
            onDismiss = { showCreateQuizDialog = false },
            onSave = { quiz, questions ->
                viewModel.createQuizWithQuestions(quiz, questions)
                showCreateQuizDialog = false
            }
        )
    }

    if (activeQuizForTaking != null) {
        QuizTakingDialog(
            quiz = activeQuizForTaking!!,
            session = session,
            viewModel = viewModel,
            onDismiss = { activeQuizForTaking = null }
        )
    }

    if (activeQuizForRecap != null) {
        QuizRecapDialog(
            quiz = activeQuizForRecap!!,
            viewModel = viewModel,
            onDismiss = { activeQuizForRecap = null }
        )
    }
}

// Live Interactive Quiz Taking Dialog
@Composable
fun QuizTakingDialog(
    quiz: QuizEntity,
    session: UserSession,
    viewModel: SmartLearningViewModel,
    onDismiss: () -> Unit
) {
    var questions by remember { mutableStateOf<List<QuestionEntity>>(emptyList()) }
    var currentQuestionIdx by remember { mutableIntStateOf(0) }
    val userAnswers = remember { mutableStateMapOf<Long, String>() }
    var timeLeftSeconds by remember { mutableIntStateOf(quiz.durationMinutes * 60) }
    var isSubmitted by remember { mutableStateOf(false) }
    var finalScore by remember { mutableIntStateOf(0) }
    var isPassed by remember { mutableStateOf(false) }

    LaunchedEffect(quiz.id) {
        questions = viewModel.getQuestionsListForQuiz(quiz.id)
    }

    LaunchedEffect(isSubmitted) {
        if (!isSubmitted) {
            while (timeLeftSeconds > 0) {
                delay(1000)
                timeLeftSeconds--
            }
            if (timeLeftSeconds <= 0 && !isSubmitted) {
                calculateAndSubmitScore(
                    quiz = quiz,
                    questions = questions,
                    userAnswers = userAnswers,
                    session = session,
                    viewModel = viewModel,
                    onResult = { score, passed ->
                        finalScore = score
                        isPassed = passed
                        isSubmitted = true
                    }
                )
            }
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (isSubmitted) onDismiss()
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(quiz.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (!isSubmitted) {
                    val minutes = timeLeftSeconds / 60
                    val seconds = timeLeftSeconds % 60
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (timeLeftSeconds < 120) Color(0xFFFEE2E2) else Color(0xFFFEF3C7))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = String.format("%02d:%02d", minutes, seconds),
                            fontWeight = FontWeight.Bold,
                            color = if (timeLeftSeconds < 120) ErrorRed else GoldPrimary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        },
        text = {
            if (questions.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldPrimary)
                }
            } else if (!isSubmitted) {
                val q = questions[currentQuestionIdx]
                val selectedAnswer = userAnswers[q.id] ?: ""

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 440.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Soal ${currentQuestionIdx + 1} dari ${questions.size}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = NavyPrimary
                        )
                        Text(
                            text = "Poin: ${q.points}",
                            fontSize = 12.sp,
                            color = GrayText
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = q.questionText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    when (q.questionType) {
                        "SINGLE_CHOICE", "TRUE_FALSE" -> {
                            val opts = q.options.split("|||").filter { it.isNotBlank() }
                            opts.forEach { opt ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { userAnswers[q.id] = opt },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selectedAnswer == opt) BlueContainer else GraySurface,
                                    border = if (selectedAnswer == opt) androidx.compose.foundation.BorderStroke(1.5.dp, NavyPrimary) else null
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = (selectedAnswer == opt),
                                            onClick = { userAnswers[q.id] = opt }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(opt, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                        "MULTIPLE_SELECT" -> {
                            val opts = q.options.split("|||").filter { it.isNotBlank() }
                            val currentPicks = selectedAnswer.split("|||").filter { it.isNotBlank() }.toSet()
                            opts.forEach { opt ->
                                val isChecked = currentPicks.contains(opt)
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            val newPicks = if (isChecked) currentPicks - opt else currentPicks + opt
                                            userAnswers[q.id] = newPicks.joinToString("|||")
                                        },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isChecked) BlueContainer else GraySurface
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { checked ->
                                                val newPicks = if (checked) currentPicks + opt else currentPicks - opt
                                                userAnswers[q.id] = newPicks.joinToString("|||")
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(opt, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                        "SHORT_ANSWER" -> {
                            OutlinedTextField(
                                value = selectedAnswer,
                                onValueChange = { userAnswers[q.id] = it },
                                label = { Text("Ketik jawaban singkat di sini...") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = { if (currentQuestionIdx > 0) currentQuestionIdx-- },
                            enabled = (currentQuestionIdx > 0),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Sebelumnya")
                        }
                        if (currentQuestionIdx < questions.size - 1) {
                            Button(
                                onClick = { currentQuestionIdx++ },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                            ) {
                                Text("Berikutnya")
                            }
                        } else {
                            Button(
                                onClick = {
                                    calculateAndSubmitScore(
                                        quiz = quiz,
                                        questions = questions,
                                        userAnswers = userAnswers,
                                        session = session,
                                        viewModel = viewModel,
                                        onResult = { score, passed ->
                                            finalScore = score
                                            isPassed = passed
                                            isSubmitted = true
                                        }
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                            ) {
                                Text("Kirim Jawaban")
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(if (isPassed) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPassed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = null,
                            tint = if (isPassed) SuccessGreen else ErrorRed,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isPassed) "Selamat, Anda Tuntas!" else "Perlu Remedial",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isPassed) SuccessGreen else ErrorRed
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Nilai Akhir Evaluasi:", fontSize = 13.sp, color = GrayText)
                    Text("$finalScore / 100", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Standar KKM: ${quiz.minimumPassingScore} Poin",
                        fontSize = 12.sp,
                        color = GrayText
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isPassed)
                            "Hasil evaluasi telah tersimpan secara otomatis ke rekapitulasi nilai akademik."
                        else
                            "Pelajari kembali materi terkait dan konsultasikan dengan guru untuk perbaikan.",
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            if (isSubmitted) {
                Button(onClick = onDismiss) { Text("Selesai") }
            } else {
                TextButton(onClick = onDismiss) { Text("Batal") }
            }
        }
    )
}

fun calculateAndSubmitScore(
    quiz: QuizEntity,
    questions: List<QuestionEntity>,
    userAnswers: Map<Long, String>,
    session: UserSession,
    viewModel: SmartLearningViewModel,
    onResult: (score: Int, isPassed: Boolean) -> Unit
) {
    var totalEarned = 0
    var totalMaxPoints = 0

    questions.forEach { q ->
        totalMaxPoints += q.points
        val userAns = userAnswers[q.id]?.trim() ?: ""
        val correctAns = q.correctAnswer.trim()

        val isCorrect = when (q.questionType) {
            "SHORT_ANSWER" -> userAns.equals(correctAns, ignoreCase = true)
            "MULTIPLE_SELECT" -> {
                val set1 = userAns.split("|||").filter { it.isNotBlank() }.toSet()
                val set2 = correctAns.split("|||").filter { it.isNotBlank() }.toSet()
                set1 == set2
            }
            else -> userAns.equals(correctAns, ignoreCase = true)
        }

        if (isCorrect) {
            totalEarned += q.points
        }
    }

    val calculatedScore = if (totalMaxPoints > 0) (totalEarned * 100) / totalMaxPoints else 0
    val passed = calculatedScore >= quiz.minimumPassingScore

    viewModel.submitQuizAttempt(
        quizId = quiz.id,
        studentId = session.currentStudentId,
        studentName = session.currentStudentName,
        score = calculatedScore,
        isPassed = passed,
        answersJson = userAnswers.toString()
    )

    onResult(calculatedScore, passed)
}

@Composable
fun QuizRecapDialog(
    quiz: QuizEntity,
    viewModel: SmartLearningViewModel,
    onDismiss: () -> Unit
) {
    val attempts by viewModel.getAttemptsForQuiz(quiz.id).collectAsState(emptyList())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Rekap Hasil Kuis Siswa", fontWeight = FontWeight.Bold)
                Text(quiz.title, fontSize = 12.sp, color = GrayText)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                val avg = if (attempts.isNotEmpty()) attempts.map { it.score }.average() else 0.0
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BlueContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Rata-rata Kelas", fontSize = 11.sp, color = NavyPrimary)
                            Text(String.format("%.1f", avg), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NavyPrimary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Peserta Selesai", fontSize = 11.sp, color = NavyPrimary)
                            Text("${attempts.size} Siswa", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NavyPrimary)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                if (attempts.isEmpty()) {
                    Text("Belum ada siswa yang menyelesaikan kuis ini.", color = GrayText, fontSize = 12.sp)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(attempts) { a ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = GraySurface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(a.studentName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text(if (a.isPassed) "Tuntas (>= ${quiz.minimumPassingScore})" else "Remedial", fontSize = 11.sp, color = if (a.isPassed) SuccessGreen else ErrorRed)
                                    }
                                    Text("${a.score}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (a.isPassed) SuccessGreen else ErrorRed)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Tutup") }
        }
    )
}

@Composable
fun CreateQuizDialog(
    classes: List<ClassEntity>,
    onDismiss: () -> Unit,
    onSave: (QuizEntity, List<QuestionEntity>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Pemrograman Bergerak") }
    var topic by remember { mutableStateOf("Dasar Algoritma") }
    var duration by remember { mutableStateOf("20") }
    var minScore by remember { mutableStateOf("75") }
    var selectedClass by remember { mutableStateOf(classes.firstOrNull() ?: ClassEntity(1, "X RPL 1")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buat Kuis Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Judul Kuis") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Mata Pelajaran") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = topic, onValueChange = { topic = it }, label = { Text("Topik") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Durasi (Menit)") }, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(value = minScore, onValueChange = { minScore = it }, label = { Text("KKM Minimum") }, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Deskripsi / Petunjuk") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isNotBlank()) {
                    val q = QuizEntity(
                        title = title,
                        description = desc,
                        classId = selectedClass.id,
                        className = selectedClass.className,
                        subject = subject,
                        topic = topic,
                        durationMinutes = duration.toIntOrNull() ?: 20,
                        minimumPassingScore = minScore.toIntOrNull() ?: 75
                    )
                    val questions = listOf(
                        QuestionEntity(
                            quizId = 0L,
                            questionText = "Manakah dari berikut yang merupakan tipe data primitif di Kotlin?",
                            questionType = "SINGLE_CHOICE",
                            options = "Int|||ArrayList|||HashMap|||Observable",
                            correctAnswer = "Int",
                            points = 50
                        ),
                        QuestionEntity(
                            quizId = 0L,
                            questionText = "Apakah Jetpack Compose mendukung dynamic state update?",
                            questionType = "TRUE_FALSE",
                            options = "Benar|||Salah",
                            correctAnswer = "Benar",
                            points = 50
                        )
                    )
                    onSave(q, questions)
                }
            }) {
                Text("Buat Kuis")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
fun AddWorksheetDialog(
    classes: List<ClassEntity>,
    onDismiss: () -> Unit,
    onSave: (title: String, instructions: String, classId: Long, className: String, subject: String, topic: String, start: String, due: String, type: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Pemrograman Bergerak") }
    var topic by remember { mutableStateOf("State Management") }
    var dueDate by remember { mutableStateOf("2024-11-25") }
    var selectedClass by remember { mutableStateOf(classes.firstOrNull() ?: ClassEntity(1, "X RPL 1")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buat LKPD Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Judul LKPD") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Mata Pelajaran") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = topic, onValueChange = { topic = it }, label = { Text("Topik Terkait") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = dueDate, onValueChange = { dueDate = it }, label = { Text("Batas Pengumpulan (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = instructions, onValueChange = { instructions = it }, label = { Text("Petunjuk Pengerjaan") }, modifier = Modifier.fillMaxWidth(), maxLines = 4)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isNotBlank()) {
                    onSave(title, instructions, selectedClass.id, selectedClass.className, subject, topic, "2024-11-01", dueDate, "TEXT")
                }
            }) {
                Text("Simpan LKPD")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
