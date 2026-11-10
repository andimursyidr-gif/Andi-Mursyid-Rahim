package com.example.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.*
import com.example.data.preferences.UserRole
import com.example.data.preferences.UserSession
import com.example.ui.components.MainNavTab
import com.example.ui.components.MetricCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    session: UserSession,
    classes: List<ClassEntity>,
    students: List<StudentEntity>,
    videos: List<VideoEntity>,
    modules: List<TeachingModuleEntity>,
    worksheets: List<WorksheetEntity>,
    quizzes: List<QuizEntity>,
    announcements: List<AnnouncementEntity>,
    settings: SchoolSettingsEntity?,
    onNavigateTab: (MainNavTab) -> Unit,
    onOpenImportExcel: () -> Unit,
    onOpenCreateQuiz: () -> Unit,
    onOpenCreateWorksheet: () -> Unit,
    onOpenAttendance: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Hero Header Banner
        item {
            DashboardHeroBanner(
                session = session,
                settings = settings
            )
        }

        if (session.role == UserRole.GURU) {
            // === GURU (ADMIN) DASHBOARD ===
            item {
                Text(
                    text = "Statistik & Ringkasan Pembelajaran",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }

            // Metric Cards Grid
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    MetricCard(
                        title = "Jumlah Kelas",
                        value = "${classes.size}",
                        subtitle = "Aktif TA 24/25",
                        icon = Icons.Default.Class,
                        iconTint = BlueModern,
                        iconBg = BlueContainer,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(MainNavTab.STUDENTS) }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    MetricCard(
                        title = "Total Siswa",
                        value = "${students.size}",
                        subtitle = "Terdaftar",
                        icon = Icons.Default.People,
                        iconTint = GoldPrimary,
                        iconBg = GoldLight,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(MainNavTab.STUDENTS) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    MetricCard(
                        title = "Modul Ajar",
                        value = "${modules.size}",
                        subtitle = "PDF & Dokumen",
                        icon = Icons.Default.MenuBook,
                        iconTint = Color(0xFF0D9488),
                        iconBg = Color(0xFFCCFBF1),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(MainNavTab.MATERIALS) }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    MetricCard(
                        title = "Video Belajar",
                        value = "${videos.size}",
                        subtitle = "Interaktif",
                        icon = Icons.Default.VideoLibrary,
                        iconTint = Color(0xFFE11D48),
                        iconBg = Color(0xFFFFE4E6),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(MainNavTab.MATERIALS) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    MetricCard(
                        title = "LKPD Siswa",
                        value = "${worksheets.size}",
                        subtitle = "Lembar Kerja",
                        icon = Icons.Default.Assignment,
                        iconTint = Color(0xFF7C3AED),
                        iconBg = Color(0xFFEDE9FE),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(MainNavTab.ASSESSMENTS) }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    MetricCard(
                        title = "Kuis Evaluasi",
                        value = "${quizzes.size}",
                        subtitle = "Auto Koreksi",
                        icon = Icons.Default.Quiz,
                        iconTint = Color(0xFFD97706),
                        iconBg = Color(0xFFFEF3C7),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(MainNavTab.ASSESSMENTS) }
                    )
                }
            }

            // Quick Actions Bar
            item {
                SectionHeader(title = "Aksi Cepat Guru")
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        QuickActionButton(
                            label = "Import Excel",
                            icon = Icons.Default.UploadFile,
                            color = SuccessGreen,
                            onClick = onOpenImportExcel,
                            testTag = "quick_action_import_excel"
                        )
                    }
                    item {
                        QuickActionButton(
                            label = "Absensi Siswa",
                            icon = Icons.Default.FactCheck,
                            color = BlueModern,
                            onClick = onOpenAttendance,
                            testTag = "quick_action_attendance"
                        )
                    }
                    item {
                        QuickActionButton(
                            label = "Buat LKPD",
                            icon = Icons.Default.PostAdd,
                            color = Color(0xFF7C3AED),
                            onClick = onOpenCreateWorksheet,
                            testTag = "quick_action_create_worksheet"
                        )
                    }
                    item {
                        QuickActionButton(
                            label = "Buat Kuis",
                            icon = Icons.Default.AddCircleOutline,
                            color = GoldPrimary,
                            onClick = onOpenCreateQuiz,
                            testTag = "quick_action_create_quiz"
                        )
                    }
                }
            }

            // Academic Progress Card
            item {
                Spacer(modifier = Modifier.height(14.dp))
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Progres Kelulusan & Nilai Rata-rata",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            StatusBadge(text = "Tuntas 92%", statusType = "SUCCESS")
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { 0.92f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = SuccessGreen,
                            trackColor = GraySurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Rata-rata Kuis: 88.5", fontSize = 12.sp, color = GrayText)
                            Text("LKPD Terkumpul: 94%", fontSize = 12.sp, color = GrayText)
                        }
                    }
                }
            }

        } else {
            // === SISWA DASHBOARD ===
            item {
                Spacer(modifier = Modifier.height(14.dp))
                StudentProgressCard(
                    studentName = session.currentStudentName,
                    className = session.currentClassName,
                    videosWatched = videos.count { it.isWatched },
                    totalVideos = videos.size,
                    modulesCompleted = modules.count { it.isCompleted },
                    totalModules = modules.size,
                    onNavigateMaterials = { onNavigateTab(MainNavTab.MATERIALS) }
                )
            }

            item {
                SectionHeader(
                    title = "Tugas yang Harus Diselesaikan",
                    actionText = "Lihat Semua",
                    onActionClick = { onNavigateTab(MainNavTab.ASSESSMENTS) }
                )
            }

            items(worksheets.take(3)) { w ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { onNavigateTab(MainNavTab.ASSESSMENTS) },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(BlueContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Assignment, contentDescription = null, tint = NavyPrimary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(w.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("Batas: ${w.dueDate} | ${w.subject}", fontSize = 12.sp, color = GrayText)
                        }
                        StatusBadge(text = "Tugas", statusType = "INFO")
                    }
                }
            }

            item {
                SectionHeader(
                    title = "Video Belajar Terbaru",
                    actionText = "Buka Video",
                    onActionClick = { onNavigateTab(MainNavTab.MATERIALS) }
                )
            }

            items(videos.take(2)) { v ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { onNavigateTab(MainNavTab.MATERIALS) },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFE4E6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFFE11D48))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(v.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${v.durationText} • ${v.subject}", fontSize = 12.sp, color = GrayText)
                        }
                        if (v.isWatched) {
                            StatusBadge(text = "Selesai", statusType = "SUCCESS")
                        } else {
                            StatusBadge(text = "Tonton", statusType = "WARNING")
                        }
                    }
                }
            }
        }

        // Announcements Section for Both
        item {
            Spacer(modifier = Modifier.height(14.dp))
            SectionHeader(title = "Pengumuman & Informasi Terbaru")
        }

        items(announcements.take(3)) { a ->
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (a.priority == "URGENT") Color(0xFFFFF1F2) else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = a.title,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f)
                        )
                        StatusBadge(
                            text = a.priority,
                            statusType = if (a.priority == "URGENT") "ERROR" else if (a.priority == "PENTING") "WARNING" else "INFO"
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = a.content,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Oleh: ${a.authorName} • Target: ${a.targetClassName}",
                        fontSize = 11.sp,
                        color = GrayText
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardHeroBanner(
    session: UserSession,
    settings: SchoolSettingsEntity?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(NavyDark, NavyPrimary, BlueModern)
                )
            )
    ) {
        // Background banner graphic if available
        Image(
            painter = painterResource(id = R.drawable.banner_smart_learning_1794280714357),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 0.dp),
            alpha = 0.35f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = settings?.schoolName ?: "SMKN 3 TANA TORAJA",
                style = MaterialTheme.typography.labelSmall,
                color = GoldAccent,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (session.role == UserRole.GURU)
                    "Selamat Datang, ${settings?.teacherName ?: "Bapak/Ibu Guru"}"
                else
                    "Halo, ${session.currentStudentName}!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = if (session.role == UserRole.GURU)
                    "Tahun Ajaran ${settings?.academicYear ?: "2024/2025"} • Semester ${settings?.semester ?: "Ganjil"}"
                else
                    "Kelas: ${session.currentClassName} • NIS: ${session.currentStudentNis}",
                style = MaterialTheme.typography.bodySmall,
                color = BlueContainer
            )
        }
    }
}

@Composable
fun QuickActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    testTag: String
) {
    ElevatedCard(
        modifier = Modifier
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun StudentProgressCard(
    studentName: String,
    className: String,
    videosWatched: Int,
    totalVideos: Int,
    modulesCompleted: Int,
    totalModules: Int,
    onNavigateMaterials: () -> Unit
) {
    val totalTasks = totalVideos + totalModules
    val completedTasks = videosWatched + modulesCompleted
    val progressFraction = if (totalTasks > 0) completedTasks.toFloat() / totalTasks.toFloat() else 0.75f
    val percent = (progressFraction * 100).toInt()

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = NavySurface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Progres Belajar Mandiri",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Kelas $className • Semester Ganjil",
                        color = GrayText,
                        fontSize = 12.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(GoldPrimary)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("$percent%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = GoldAccent,
                trackColor = Color(0xFF334155)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Video: $videosWatched / $totalVideos Ditonton", color = Color.White, fontSize = 12.sp)
                Text("Modul: $modulesCompleted / $totalModules Dibaca", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}
