package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.StudentEntity
import com.example.data.preferences.UserRole
import com.example.data.preferences.UserSession
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    session: UserSession,
    students: List<StudentEntity>,
    announcementCount: Int,
    onRoleSwitch: (UserRole) -> Unit,
    onSelectStudent: (StudentEntity) -> Unit,
    onAnnouncementsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onToggleOffline: (Boolean) -> Unit
) {
    var showRoleDialog by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_custom_logo),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SMKN 3 TANA TORAJA",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                // Subtitle indicating active role & user
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (session.role == UserRole.GURU) NavyPrimary else BlueModern
                            )
                            .clickable { showRoleDialog = true }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (session.role == UserRole.GURU) "Mode Guru (Admin)" else "Siswa: ${session.currentStudentName}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    // Offline badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (session.isOfflineMode) WarningOrange else SuccessGreen)
                            .clickable { onToggleOffline(!session.isOfflineMode) }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (session.isOfflineMode) "Offline" else "Online (Sync)",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        actions = {
            // Announcements with badge
            IconButton(
                onClick = onAnnouncementsClick,
                modifier = Modifier.testTag("topbar_announcements_button")
            ) {
                BadgedBox(
                    badge = {
                        if (announcementCount > 0) {
                            Badge(
                                containerColor = ErrorRed,
                                contentColor = Color.White
                            ) {
                                Text("$announcementCount")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Pengumuman",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Role Switch Quick Button
            IconButton(
                onClick = { showRoleDialog = true },
                modifier = Modifier.testTag("topbar_switch_role_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Ganti Peran",
                    tint = GoldAccent
                )
            }

            // Settings Button
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.testTag("topbar_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Pengaturan",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    )

    if (showRoleDialog) {
        RoleSwitchDialog(
            currentRole = session.role,
            students = students,
            currentStudentId = session.currentStudentId,
            onDismiss = { showRoleDialog = false },
            onSelectRole = { role ->
                onRoleSwitch(role)
                showRoleDialog = false
            },
            onSelectStudent = { student ->
                onSelectStudent(student)
                onRoleSwitch(UserRole.SISWA)
                showRoleDialog = false
            }
        )
    }
}

@Composable
fun RoleSwitchDialog(
    currentRole: UserRole,
    students: List<StudentEntity>,
    currentStudentId: Long,
    onDismiss: () -> Unit,
    onSelectRole: (UserRole) -> Unit,
    onSelectStudent: (StudentEntity) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = NavyPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ganti Akun & Hak Akses", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Pilih peran untuk menguji antarmuka Guru atau Siswa:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GrayText
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Option Guru
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectRole(UserRole.GURU) }
                        .testTag("role_option_guru"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (currentRole == UserRole.GURU) BlueContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (currentRole == UserRole.GURU),
                            onClick = { onSelectRole(UserRole.GURU) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Akun Guru (Admin)", fontWeight = FontWeight.Bold)
                            Text("Akses penuh: kelola kelas, import Excel, buat modul, LKPD, kuis & rekap nilai", fontSize = 12.sp, color = GrayText)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Masuk Sebagai Siswa:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(6.dp))

                // Student options
                students.take(5).forEach { s ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onSelectStudent(s) }
                            .testTag("role_option_student_${s.nis}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (currentRole == UserRole.SISWA && currentStudentId == s.id) BlueContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (currentRole == UserRole.SISWA && currentStudentId == s.id),
                                onClick = { onSelectStudent(s) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(s.fullName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("NIS: ${s.nis} | Kelas: ${s.className}", fontSize = 11.sp, color = GrayText)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}
