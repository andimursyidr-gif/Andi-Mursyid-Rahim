package com.example.ui.students

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.data.model.ClassEntity
import com.example.data.model.StudentEntity
import com.example.data.preferences.UserRole
import com.example.data.preferences.UserSession
import com.example.ui.SmartLearningViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun StudentManagementScreen(
    viewModel: SmartLearningViewModel,
    session: UserSession,
    classes: List<ClassEntity>,
    students: List<StudentEntity>,
    onOpenImportExcel: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedClassId by remember { mutableLongStateOf(0L) } // 0 = all
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<StudentEntity?>(null) }
    var viewingStudent by remember { mutableStateOf<StudentEntity?>(null) }
    var studentToDelete by remember { mutableStateOf<StudentEntity?>(null) }

    val filteredStudents = students.filter { s ->
        (selectedClassId == 0L || s.classId == selectedClassId) &&
                (searchQuery.isBlank() || s.fullName.contains(searchQuery, ignoreCase = true) || s.nis.contains(searchQuery))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("student_management_screen")
    ) {
        // Search & Filter Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_search_field"),
                    placeholder = { Text("Cari berdasarkan nama atau NIS...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Class Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = (selectedClassId == 0L),
                        onClick = { selectedClassId = 0L },
                        label = { Text("Semua (${students.size})") },
                        modifier = Modifier.testTag("class_filter_all")
                    )
                    classes.forEach { c ->
                        val count = students.count { it.classId == c.id }
                        FilterChip(
                            selected = (selectedClassId == c.id),
                            onClick = { selectedClassId = c.id },
                            label = { Text("${c.className} ($count)") },
                            modifier = Modifier.testTag("class_filter_${c.className}")
                        )
                    }
                }

                // Action Bar for Guru
                if (session.role == UserRole.GURU) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenImportExcel,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_open_import_excel"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import Excel", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { showAddDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_add_student_manual"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tambah Siswa", fontSize = 12.sp)
                        }
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    val pdf = viewModel.generateStudentListPdf(if (selectedClassId == 0L) null else selectedClassId)
                                    Toast.makeText(context, "PDF Berhasil dibuat: ${pdf.name}", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier.testTag("btn_export_student_pdf")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "Cetak PDF", tint = ErrorRed)
                        }
                    }
                }
            }
        }

        // Student List
        if (filteredStudents.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.PersonOff,
                title = "Belum Ada Data Siswa",
                message = if (searchQuery.isNotEmpty()) "Tidak ditemukan siswa dengan kata kunci \"$searchQuery\"" else "Mulai dengan mengimpor data siswa dari Excel atau tambah secara manual.",
                actionLabel = if (session.role == UserRole.GURU) "Import dari Excel" else null,
                onAction = if (session.role == UserRole.GURU) onOpenImportExcel else null
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "Menampilkan ${filteredStudents.size} Peserta Didik",
                        fontSize = 12.sp,
                        color = GrayText,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                items(filteredStudents, key = { it.id }) { student ->
                    StudentItemCard(
                        student = student,
                        isTeacher = (session.role == UserRole.GURU),
                        onClick = { viewingStudent = student },
                        onEdit = { editingStudent = student },
                        onDelete = { studentToDelete = student }
                    )
                }
            }
        }
    }

    // Add / Edit Student Dialog
    if (showAddDialog || editingStudent != null) {
        val student = editingStudent
        AddEditStudentDialog(
            student = student,
            classes = classes,
            onDismiss = {
                showAddDialog = false
                editingStudent = null
            },
            onSave = { nis, nisn, name, gender, birthPlace, birthDate, classId, className, address, phone ->
                if (student == null) {
                    viewModel.addStudent(nis, nisn, name, gender, birthPlace, birthDate, classId, className, address, phone)
                } else {
                    viewModel.updateStudent(
                        student.copy(
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
                showAddDialog = false
                editingStudent = null
            }
        )
    }

    // Student Detail Dialog
    if (viewingStudent != null) {
        StudentDetailDialog(
            student = viewingStudent!!,
            onDismiss = { viewingStudent = null }
        )
    }

    // Delete Confirmation Dialog
    if (studentToDelete != null) {
        AlertDialog(
            onDismissRequest = { studentToDelete = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed) },
            title = { Text("Hapus Siswa") },
            text = { Text("Apakah Anda yakin ingin menghapus data siswa \"${studentToDelete?.fullName}\" (NIS: ${studentToDelete?.nis})? Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                Button(
                    onClick = {
                        studentToDelete?.let { viewModel.deleteStudent(it) }
                        studentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun StudentItemCard(
    student: StudentEntity,
    isTeacher: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("student_card_${student.nis}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (student.gender == "P") Color(0xFFFCE7F3) else BlueContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = student.gender,
                    fontWeight = FontWeight.Bold,
                    color = if (student.gender == "P") Color(0xFFBE185D) else NavyPrimary,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.fullName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "NIS: ${student.nis}  |  Kelas: ${student.className}",
                    fontSize = 12.sp,
                    color = GrayText
                )
                if (student.parentPhone.isNotBlank()) {
                    Text(
                        text = "HP Ortu: ${student.parentPhone}",
                        fontSize = 11.sp,
                        color = GrayText
                    )
                }
            }

            if (isTeacher) {
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = BlueModern, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = ErrorRed, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditStudentDialog(
    student: StudentEntity?,
    classes: List<ClassEntity>,
    onDismiss: () -> Unit,
    onSave: (nis: String, nisn: String, name: String, gender: String, birthPlace: String, birthDate: String, classId: Long, className: String, address: String, phone: String) -> Unit
) {
    var nis by remember { mutableStateOf(student?.nis ?: "") }
    var nisn by remember { mutableStateOf(student?.nisn ?: "") }
    var name by remember { mutableStateOf(student?.fullName ?: "") }
    var gender by remember { mutableStateOf(student?.gender ?: "L") }
    var birthPlace by remember { mutableStateOf(student?.birthPlace ?: "") }
    var birthDate by remember { mutableStateOf(student?.birthDate ?: "2008-01-01") }
    var selectedClass by remember { mutableStateOf(classes.firstOrNull { it.id == student?.classId } ?: classes.firstOrNull() ?: ClassEntity(1, "X RPL 1")) }
    var address by remember { mutableStateOf(student?.address ?: "") }
    var phone by remember { mutableStateOf(student?.parentPhone ?: "") }

    var errorMsg by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (student == null) "Tambah Siswa Baru" else "Edit Data Siswa", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                if (errorMsg.isNotEmpty()) {
                    Text(errorMsg, color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Lengkap *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = nis,
                        onValueChange = { nis = it },
                        label = { Text("NIS *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = nisn,
                        onValueChange = { nisn = it },
                        label = { Text("NISN") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Gender radio
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Jenis Kelamin: ", fontSize = 13.sp)
                    RadioButton(selected = (gender == "L"), onClick = { gender = "L" })
                    Text("L (Laki-laki)", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    RadioButton(selected = (gender == "P"), onClick = { gender = "P" })
                    Text("P (Perempuan)", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = birthPlace,
                        onValueChange = { birthPlace = it },
                        label = { Text("Tempat Lahir") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = birthDate,
                        onValueChange = { birthDate = it },
                        label = { Text("Tgl Lahir (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Nomor HP Orang Tua") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Alamat") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || nis.isBlank()) {
                        errorMsg = "Nama Lengkap dan NIS wajib diisi!"
                    } else {
                        onSave(nis, nisn, name, gender, birthPlace, birthDate, selectedClass.id, selectedClass.className, address, phone)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun StudentDetailDialog(
    student: StudentEntity,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Badge, contentDescription = null, tint = NavyPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Kartu Data Siswa", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(student.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Kelas: ${student.className}", color = NavyPrimary, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(10.dp))
                DetailRow(label = "Nomor Induk Siswa (NIS)", value = student.nis)
                DetailRow(label = "NISN", value = student.nisn.ifBlank { "-" })
                DetailRow(label = "Jenis Kelamin", value = if (student.gender == "L") "Laki-laki" else "Perempuan")
                DetailRow(label = "Tempat & Tgl Lahir", value = "${student.birthPlace}, ${student.birthDate}")
                DetailRow(label = "Nomor HP Orang Tua", value = student.parentPhone.ifBlank { "-" })
                DetailRow(label = "Alamat Domisili", value = student.address.ifBlank { "-" })
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = GrayText)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
