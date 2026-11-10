package com.example.ui.ai

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject

private const val TAG = "AiLearningViewModel"

sealed interface AiChatUiState {
    data object Idle : AiChatUiState
    data object Loading : AiChatUiState
    data class Streaming(val partialResponse: String) : AiChatUiState
    data class Success(val fullResponse: String) : AiChatUiState
    data class Error(val errorMessage: String) : AiChatUiState
}

data class GeneratedQuizItem(
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswer: String, // "A", "B", "C", or "D"
    val explanation: String
)

sealed interface QuizGenUiState {
    data object Idle : QuizGenUiState
    data object Loading : QuizGenUiState
    data class Success(val questions: List<GeneratedQuizItem>) : QuizGenUiState
    data class Error(val errorMessage: String) : QuizGenUiState
}

data class ChatMessage(
    val sender: String, // "user" or "ai"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

class AiLearningViewModel : ViewModel() {

    // General text/Q&A chat model initialized lazily
    private val generalModel by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    // Structured JSON Quiz generator model
    private val quizSchema = Schema.obj(
        properties = mapOf(
            "questions" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "question" to Schema.string(description = "Kalimat pertanyaan soal"),
                        "optionA" to Schema.string(description = "Pilihan jawaban A"),
                        "optionB" to Schema.string(description = "Pilihan jawaban B"),
                        "optionC" to Schema.string(description = "Pilihan jawaban C"),
                        "optionD" to Schema.string(description = "Pilihan jawaban D"),
                        "correctAnswer" to Schema.string(description = "Kunci jawaban yang benar: A, B, C, atau D"),
                        "explanation" to Schema.string(description = "Pembahasan ringkas mengapa jawaban tersebut tepat")
                    )
                ),
                description = "Daftar butir soal kuis pilihan ganda"
            )
        )
    )

    private val quizModel by lazy {
        val config = generationConfig {
            responseMimeType = "application/json"
            responseSchema = quizSchema
        }
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = config
        )
    }

    private val _chatUiState = MutableStateFlow<AiChatUiState>(AiChatUiState.Idle)
    val chatUiState: StateFlow<AiChatUiState> = _chatUiState.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "ai",
                message = "Halo! Saya Asisten Belajar Pintar SMKN 3 Tana Toraja didukung Gemini AI. Ada materi kejuruan atau pelajaran yang ingin ditanyakan atau diringkas?"
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _quizGenUiState = MutableStateFlow<QuizGenUiState>(QuizGenUiState.Idle)
    val quizGenUiState: StateFlow<QuizGenUiState> = _quizGenUiState.asStateFlow()

    fun sendMessage(userText: String, topicContext: String = "") {
        if (userText.isBlank()) return

        val userMsg = ChatMessage(sender = "user", message = userText)
        _messages.value = _messages.value + userMsg

        viewModelScope.launch {
            _chatUiState.value = AiChatUiState.Loading
            val accumulated = StringBuilder()

            try {
                val systemPrompt = """
                    Anda adalah 'Guru Virtual & Asisten Pembelajaran Pintar SMKN 3 Tana Toraja'.
                    Konteks sekolah: SMKN 3 Tana Toraja (Sulawesi Selatan), berfokus pada pendidikan vokasi/SMK kejuruan (Teknik Mesin, Otomotif, Komputer/Informatika, Listrik, dsb.) serta mata pelajaran umum.
                    Tugas Anda:
                    1. Menjelaskan materi pembelajaran secara runtut, pedagogis, ramah, dan memotivasi siswa.
                    2. Memberikan penjelasan langkah-demi-langkah jika ada pertanyaan hitungan atau praktikum teknis.
                    3. Berbahasa Indonesia yang baik, lugas, edukatif, dan santun.
                    ${if (topicContext.isNotBlank()) "Konteks modul/topik saat ini: $topicContext" else ""}
                    
                    Pertanyaan Pengguna:
                    $userText
                """.trimIndent()

                withTimeout(60_000L) {
                    generalModel.generateContentStream(systemPrompt).collect { chunk ->
                        val text = chunk.text.orEmpty()
                        accumulated.append(text)
                        _chatUiState.value = AiChatUiState.Streaming(accumulated.toString())
                    }
                }

                val finalResult = accumulated.toString()
                if (finalResult.isNotBlank()) {
                    _chatUiState.value = AiChatUiState.Success(finalResult)
                    _messages.value = _messages.value + ChatMessage(sender = "ai", message = finalResult)
                } else {
                    _chatUiState.value = AiChatUiState.Error("Model tidak mengembalikan respon.")
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Chat request timed out", e)
                val err = "Permintaan melebihi batas waktu (60 detik). Silakan periksa jaringan dan coba lagi."
                _chatUiState.value = AiChatUiState.Error(err)
                _messages.value = _messages.value + ChatMessage(sender = "ai", message = "Maaf, $err")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Chat error", e)
                val err = e.localizedMessage ?: "Terjadi kesalahan saat memproses permintaan AI."
                _chatUiState.value = AiChatUiState.Error(err)
                _messages.value = _messages.value + ChatMessage(sender = "ai", message = "Maaf, gagal memproses: $err")
            }
        }
    }

    fun generateQuizQuestions(topic: String, count: Int = 3) {
        if (topic.isBlank()) return

        viewModelScope.launch {
            _quizGenUiState.value = QuizGenUiState.Loading
            try {
                val prompt = """
                    Buat $count butir soal kuis pilihan ganda (4 opsi: A, B, C, D) seputar topik pembelajaran kejuruan/sekolah SMK: '$topic'.
                    Sertakan kunci jawaban yang tepat dan pembahasan ringkas.
                """.trimIndent()

                val response = withTimeout(60_000L) {
                    quizModel.generateContent(prompt)
                }

                val jsonText = response.text ?: throw IllegalStateException("Respon kosong dari AI.")
                val root = JSONObject(jsonText)
                val array = root.getJSONArray("questions")
                val resultList = mutableListOf<GeneratedQuizItem>()

                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    resultList.add(
                        GeneratedQuizItem(
                            question = obj.optString("question", ""),
                            optionA = obj.optString("optionA", ""),
                            optionB = obj.optString("optionB", ""),
                            optionC = obj.optString("optionC", ""),
                            optionD = obj.optString("optionD", ""),
                            correctAnswer = obj.optString("correctAnswer", "A").trim().uppercase(),
                            explanation = obj.optString("explanation", "")
                        )
                    )
                }

                _quizGenUiState.value = QuizGenUiState.Success(resultList)
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Quiz generation timed out", e)
                _quizGenUiState.value = QuizGenUiState.Error("Pembuatan kuis melebihi batas waktu (60 detik).")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Quiz generation error", e)
                _quizGenUiState.value = QuizGenUiState.Error(e.localizedMessage ?: "Gagal membuat kuis.")
            }
        }
    }

    fun resetQuizState() {
        _quizGenUiState.value = QuizGenUiState.Idle
    }
}
