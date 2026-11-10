package com.example.ui.announcements

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnnouncementEntity
import com.example.data.model.ClassEntity
import com.example.data.preferences.UserRole
import com.example.data.preferences.UserSession
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun AnnouncementDialog(
    session: UserSession,
    classes: List<ClassEntity>,
    announcements: List<AnnouncementEntity>,
    onDismiss: () -> Unit,
    onAddAnnouncement: (title: String, content: String, author: String, classId: Long, className: String, priority: String) -> Unit,
    onDeleteAnnouncement: (AnnouncementEntity) -> Unit
) {
    var showCreateForm by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("NORMAL") }
    var selectedClass by remember { mutableStateOf(classes.firstOrNull() ?: ClassEntity(0, "Semua Kelas")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Campaign, contentDescription = null, tint = NavyPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pemberitahuan & Pengumuman", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
            ) {
                if (!showCreateForm) {
                    if (session.role == UserRole.GURU) {
                        Button(
                            onClick = { showCreateForm = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Buat Pengumuman Baru")
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    if (announcements.isEmpty()) {
                        Text("Belum ada pengumuman.", color = GrayText, fontSize = 13.sp)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(announcements) { a ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = GraySurface,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(a.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                            StatusBadge(
                                                text = a.priority,
                                                statusType = if (a.priority == "URGENT") "ERROR" else if (a.priority == "PENTING") "WARNING" else "INFO"
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(a.content, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Target: ${a.targetClassName} • Oleh: ${a.authorName}", fontSize = 10.sp, color = GrayText)
                                            if (session.role == UserRole.GURU) {
                                                IconButton(
                                                    onClick = { onDeleteAnnouncement(a) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Create Form
                    Text("Tulis Pengumuman Baru", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Judul Pengumuman") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Isi Pengumuman") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Prioritas: ", fontSize = 12.sp)
                        listOf("NORMAL", "PENTING", "URGENT").forEach { p ->
                            FilterChip(
                                selected = (priority == p),
                                onClick = { priority = p },
                                label = { Text(p, fontSize = 10.sp) },
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showCreateForm = false }) { Text("Batal") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    onAddAnnouncement(
                                        title,
                                        content,
                                        session.currentStudentName.ifBlank { "Admin Guru" },
                                        selectedClass.id,
                                        selectedClass.className,
                                        priority
                                    )
                                    showCreateForm = false
                                }
                            }
                        ) {
                            Text("Publikasikan")
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!showCreateForm) {
                TextButton(onClick = onDismiss) { Text("Tutup") }
            }
        }
    )
}
