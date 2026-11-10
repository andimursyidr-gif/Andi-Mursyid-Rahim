package com.example.data.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.data.model.AttendanceEntity
import com.example.data.model.ClassEntity
import com.example.data.model.StudentEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    fun generateStudentListPdf(
        context: Context,
        classInfo: ClassEntity?,
        students: List<StudentEntity>
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint()
        val titlePaint = Paint().apply {
            color = Color.rgb(30, 58, 138) // Navy
            textSize = 18f
            isFakeBoldText = true
        }
        val subPaint = Paint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 11f
        }
        val headerPaint = Paint().apply {
            color = Color.rgb(30, 58, 138)
            textSize = 11f
            isFakeBoldText = true
        }
        val cellPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
        }
        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        // Header
        canvas.drawText("SMKN 3 TANA TORAJA - DAFTAR SISWA KELAS", 40f, 50f, titlePaint)
        val cName = classInfo?.className ?: "Semua Kelas"
        val yr = classInfo?.academicYear ?: "2024/2025"
        val sem = classInfo?.semester ?: "Ganjil"
        canvas.drawText("Kelas: $cName | Tahun Ajaran: $yr | Semester: $sem", 40f, 70f, subPaint)
        val dateStr = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date())
        canvas.drawText("Tanggal Cetak: $dateStr | Total Siswa: ${students.size}", 40f, 85f, subPaint)

        canvas.drawLine(40f, 100f, 555f, 100f, linePaint)

        // Table Header
        var y = 125f
        canvas.drawText("No", 45f, y, headerPaint)
        canvas.drawText("NIS", 75f, y, headerPaint)
        canvas.drawText("NISN", 130f, y, headerPaint)
        canvas.drawText("Nama Lengkap", 205f, y, headerPaint)
        canvas.drawText("L/P", 380f, y, headerPaint)
        canvas.drawText("No HP Ortu", 430f, y, headerPaint)

        canvas.drawLine(40f, y + 8f, 555f, y + 8f, linePaint)
        y += 24f

        students.forEachIndexed { index, s ->
            if (y > 780f) return@forEachIndexed // safety single-page margin
            canvas.drawText("${index + 1}", 45f, y, cellPaint)
            canvas.drawText(s.nis, 75f, y, cellPaint)
            canvas.drawText(s.nisn.ifBlank { "-" }, 130f, y, cellPaint)
            val nameDisplay = if (s.fullName.length > 25) s.fullName.substring(0, 23) + "..." else s.fullName
            canvas.drawText(nameDisplay, 205f, y, cellPaint)
            canvas.drawText(s.gender, 385f, y, cellPaint)
            canvas.drawText(s.parentPhone.ifBlank { "-" }, 430f, y, cellPaint)
            y += 20f
        }

        // Footer signature
        y = maxOf(y + 30f, 720f)
        canvas.drawText("Wali Kelas / Guru Pengajar,", 380f, y, cellPaint)
        canvas.drawText(classInfo?.teacherName ?: "Drs. H. Mursyid, M.Pd.", 380f, y + 50f, headerPaint)

        document.finishPage(page)

        val outputDir = context.getExternalFilesDir(null) ?: context.filesDir
        val file = File(outputDir, "Daftar_Siswa_${cName.replace(" ", "_")}_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
        return file
    }

    fun generateAcademicReportPdf(
        context: Context,
        classInfo: ClassEntity?,
        studentCount: Int,
        quizAverage: Double,
        worksheetAverage: Double,
        completionRate: Int,
        attendanceList: List<AttendanceEntity>
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(30, 58, 138)
            textSize = 18f
            isFakeBoldText = true
        }
        val subPaint = Paint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 11f
        }
        val sectionPaint = Paint().apply {
            color = Color.rgb(217, 119, 6) // Gold / Amber
            textSize = 13f
            isFakeBoldText = true
        }
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
        }

        canvas.drawText("LAPORAN REKAPITULASI HASIL BELAJAR", 40f, 50f, titlePaint)
        val cName = classInfo?.className ?: "Semua Kelas"
        canvas.drawText("Kelas: $cName | Semester: ${classInfo?.semester ?: "Ganjil"} | TA: ${classInfo?.academicYear ?: "2024/2025"}", 40f, 72f, subPaint)

        val dateStr = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("id", "ID")).format(Date())
        canvas.drawText("Diterbitkan: $dateStr", 40f, 90f, subPaint)

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }
        canvas.drawLine(40f, 105f, 555f, 105f, linePaint)

        // Summary Section
        var y = 135f
        canvas.drawText("1. RINGKASAN AKADEMIK KELAS", 40f, y, sectionPaint)
        y += 24f
        canvas.drawText("• Total Peserta Didik: $studentCount Siswa", 50f, y, textPaint)
        y += 20f
        canvas.drawText("• Rata-rata Nilai Kuis Interaktif: ${String.format(Locale.US, "%.1f", quizAverage)} / 100", 50f, y, textPaint)
        y += 20f
        canvas.drawText("• Rata-rata Nilai LKPD: ${String.format(Locale.US, "%.1f", worksheetAverage)} / 100", 50f, y, textPaint)
        y += 20f
        canvas.drawText("• Persentase Ketuntasan Belajar: $completionRate%", 50f, y, textPaint)

        // Attendance Section
        y += 35f
        canvas.drawText("2. REKAPITULASI KEHADIRAN SISWA", 40f, y, sectionPaint)
        y += 24f
        val hadir = attendanceList.count { it.status == "HADIR" }
        val sakit = attendanceList.count { it.status == "SAKIT" }
        val izin = attendanceList.count { it.status == "IZIN" }
        val alfa = attendanceList.count { it.status == "ALFA" }
        val totalAtt = maxOf(1, hadir + sakit + izin + alfa)
        val attRate = (hadir * 100) / totalAtt

        canvas.drawText("• Tingkat Kehadiran: $attRate% ($hadir Hadir dari $totalAtt sesi)", 50f, y, textPaint)
        y += 20f
        canvas.drawText("• Sakit: $sakit  |  Izin: $izin  |  Tanpa Keterangan (Alfa): $alfa", 50f, y, textPaint)

        y += 40f
        canvas.drawText("3. EVALUASI DAN REKOMENDASI PEMBELAJARAN", 40f, y, sectionPaint)
        y += 24f
        val evalText = if (quizAverage >= 75.0) {
            "Secara umum target pencapaian pembelajaran telah tercapai dengan baik (Tuntas). Pertahankan motivasi belajar dan berikan pengayaan topik lanjutan."
        } else {
            "Diperlukan pendampingan dan remedial interaktif untuk topik-topik materi dengan pencapaian di bawah KKM."
        }
        canvas.drawText(evalText, 50f, y, textPaint)

        // Signature
        y = 680f
        canvas.drawText("Mengetahui,", 380f, y, textPaint)
        canvas.drawText("Kepala Sekolah / Pengawas,", 380f, y + 16f, textPaint)
        canvas.drawText(classInfo?.teacherName ?: "Drs. H. Mursyid, M.Pd.", 380f, y + 65f, sectionPaint)
        canvas.drawText("NIP: 19750812 200212 1 004", 380f, y + 80f, subPaint)

        document.finishPage(page)

        val outputDir = context.getExternalFilesDir(null) ?: context.filesDir
        val file = File(outputDir, "Laporan_Hasil_Belajar_${cName.replace(" ", "_")}.pdf")
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
        return file
    }
}
