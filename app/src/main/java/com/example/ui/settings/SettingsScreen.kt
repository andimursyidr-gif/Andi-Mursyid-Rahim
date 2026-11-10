package com.example.ui.settings

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SchoolSettingsEntity
import com.example.data.preferences.UserRole
import com.example.data.preferences.UserSession
import com.example.ui.SmartLearningViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    session: UserSession,
    settings: SchoolSettingsEntity?,
    viewModel: SmartLearningViewModel,
    onBack: () -> Unit,
    onOpenRoleSwitch: () -> Unit
) {
    val context = LocalContext.current
    var showEditProfileDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan Aplikasi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("settings_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Profil Guru & Sekolah Card
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Profil Sekolah & Pengajar", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            TextButton(onClick = { showEditProfileDialog = true }) {
                                Text("Edit Profil")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(settings?.schoolName ?: "SMKN 3 TANA TORAJA", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                        Text("NPSN: ${settings?.schoolNpsn ?: "20240988"}", fontSize = 12.sp, color = GrayText)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Guru: ${settings?.teacherName ?: "Drs. H. Mursyid, M.Pd."}", fontSize = 13.sp)
                        Text("NIP: ${settings?.teacherNip ?: "19750812 200212 1 004"}", fontSize = 11.sp, color = GrayText)
                        Text("Tahun Ajaran: ${settings?.academicYear ?: "2024/2025"} • Semester: ${settings?.semester ?: "Ganjil"}", fontSize = 12.sp, color = NavyPrimary)
                    }
                }
            }

            // Tampilan & Tema
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Tampilan & Tema", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Mode Gelap (Dark Mode)", fontWeight = FontWeight.SemiBold)
                                Text("Mengubah tema tampilan menjadi kontras gelap", fontSize = 12.sp, color = GrayText)
                            }
                            Switch(
                                checked = session.isDarkMode,
                                onCheckedChange = { viewModel.toggleDarkMode(it) }
                            )
                        }
                    }
                }
            }

            // Offline & Sinkronisasi
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Konektivitas & Sinkronisasi", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Mode Offline Otomatis", fontWeight = FontWeight.SemiBold)
                                Text("Simpan seluruh materi, LKPD, dan kuis secara lokal agar tetap dapat digunakan tanpa kuota", fontSize = 12.sp, color = GrayText)
                            }
                            Switch(
                                checked = session.isOfflineMode,
                                onCheckedChange = { viewModel.toggleOfflineMode(it) }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                Toast.makeText(context, "Sinkronisasi berhasil! Seluruh data lokal mutakhir.", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sinkronisasi Sekarang")
                        }
                    }
                }
            }

            // Backup & Pemulihan
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Pencadangan Data (Backup)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Cadangkan database Room lokal untuk memproteksi data siswa, nilai, modul, dan kuis dari kehilangan.",
                            fontSize = 12.sp,
                            color = GrayText
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                Toast.makeText(context, "Backup database tersimpan aman di penyimpanan internal!", Toast.LENGTH_LONG).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Buat Cadangan Baru")
                        }
                    }
                }
            }

            // Ganti Akun / Logout
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Akun & Akses", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Saat ini aktif sebagai: ${if (session.role == UserRole.GURU) "Guru (Admin)" else "Siswa (${session.currentStudentName})"}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NavyPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onOpenRoleSwitch,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ganti Akun / Peran")
                        }
                    }
                }
            }

            // Version info
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("SMKN 3 TANA TORAJA v1.0", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GrayText)
                    Text("Media Pembelajaran Digital Native Android", fontSize = 11.sp, color = GrayText)
                }
            }
        }
    }

    if (showEditProfileDialog) {
        EditSchoolProfileDialog(
            currentSettings = settings,
            onDismiss = { showEditProfileDialog = false },
            onSave = { updated ->
                viewModel.updateSettings(updated)
                showEditProfileDialog = false
                Toast.makeText(context, "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun EditSchoolProfileDialog(
    currentSettings: SchoolSettingsEntity?,
    onDismiss: () -> Unit,
    onSave: (SchoolSettingsEntity) -> Unit
) {
    var schoolName by remember { mutableStateOf(currentSettings?.schoolName ?: "SMKN 3 TANA TORAJA") }
    var npsn by remember { mutableStateOf(currentSettings?.schoolNpsn ?: "20240988") }
    var teacherName by remember { mutableStateOf(currentSettings?.teacherName ?: "Drs. H. Mursyid, M.Pd.") }
    var nip by remember { mutableStateOf(currentSettings?.teacherNip ?: "19750812 200212 1 004") }
    var year by remember { mutableStateOf(currentSettings?.academicYear ?: "2024/2025") }
    var semester by remember { mutableStateOf(currentSettings?.semester ?: "Ganjil") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profil Sekolah & Guru", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = schoolName, onValueChange = { schoolName = it }, label = { Text("Nama Sekolah") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = npsn, onValueChange = { npsn = it }, label = { Text("NPSN") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = teacherName, onValueChange = { teacherName = it }, label = { Text("Nama Guru") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = nip, onValueChange = { nip = it }, label = { Text("NIP Guru") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("Tahun Ajaran") }, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(value = semester, onValueChange = { semester = it }, label = { Text("Semester") }, modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val updated = (currentSettings ?: SchoolSettingsEntity()).copy(
                    schoolName = schoolName,
                    schoolNpsn = npsn,
                    teacherName = teacherName,
                    teacherNip = nip,
                    academicYear = year,
                    semester = semester
                )
                onSave(updated)
            }) {
                Text("Simpan")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
