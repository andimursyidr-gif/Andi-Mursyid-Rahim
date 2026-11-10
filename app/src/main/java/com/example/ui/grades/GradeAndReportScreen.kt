package com.example.ui.grades

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.preferences.UserRole
import com.example.data.preferences.UserSession
import com.example.ui.SmartLearningViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MetricCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun GradeAndReportScreen(
    viewModel: SmartLearningViewModel,
    session: UserSession,
    classes: List<ClassEntity>,
    students: List<StudentEntity>,
    allAttempts: List<QuizAttemptEntity>,
    onOpenAttendance: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedClassId by remember { mutableLongStateOf(0L) }
    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Nilai Akademik, 1: Rekap Absensi

    val filteredStudents = students.filter { selectedClassId == 0L || it.classId == selectedClassId }
    val filteredAttempts = allAttempts.filter { a ->
        val s = students.firstOrNull { it.id == a.studentId }
        selectedClassId == 0L || s?.classId == selectedClassId
    }

    val avgQuizScore = if (filteredAttempts.isNotEmpty()) filteredAttempts.map { it.score }.average() else 85.0
    val maxQuizScore = if (filteredAttempts.isNotEmpty()) filteredAttempts.maxOf { it.score } else 100
    val minQuizScore = if (filteredAttempts.isNotEmpty()) filteredAttempts.minOf { it.score } else 65
    val passedCount = filteredAttempts.count { it.isPassed }
    val totalAttempts = maxOf(1, filteredAttempts.size)
    val passPercentage = (passedCount * 100) / totalAttempts

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("grade_report_screen")
    ) {
        // Class Filter & Export Header
        Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Class filter chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = (selectedClassId == 0L),
                        onClick = { selectedClassId = 0L },
                        label = { Text("Semua Kelas") }
                    )
                    classes.forEach { c ->
                        FilterChip(
                            selected = (selectedClassId == c.id),
                            onClick = { selectedClassId = c.id },
                            label = { Text(c.className) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action buttons: Export to PDF, Export to Excel/CSV
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val pdf = viewModel.generateAcademicReportPdf(if (selectedClassId == 0L) null else selectedClassId)
                                Toast.makeText(context, "Laporan PDF berhasil dibuat: ${pdf.name}", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_export_academic_pdf"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ekspor PDF", fontSize = 12.sp)
                    }
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val csv = viewModel.exportStudentsCsv(if (selectedClassId == 0L) null else selectedClassId)
                                Toast.makeText(context, "Ekspor Excel/CSV berhasil: ${csv.name}", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_export_academic_csv"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                    ) {
                        Icon(Icons.Default.TableView, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ekspor Excel", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = onOpenAttendance,
                        modifier = Modifier.testTag("btn_quick_open_attendance"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Absensi", fontSize = 12.sp)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Summary Analytics Cards
            item {
                Text("Analisis Hasil Belajar & Ketuntasan", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Rata-rata",
                        value = String.format("%.1f", avgQuizScore),
                        subtitle = "Kuis & Evaluasi",
                        icon = Icons.Default.Analytics,
                        iconTint = BlueModern,
                        iconBg = BlueContainer,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Ketuntasan",
                        value = "$passPercentage%",
                        subtitle = "$passedCount dari $totalAttempts tuntas",
                        icon = Icons.Default.CheckCircle,
                        iconTint = SuccessGreen,
                        iconBg = Color(0xFFDCFCE7),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Nilai Tertinggi",
                        value = "$maxQuizScore",
                        subtitle = "Skor Sempurna",
                        icon = Icons.Default.Star,
                        iconTint = GoldPrimary,
                        iconBg = GoldLight,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Nilai Terendah",
                        value = "$minQuizScore",
                        subtitle = "Perlu Remedial",
                        icon = Icons.Default.Warning,
                        iconTint = WarningOrange,
                        iconBg = Color(0xFFFEF3C7),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Student Performance List
            item {
                SectionHeader(title = "Rekapitulasi Nilai Siswa")
            }

            if (filteredStudents.isEmpty()) {
                item {
                    Text("Belum ada data siswa di kelas yang dipilih.", color = GrayText, fontSize = 12.sp)
                }
            } else {
                items(filteredStudents) { s ->
                    val sAttempts = allAttempts.filter { it.studentId == s.id }
                    val sAvg = if (sAttempts.isNotEmpty()) sAttempts.map { it.score }.average().toInt() else 85

                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (sAvg >= 75) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$sAvg",
                                    fontWeight = FontWeight.Bold,
                                    color = if (sAvg >= 75) SuccessGreen else ErrorRed,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(s.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("NIS: ${s.nis} • Kelas: ${s.className}", fontSize = 11.sp, color = GrayText)
                            }
                            StatusBadge(
                                text = if (sAvg >= 75) "Tuntas" else "Remedial",
                                statusType = if (sAvg >= 75) "SUCCESS" else "WARNING"
                            )
                        }
                    }
                }
            }
        }
    }
}
