package com.example.ui.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantDialog(
    viewModel: AiLearningViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onDismiss: () -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    val chatState by viewModel.chatUiState.collectAsState()
    val quizState by viewModel.quizGenUiState.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Chat Belajar, 1: Generator Kuis AI
    val listState = rememberLazyListState()

    // Scroll to bottom on new message
    LaunchedEffect(messages.size, chatState) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .clip(RoundedCornerShape(20.dp))
                .testTag("ai_assistant_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NavyPrimary)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(GoldAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI",
                                tint = NavyPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Asisten Belajar Pintar AI",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "SMKN 3 Tana Toraja • Gemini 3.8 Flash",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("ai_dialog_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color.White
                        )
                    }
                }

                // Tabs: Tanya Jawab vs Pembuat Soal AI
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = BlueContainer.copy(alpha = 0.5f)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Tanya Jawab Belajar", fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.Psychology, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Buat Soal Kuis AI", fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.Quiz, contentDescription = null) }
                    )
                }

                if (selectedTab == 0) {
                    // Chat view
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(messages) { msg ->
                                val isUser = msg.sender == "user"
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(
                                            topStart = 14.dp,
                                            topEnd = 14.dp,
                                            bottomStart = if (isUser) 14.dp else 2.dp,
                                            bottomEnd = if (isUser) 2.dp else 14.dp
                                        ),
                                        color = if (isUser) BlueModern else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.widthIn(max = 300.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = if (isUser) "Anda" else "Guru AI SMKN 3 Tana Toraja",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = if (isUser) GoldAccent else NavyPrimary
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = msg.message,
                                                fontSize = 14.sp,
                                                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            if (chatState is AiChatUiState.Streaming) {
                                val partial = (chatState as AiChatUiState.Streaming).partialResponse
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.widthIn(max = 300.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(14.dp),
                                                        strokeWidth = 2.dp,
                                                        color = BlueModern
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Sedang mengetik...",
                                                        fontSize = 11.sp,
                                                        color = NavyPrimary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(text = partial, fontSize = 14.sp)
                                            }
                                        }
                                    }
                                }
                            } else if (chatState is AiChatUiState.Loading) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Gemini AI sedang berpikir...", fontSize = 13.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Input bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                placeholder = { Text("Tanyakan konsep materi, rumus, praktik kejuruan...") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("ai_chat_input"),
                                shape = RoundedCornerShape(24.dp),
                                maxLines = 3
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            FloatingActionButton(
                                onClick = {
                                    if (inputText.isNotBlank()) {
                                        val text = inputText
                                        inputText = ""
                                        viewModel.sendMessage(text)
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("ai_chat_send_button"),
                                containerColor = NavyPrimary,
                                contentColor = Color.White
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Kirim",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Generator Kuis AI Tab
                    var quizTopic by remember { mutableStateOf("") }
                    var questionCount by remember { mutableIntStateOf(3) }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Generator Soal Otomatis (Gemini Structured JSON)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = NavyPrimary
                        )
                        Text(
                            text = "Guru dapat memasukkan topik kompetensi kejuruan dan AI akan menyusun soal pilihan ganda lengkap dengan kunci dan pembahasan.",
                            fontSize = 12.sp,
                            color = GrayText
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = quizTopic,
                            onValueChange = { quizTopic = it },
                            label = { Text("Topik / Mata Pelajaran") },
                            placeholder = { Text("Misal: Keselamatan Kerja K3 Bengkel Otomotif") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ai_quiz_topic_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Jumlah Soal: ", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            listOf(2, 3, 5).forEach { count ->
                                FilterChip(
                                    selected = questionCount == count,
                                    onClick = { questionCount = count },
                                    label = { Text("$count Soal") },
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                viewModel.generateQuizQuestions(quizTopic, questionCount)
                            },
                            enabled = quizTopic.isNotBlank() && quizState !is QuizGenUiState.Loading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ai_generate_quiz_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                        ) {
                            if (quizState is QuizGenUiState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Menyusun Soal dengan Gemini...")
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GoldAccent)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate Soal Kuis AI")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Results list
                        when (val state = quizState) {
                            is QuizGenUiState.Error -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.15f)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Error, contentDescription = null, tint = ErrorRed)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(state.errorMessage, color = ErrorRed, fontSize = 13.sp)
                                    }
                                }
                            }
                            is QuizGenUiState.Success -> {
                                Text(
                                    text = "Hasil Soal Berhasil Dibuat (${state.questions.size} Soal):",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(state.questions) { q ->
                                        ElevatedCard(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(
                                                    text = q.question,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text("A. ${q.optionA}", fontSize = 12.sp)
                                                Text("B. ${q.optionB}", fontSize = 12.sp)
                                                Text("C. ${q.optionC}", fontSize = 12.sp)
                                                Text("D. ${q.optionD}", fontSize = 12.sp)
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = "Kunci: ${q.correctAnswer} • ${q.explanation}",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = SuccessGreen
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}
