package com.example.ui.students

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
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
import com.example.data.model.ClassEntity
import com.example.data.util.ExcelParser
import com.example.data.util.ImportResult
import com.example.data.util.ParsedStudentRow
import com.example.ui.ExcelImportUiState
import com.example.ui.SmartLearningViewModel
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExcelImportScreen(
    viewModel: SmartLearningViewModel,
    classes: List<ClassEntity>,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val importState by viewModel.excelImportState.collectAsState()

    var selectedClass by remember { mutableStateOf(classes.firstOrNull() ?: ClassEntity(1, "X RPL 1")) }
    var currentStep by remember { mutableIntStateOf(1) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }
    var parsedResult by remember { mutableStateOf<ImportResult?>(null) }
    var savedSuccessCount by remember { mutableIntStateOf(0) }
    var isProcessing by remember { mutableStateOf(false) }

    // SAF file picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            val name = uri.lastPathSegment?.substringAfterLast("/") ?: "daftar_siswa.xlsx"
            selectedFileName = name
            currentStep = 2 // Move to read data

            // Trigger parser
            coroutineScope.launch {
                isProcessing = true
                val existing = viewModel.students.value.map { it.nis }.toSet()
                val result = withContext(Dispatchers.IO) {
                    ExcelParser.parseFile(context, uri, name, existing)
                }
                parsedResult = result
                isProcessing = false
                currentStep = 3 // Move to preview & validation
            }
        }
    }

    // Function to load built-in sample template file
    fun loadSampleTemplate() {
        coroutineScope.launch {
            isProcessing = true
            val sampleCsv = ExcelParser.generateSampleTemplate()
            val tempFile = File(context.cacheDir, "Template_Daftar_Siswa_Contoh.csv")
            FileOutputStream(tempFile).use { it.write(sampleCsv.toByteArray()) }
            val uri = Uri.fromFile(tempFile)
            selectedFileUri = uri
            selectedFileName = tempFile.name
            val existing = viewModel.students.value.map { it.nis }.toSet()
            val result = withContext(Dispatchers.IO) {
                ExcelParser.parseFile(context, uri, tempFile.name, existing)
            }
            parsedResult = result
            isProcessing = false
            currentStep = 3
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Siswa dari Excel / CSV", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("import_back_button")) {
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
                .testTag("excel_import_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Step Progress Indicator
            item {
                ImportStepIndicator(currentStep = currentStep)
            }

            // Target Class Selector Card
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Target Kelas Penyimpanan",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            classes.forEach { c ->
                                FilterChip(
                                    selected = (selectedClass.id == c.id),
                                    onClick = { selectedClass = c },
                                    label = { Text(c.className) },
                                    modifier = Modifier.testTag("target_class_chip_${c.className}")
                                )
                            }
                        }
                    }
                }
            }

            // TAHAP 1 & 2: Pilih File
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(BlueContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("1", fontWeight = FontWeight.Bold, color = NavyPrimary)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Tahap 1: Pilih File Excel dari Perangkat", fontWeight = FontWeight.Bold)
                                Text("Mendukung format .xlsx, .xls, dan .csv", fontSize = 12.sp, color = GrayText)
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        if (selectedFileName.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = GraySurface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = SuccessGreen)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(selectedFileName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text("File siap diproses & divalidasi", fontSize = 11.sp, color = GrayText)
                                    }
                                    StatusBadge(text = "Terpilih", statusType = "SUCCESS")
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { filePickerLauncher.launch("*/*") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("choose_excel_file_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                            ) {
                                Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Buka File Manager")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = { loadSampleTemplate() },
                                modifier = Modifier.testTag("load_sample_template_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Preview, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tes Template")
                            }
                        }
                    }
                }
            }

            // TAHAP 3 & 4: Pratinjau & Validasi Data
            if (isProcessing) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = NavyPrimary)
                    }
                }
            } else if (parsedResult != null) {
                val res = parsedResult!!

                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(GoldLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("2", fontWeight = FontWeight.Bold, color = GoldPrimary)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Tahap 2 & 3: Pratinjau & Hasil Validasi", fontWeight = FontWeight.Bold)
                                    Text("${res.totalFound} baris data siswa ditemukan", fontSize = 12.sp, color = GrayText)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            // Summary Pills
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ValidationPill(
                                    label = "Valid",
                                    count = res.validCount,
                                    bgColor = Color(0xFFDCFCE7),
                                    textColor = Color(0xFF15803D),
                                    modifier = Modifier.weight(1f)
                                )
                                ValidationPill(
                                    label = "Ganda",
                                    count = res.duplicateCount,
                                    bgColor = Color(0xFFFEF3C7),
                                    textColor = Color(0xFFB45309),
                                    modifier = Modifier.weight(1f)
                                )
                                ValidationPill(
                                    label = "Tidak Valid",
                                    count = res.invalidCount,
                                    bgColor = Color(0xFFFEE2E2),
                                    textColor = Color(0xFFB91C1C),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Table of Parsed Rows
                item {
                    Text(
                        text = "Daftar Baris Siswa:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                items(res.rows) { row ->
                    StudentRowPreviewCard(row = row)
                }

                // TAHAP 5: SIMPAN DATA
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = NavySurface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Tahap 4 & 5: Simpan ke Database Room",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Hanya baris yang valid (${res.validCount} siswa) yang akan disimpan ke kelas ${selectedClass.className}. Data ganda atau tidak valid akan otomatis dilewati.",
                                color = GrayBorder,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    if (selectedFileUri != null) {
                                        viewModel.importExcelFile(
                                            uri = selectedFileUri!!,
                                            fileName = selectedFileName,
                                            targetClassId = selectedClass.id,
                                            targetClassName = selectedClass.className
                                        )
                                        savedSuccessCount = res.validCount
                                        currentStep = 5
                                        Toast.makeText(
                                            context,
                                            "Berhasil menyimpan ${res.validCount} siswa ke ${selectedClass.className}!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("save_all_students_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                enabled = (res.validCount > 0)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("SIMPAN SEMUA DATA (${res.validCount} SISWA)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // TAHAP 6: Ekspor & Unduh Template
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = NavyPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tahap 6: Template & Ekspor", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Unduh template Excel / CSV kosong dengan format kolom standar agar guru dapat mengisi data siswa dengan mudah.",
                            fontSize = 12.sp,
                            color = GrayText
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        val templateContent = ExcelParser.generateSampleTemplate()
                                        val dir = context.getExternalFilesDir(null) ?: context.filesDir
                                        val tFile = File(dir, "Template_Format_Daftar_Siswa.csv")
                                        FileOutputStream(tFile).use { it.write(templateContent.toByteArray()) }
                                        Toast.makeText(context, "Template tersimpan di: ${tFile.name}", Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("download_template_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Unduh Template", fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        val file = viewModel.exportStudentsCsv(selectedClass.id)
                                        Toast.makeText(context, "Ekspor CSV berhasil: ${file.name}", Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("export_csv_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.TableView, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ekspor CSV/Excel", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ImportStepIndicator(currentStep: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val steps = listOf("Pilih File", "Baca Data", "Pratinjau", "Validasi", "Simpan")
        steps.forEachIndexed { index, stepName ->
            val stepNumber = index + 1
            val isActive = currentStep >= stepNumber
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isActive) NavyPrimary else GrayBorder),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$stepNumber",
                        color = if (isActive) Color.White else GrayText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stepName,
                    fontSize = 10.sp,
                    color = if (isActive) NavyPrimary else GrayText,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun ValidationPill(
    label: String,
    count: Int,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = bgColor
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("$count", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor)
            Text(label, fontSize = 11.sp, color = textColor)
        }
    }
}

@Composable
fun StudentRowPreviewCard(row: ParsedStudentRow) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("preview_row_${row.rowNumber}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (row.isDuplicate) Color(0xFFFFFBEB) else if (!row.isValid) Color(0xFFFFF1F2) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${row.rowNumber}. ${row.fullName.ifBlank { "(Nama Kosong)" }}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                if (row.isValid && !row.isDuplicate) {
                    StatusBadge(text = "Valid", statusType = "SUCCESS")
                } else if (row.isDuplicate) {
                    StatusBadge(text = "Duplikat", statusType = "WARNING")
                } else {
                    StatusBadge(text = "Error", statusType = "ERROR")
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "NIS: ${row.nis.ifBlank { "-" }} • NISN: ${row.nisn.ifBlank { "-" }} • JK: ${row.gender}",
                fontSize = 12.sp,
                color = GrayText
            )
            if (row.birthPlace.isNotBlank() || row.birthDate.isNotBlank()) {
                Text(
                    text = "Lahir: ${row.birthPlace}, ${row.birthDate}",
                    fontSize = 11.sp,
                    color = GrayText
                )
            }
            if (row.validationErrors.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                row.validationErrors.forEach { err ->
                    Text(
                        text = "⚠ $err",
                        color = ErrorRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
