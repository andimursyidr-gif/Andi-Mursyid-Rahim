package com.example.ui.attendance

import android.widget.Toast
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceEntity
import com.example.data.model.ClassEntity
import com.example.data.model.StudentEntity
import com.example.ui.SmartLearningViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AttendanceTrackerDialog(
    classes: List<ClassEntity>,
    students: List<StudentEntity>,
    viewModel: SmartLearningViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedClass by remember { mutableStateOf(classes.firstOrNull() ?: ClassEntity(1, "X RPL 1")) }
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    val classStudents = students.filter { it.classId == selectedClass.id }
    val attendanceStatusMap = remember { mutableStateMapOf<Long, String>() }

    // Initialize with "HADIR" by default
    LaunchedEffect(classStudents) {
        classStudents.forEach { s ->
            if (!attendanceStatusMap.containsKey(s.id)) {
                attendanceStatusMap[s.id] = "HADIR"
            }
        }
    }

    val hadirCount = attendanceStatusMap.values.count { it == "HADIR" }
    val sakitCount = attendanceStatusMap.values.count { it == "SAKIT" }
    val izinCount = attendanceStatusMap.values.count { it == "IZIN" }
    val alfaCount = attendanceStatusMap.values.count { it == "ALFA" }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FactCheck, contentDescription = null, tint = NavyPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Presensi Kehadiran Siswa", fontWeight = FontWeight.Bold)
                }
                Text("Tanggal: $today | Kelas: ${selectedClass.className}", fontSize = 12.sp, color = GrayText)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
            ) {
                // Class Picker & "Hadirkan Semua"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        classStudents.forEach { s -> attendanceStatusMap[s.id] = "HADIR" }
                    }) {
                        Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hadirkan Semua", fontSize = 11.sp)
                    }
                }

                // Summary Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), color = Color(0xFFDCFCE7)) {
                        Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$hadirCount", fontWeight = FontWeight.Bold, color = SuccessGreen, fontSize = 14.sp)
                            Text("Hadir", fontSize = 10.sp, color = SuccessGreen)
                        }
                    }
                    Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), color = Color(0xFFFEF3C7)) {
                        Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$sakitCount", fontWeight = FontWeight.Bold, color = GoldPrimary, fontSize = 14.sp)
                            Text("Sakit", fontSize = 10.sp, color = GoldPrimary)
                        }
                    }
                    Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), color = Color(0xFFE0F2FE)) {
                        Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$izinCount", fontWeight = FontWeight.Bold, color = SkyAccent, fontSize = 14.sp)
                            Text("Izin", fontSize = 10.sp, color = SkyAccent)
                        }
                    }
                    Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), color = Color(0xFFFEE2E2)) {
                        Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$alfaCount", fontWeight = FontWeight.Bold, color = ErrorRed, fontSize = 14.sp)
                            Text("Alfa", fontSize = 10.sp, color = ErrorRed)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Students List with Status Selector
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(classStudents) { student ->
                        val currentStatus = attendanceStatusMap[student.id] ?: "HADIR"
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = GraySurface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(student.fullName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text("NIS: ${student.nis}", fontSize = 11.sp, color = GrayText)
                                }
                                // Status buttons (H, S, I, A)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    StatusChip(label = "H", isSelected = (currentStatus == "HADIR"), color = SuccessGreen) {
                                        attendanceStatusMap[student.id] = "HADIR"
                                    }
                                    StatusChip(label = "S", isSelected = (currentStatus == "SAKIT"), color = GoldPrimary) {
                                        attendanceStatusMap[student.id] = "SAKIT"
                                    }
                                    StatusChip(label = "I", isSelected = (currentStatus == "IZIN"), color = SkyAccent) {
                                        attendanceStatusMap[student.id] = "IZIN"
                                    }
                                    StatusChip(label = "A", isSelected = (currentStatus == "ALFA"), color = ErrorRed) {
                                        attendanceStatusMap[student.id] = "ALFA"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val records = classStudents.map { s ->
                        AttendanceEntity(
                            studentId = s.id,
                            studentName = s.fullName,
                            classId = s.classId,
                            date = today,
                            status = attendanceStatusMap[s.id] ?: "HADIR"
                        )
                    }
                    viewModel.saveAttendanceRecords(records)
                    Toast.makeText(context, "Presensi berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                Text("Simpan Presensi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun StatusChip(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(if (isSelected) color else Color.LightGray.copy(alpha = 0.4f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else Color.DarkGray,
            fontSize = 11.sp
        )
    }
}
