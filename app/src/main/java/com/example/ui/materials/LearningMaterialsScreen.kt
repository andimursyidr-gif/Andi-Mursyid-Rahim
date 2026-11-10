package com.example.ui.materials

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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

@Composable
fun LearningMaterialsScreen(
    viewModel: SmartLearningViewModel,
    session: UserSession,
    classes: List<ClassEntity>,
    videos: List<VideoEntity>,
    modules: List<TeachingModuleEntity>,
    mediaList: List<LearningMediaEntity>
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Video, 1: Modul, 2: Media
    val tabs = listOf("Video Belajar", "Modul Ajar PDF", "Media & Galeri")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("learning_materials_screen")
    ) {
        // Tab Header
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
                    modifier = Modifier.testTag("materials_tab_$index")
                )
            }
        }

        when (selectedTab) {
            0 -> VideoListTab(
                viewModel = viewModel,
                session = session,
                classes = classes,
                videos = videos
            )
            1 -> ModuleListTab(
                viewModel = viewModel,
                session = session,
                classes = classes,
                modules = modules
            )
            2 -> MediaGalleryTab(
                viewModel = viewModel,
                session = session,
                classes = classes,
                mediaList = mediaList
            )
        }
    }
}

// ==================== TAB 1: VIDEO PEMBELAJARAN ====================
@Composable
fun VideoListTab(
    viewModel: SmartLearningViewModel,
    session: UserSession,
    classes: List<ClassEntity>,
    videos: List<VideoEntity>
) {
    val context = LocalContext.current
    var showAddVideoDialog by remember { mutableStateOf(false) }
    var selectedVideoForPlay by remember { mutableStateOf<VideoEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedClassId by remember { mutableLongStateOf(0L) }

    val filtered = videos.filter { v ->
        (selectedClassId == 0L || v.classId == selectedClassId || v.classId == 0L) &&
                (searchQuery.isBlank() || v.title.contains(searchQuery, ignoreCase = true) || v.subject.contains(searchQuery, ignoreCase = true))
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header actions
        Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Cari judul video / materi...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    if (session.role == UserRole.GURU) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { showAddVideoDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            modifier = Modifier.testTag("btn_add_video")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tambah", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.VideoLibrary,
                title = "Belum Ada Video Pembelajaran",
                message = "Tambahkan tautan video YouTube atau materi multimedia untuk kelas ini."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.id }) { v ->
                    VideoItemCard(
                        video = v,
                        isTeacher = (session.role == UserRole.GURU),
                        onPlay = {
                            selectedVideoForPlay = v
                            if (session.role == UserRole.SISWA) {
                                viewModel.markVideoWatched(v.id, true)
                            }
                        },
                        onToggleWatched = {
                            viewModel.markVideoWatched(v.id, !v.isWatched)
                        },
                        onDelete = {
                            viewModel.deleteVideo(v)
                        }
                    )
                }
            }
        }
    }

    if (showAddVideoDialog) {
        AddVideoDialog(
            classes = classes,
            onDismiss = { showAddVideoDialog = false },
            onSave = { title, desc, subject, classId, className, topic, url, duration ->
                viewModel.addVideo(title, desc, subject, classId, className, topic, url, duration)
                showAddVideoDialog = false
            }
        )
    }

    if (selectedVideoForPlay != null) {
        val v = selectedVideoForPlay!!
        AlertDialog(
            onDismissRequest = { selectedVideoForPlay = null },
            title = {
                Text(v.title, fontWeight = FontWeight.Bold, maxLines = 2)
            },
            text = {
                Column {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = NavyDark
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.PlayCircle, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(54.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Pemutar Video Pembelajaran", color = Color.White, fontWeight = FontWeight.Bold)
                                Text(v.durationText, color = BlueContainer, fontSize = 12.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Topik: ${v.topic} | Mapel: ${v.subject}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(v.description, fontSize = 12.sp, color = GrayText)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("URL: ${v.youtubeUrl}", fontSize = 11.sp, color = BlueModern)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(v.youtubeUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Membuka link video: ${v.youtubeUrl}", Toast.LENGTH_SHORT).show()
                        }
                        selectedVideoForPlay = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Buka di YouTube")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedVideoForPlay = null }) {
                    Text("Tutup")
                }
            }
        )
    }
}

@Composable
fun VideoItemCard(
    video: VideoEntity,
    isTeacher: Boolean,
    onPlay: () -> Unit,
    onToggleWatched: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("video_card_${video.id}"),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFE4E6))
                        .clickable { onPlay() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color(0xFFE11D48), modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(video.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${video.subject} • ${video.className}", fontSize = 12.sp, color = GrayText)
                }
                if (video.isWatched) {
                    StatusBadge(text = "Selesai", statusType = "SUCCESS")
                } else {
                    StatusBadge(text = video.durationText, statusType = "INFO")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(video.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onPlay) {
                    Icon(Icons.Default.PlayCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tonton Sekarang", fontSize = 12.sp)
                }
                Row {
                    IconButton(onClick = onToggleWatched, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (video.isWatched) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                            contentDescription = "Tandai Selesai",
                            tint = if (video.isWatched) SuccessGreen else GrayText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    if (isTeacher) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = ErrorRed, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

// ==================== TAB 2: MODUL AJAR PDF ====================
@Composable
fun ModuleListTab(
    viewModel: SmartLearningViewModel,
    session: UserSession,
    classes: List<ClassEntity>,
    modules: List<TeachingModuleEntity>
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var readingModule by remember { mutableStateOf<TeachingModuleEntity?>(null) }

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
                    Text("Daftar Modul Ajar (${modules.size})", fontWeight = FontWeight.Bold)
                    Button(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        modifier = Modifier.testTag("btn_add_module")
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Unggah Modul", fontSize = 12.sp)
                    }
                }
            }
        }

        if (modules.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.MenuBook,
                title = "Belum Ada Modul Ajar",
                message = "Unggah modul pembelajaran format PDF atau DOCX untuk memandu siswa."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(modules, key = { it.id }) { m ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { readingModule = m }
                            .testTag("module_card_${m.id}"),
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
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFFEF2F2)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(26.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(m.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("${m.subject} • ${m.className} • ${m.fileSize}", fontSize = 12.sp, color = GrayText)
                                }
                                if (m.isCompleted) {
                                    StatusBadge(text = "Tuntas", statusType = "SUCCESS")
                                } else {
                                    StatusBadge(text = m.fileType, statusType = "INFO")
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(m.description, fontSize = 12.sp, color = GrayText, maxLines = 2)

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { readingModule = m }) {
                                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Buka & Baca Modul", fontSize = 12.sp)
                                }
                                Row {
                                    IconButton(
                                        onClick = {
                                            viewModel.markModuleCompleted(m.id, !m.isCompleted)
                                            Toast.makeText(context, if (!m.isCompleted) "Ditandai selesai dibaca!" else "Status dibatalkan", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (m.isCompleted) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                                            contentDescription = null,
                                            tint = if (m.isCompleted) SuccessGreen else GrayText,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    if (session.role == UserRole.GURU) {
                                        IconButton(onClick = { viewModel.deleteModule(m) }, modifier = Modifier.size(36.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(18.dp))
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

    if (showAddDialog) {
        AddModuleDialog(
            classes = classes,
            onDismiss = { showAddDialog = false },
            onSave = { title, desc, subject, classId, className, topic, objectives, fileType, size, summary ->
                viewModel.addModule(title, desc, subject, classId, className, topic, objectives, fileType, size, summary)
                showAddDialog = false
            }
        )
    }

    if (readingModule != null) {
        val m = readingModule!!
        AlertDialog(
            onDismissRequest = { readingModule = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = NavyPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pembaca Modul Ajar Digital", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                ) {
                    Text(m.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Mata Pelajaran: ${m.subject} • Kelas: ${m.className}", fontSize = 12.sp, color = NavyPrimary)
                    Spacer(modifier = Modifier.height(10.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Tujuan Pembelajaran:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(m.learningObjectives, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Ringkasan Isi & Bab Modul:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = GraySurface,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = m.contentSummary.ifBlank { m.description },
                            fontSize = 12.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.markModuleCompleted(m.id, true)
                        Toast.makeText(context, "Modul selesai dipelajari!", Toast.LENGTH_SHORT).show()
                        readingModule = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tandai Selesai")
                }
            },
            dismissButton = {
                TextButton(onClick = { readingModule = null }) {
                    Text("Tutup")
                }
            }
        )
    }
}

// ==================== TAB 3: MEDIA & GALERI ====================
@Composable
fun MediaGalleryTab(
    viewModel: SmartLearningViewModel,
    session: UserSession,
    classes: List<ClassEntity>,
    mediaList: List<LearningMediaEntity>
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("SEMUA") }
    val categories = listOf("SEMUA", "INFOGRAFIS", "PRESENTASI", "INTERAKTIF", "GAMBAR", "AUDIO")

    val filtered = mediaList.filter {
        selectedCategory == "SEMUA" || it.category.equals(selectedCategory, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Galeri Media Pembelajaran", fontWeight = FontWeight.Bold)
                    if (session.role == UserRole.GURU) {
                        Button(
                            onClick = { showAddDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            modifier = Modifier.testTag("btn_add_media")
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tambah Media", fontSize = 12.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = (selectedCategory == cat),
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.PermMedia,
                title = "Belum Ada Media",
                message = "Tambahkan infografis, slide presentasi, atau laboratorium virtual interaktif."
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.id }) { m ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("media_card_${m.id}"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        when (m.category) {
                                            "INFOGRAFIS" -> Color(0xFFCCFBF1)
                                            "INTERAKTIF" -> Color(0xFFFEF3C7)
                                            "PRESENTASI" -> Color(0xFFEDE9FE)
                                            else -> BlueContainer
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (m.category) {
                                        "INFOGRAFIS" -> Icons.Default.InsertChart
                                        "INTERAKTIF" -> Icons.Default.Science
                                        "PRESENTASI" -> Icons.Default.Slideshow
                                        "AUDIO" -> Icons.Default.Headphones
                                        else -> Icons.Default.Image
                                    },
                                    contentDescription = null,
                                    tint = NavyPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(m.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(m.category, fontSize = 11.sp, color = GoldPrimary, fontWeight = FontWeight.SemiBold)
                            Text(m.description, fontSize = 11.sp, color = GrayText, maxLines = 2)

                            if (session.role == UserRole.GURU) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(
                                        onClick = { viewModel.deleteMedia(m) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddMediaDialog(
            classes = classes,
            onDismiss = { showAddDialog = false },
            onSave = { title, desc, cat, url, cId, cName ->
                viewModel.addMedia(title, desc, cat, url, cId, cName)
                showAddDialog = false
            }
        )
    }
}

// Dialogs for Adding Items
@Composable
fun AddVideoDialog(
    classes: List<ClassEntity>,
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, subject: String, classId: Long, className: String, topic: String, url: String, duration: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Pemrograman Bergerak") }
    var topic by remember { mutableStateOf("Android & Kotlin") }
    var url by remember { mutableStateOf("https://www.youtube.com/watch?v=sample") }
    var duration by remember { mutableStateOf("15 Menit") }
    var selectedClass by remember { mutableStateOf(classes.firstOrNull() ?: ClassEntity(1, "X RPL 1")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Video Pembelajaran", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Judul Video") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Mata Pelajaran") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = topic, onValueChange = { topic = it }, label = { Text("Topik Pembelajaran") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("Link YouTube / Video URL") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Durasi (cth: 20 Menit)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isNotBlank()) onSave(title, desc, subject, selectedClass.id, selectedClass.className, topic, url, duration)
            }) {
                Text("Simpan")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
fun AddModuleDialog(
    classes: List<ClassEntity>,
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, subject: String, classId: Long, className: String, topic: String, objectives: String, fileType: String, size: String, summary: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Basis Data") }
    var topic by remember { mutableStateOf("Normalisasi") }
    var objectives by remember { mutableStateOf("1. Memahami 1NF, 2NF, 3NF.\n2. Mampu membuat skema database optimal.") }
    var summary by remember { mutableStateOf("BAB 1: Pengantar Basis Data\nBAB 2: Relasi Entitas\nBAB 3: Praktik Query SQL") }
    var selectedClass by remember { mutableStateOf(classes.firstOrNull() ?: ClassEntity(1, "X RPL 1")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Unggah Modul Ajar Digital", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Judul Modul") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Mata Pelajaran") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = topic, onValueChange = { topic = it }, label = { Text("Topik") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = objectives, onValueChange = { objectives = it }, label = { Text("Tujuan Pembelajaran") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = summary, onValueChange = { summary = it }, label = { Text("Ringkasan / Bab") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isNotBlank()) onSave(title, desc, subject, selectedClass.id, selectedClass.className, topic, objectives, "PDF", "2.8 MB", summary)
            }) {
                Text("Simpan Modul")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
fun AddMediaDialog(
    classes: List<ClassEntity>,
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, category: String, url: String, classId: Long, className: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("INFOGRAFIS") }
    var url by remember { mutableStateOf("https://sample-media.org/image.png") }
    var selectedClass by remember { mutableStateOf(classes.firstOrNull() ?: ClassEntity(1, "X RPL 1")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Media Pembelajaran", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Judul Media") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Kategori (INFOGRAFIS/INTERAKTIF/SLIDE)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("Tautan Media URL") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isNotBlank()) onSave(title, desc, category, url, selectedClass.id, selectedClass.className)
            }) {
                Text("Simpan")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
