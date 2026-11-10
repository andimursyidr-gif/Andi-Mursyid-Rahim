package com.example.data.util

import android.content.Context
import android.net.Uri
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

data class ParsedStudentRow(
    val rowNumber: Int,
    val nis: String,
    val nisn: String,
    val fullName: String,
    val gender: String, // "L" or "P"
    val birthPlace: String,
    val birthDate: String,
    val className: String,
    val address: String,
    val parentPhone: String,
    val isValid: Boolean,
    val isDuplicate: Boolean,
    val validationErrors: List<String>
)

data class ImportResult(
    val fileName: String,
    val totalFound: Int,
    val validCount: Int,
    val duplicateCount: Int,
    val invalidCount: Int,
    val rows: List<ParsedStudentRow>,
    val detectedColumns: Map<String, Int>
)

object ExcelParser {

    fun parseFile(
        context: Context,
        uri: Uri,
        fileName: String,
        existingNisList: Set<String>
    ): ImportResult {
        val inputStream = try {
            if (uri.scheme == "file") {
                val path = uri.path
                if (path != null && java.io.File(path).exists()) {
                    java.io.FileInputStream(java.io.File(path))
                } else {
                    context.contentResolver.openInputStream(uri)
                }
            } else {
                context.contentResolver.openInputStream(uri)
            }
        } catch (_: Exception) {
            try {
                val path = uri.path
                if (path != null && java.io.File(path).exists()) {
                    java.io.FileInputStream(java.io.File(path))
                } else null
            } catch (_: Exception) {
                null
            }
        } ?: return ImportResult(fileName, 0, 0, 0, 0, emptyList(), emptyMap())

        return try {
            val lower = fileName.lowercase()
            val rawTable: List<List<String>> = if (lower.endsWith(".xlsx")) {
                parseXlsx(inputStream)
            } else {
                parseCsvOrDelimited(inputStream)
            }
            processRawTable(fileName, rawTable, existingNisList)
        } catch (e: Exception) {
            e.printStackTrace()
            ImportResult(fileName, 0, 0, 0, 0, emptyList(), emptyMap())
        } finally {
            try { inputStream.close() } catch (_: Exception) {}
        }
    }

    /**
     * Parses XLSX using pure Android XML Pull Parser on the internal zip entries
     */
    private fun parseXlsx(inputStream: InputStream): List<List<String>> {
        val zip = ZipInputStream(inputStream)
        var entry: ZipEntry?
        val sharedStrings = mutableListOf<String>()
        var sheetBytes: ByteArray? = null

        while (zip.nextEntry.also { entry = it } != null) {
            val name = entry?.name ?: ""
            if (name.equals("xl/sharedStrings.xml", ignoreCase = true)) {
                sharedStrings.addAll(parseSharedStrings(zip))
            } else if (name.contains("xl/worksheets/sheet1.xml", ignoreCase = true) ||
                (name.startsWith("xl/worksheets/sheet") && name.endsWith(".xml") && sheetBytes == null)
            ) {
                sheetBytes = zip.readBytes()
            }
            zip.closeEntry()
        }

        if (sheetBytes != null) {
            return parseSheetXml(sheetBytes.inputStream(), sharedStrings)
        }
        return emptyList()
    }

    private fun parseSharedStrings(stream: InputStream): List<String> {
        val list = mutableListOf<String>()
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(stream, "UTF-8")

        var eventType = parser.eventType
        var currentText = StringBuilder()
        var insideT = false

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name.equals("t", ignoreCase = true)) {
                        insideT = true
                    } else if (parser.name.equals("si", ignoreCase = true)) {
                        currentText = StringBuilder()
                    }
                }
                XmlPullParser.TEXT -> {
                    if (insideT) {
                        currentText.append(parser.text)
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name.equals("t", ignoreCase = true)) {
                        insideT = false
                    } else if (parser.name.equals("si", ignoreCase = true)) {
                        list.add(currentText.toString())
                    }
                }
            }
            eventType = parser.next()
        }
        return list
    }

    private fun parseSheetXml(stream: InputStream, sharedStrings: List<String>): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(stream, "UTF-8")

        var eventType = parser.eventType
        var currentRow = mutableListOf<String>()
        var cellType: String? = null
        var cellValue = StringBuilder()
        var insideValue = false

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "row" -> {
                            currentRow = mutableListOf()
                        }
                        "c" -> {
                            cellType = parser.getAttributeValue(null, "t")
                            cellValue = StringBuilder()
                        }
                        "v", "t" -> {
                            insideValue = true
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    if (insideValue) {
                        cellValue.append(parser.text)
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "v", "t" -> {
                            insideValue = false
                        }
                        "c" -> {
                            val raw = cellValue.toString().trim()
                            val parsedValue = if (cellType == "s") {
                                val idx = raw.toIntOrNull()
                                if (idx != null && idx in sharedStrings.indices) {
                                    sharedStrings[idx]
                                } else raw
                            } else {
                                raw
                            }
                            currentRow.add(parsedValue)
                        }
                        "row" -> {
                            if (currentRow.isNotEmpty() && currentRow.any { it.isNotBlank() }) {
                                rows.add(currentRow)
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        return rows
    }

    private fun parseCsvOrDelimited(inputStream: InputStream): List<List<String>> {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val rows = mutableListOf<List<String>>()
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            val l = line?.trim() ?: continue
            if (l.isEmpty()) continue

            // Determine delimiter: comma, semicolon, or tab
            val delimiter = when {
                l.contains(";") -> ';'
                l.contains("\t") -> '\t'
                else -> ','
            }

            val tokens = splitCsvLine(l, delimiter)
            if (tokens.any { it.isNotBlank() }) {
                rows.add(tokens)
            }
        }
        return rows
    }

    private fun splitCsvLine(line: String, delimiter: Char): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false

        for (ch in line) {
            if (ch == '\"') {
                inQuotes = !inQuotes
            } else if (ch == delimiter && !inQuotes) {
                result.add(sb.toString().trim().removeSurrounding("\""))
                sb.clear()
            } else {
                sb.append(ch)
            }
        }
        result.add(sb.toString().trim().removeSurrounding("\""))
        return result
    }

    private fun processRawTable(
        fileName: String,
        table: List<List<String>>,
        existingNisList: Set<String>
    ): ImportResult {
        if (table.isEmpty()) {
            return ImportResult(fileName, 0, 0, 0, 0, emptyList(), emptyMap())
        }

        // Find header row (usually row 0 or 1)
        var headerIndex = 0
        for (i in 0 until minOf(3, table.size)) {
            val row = table[i].map { it.lowercase() }
            if (row.any { it.contains("nis") || it.contains("nama") || it.contains("siswa") }) {
                headerIndex = i
                break
            }
        }

        val headers = table[headerIndex].map { it.trim().lowercase() }
        val colMap = mutableMapOf<String, Int>()

        headers.forEachIndexed { idx, h ->
            when {
                h.contains("jk") || h.contains("kelamin") || h.contains("gender") || h.contains("l/p") -> colMap["gender"] = idx
                h.contains("nisn") -> colMap["nisn"] = idx
                (h == "nis" || h.startsWith("nis ") || h.endsWith(" nis") || h.contains("no induk") || h.contains("nomor induk") || (h.contains("nis") && !h.contains("jenis") && !h.contains("nisn"))) -> colMap["nis"] = idx
                h.contains("nama") -> colMap["name"] = idx
                h.contains("tempat") -> colMap["birth_place"] = idx
                h.contains("tanggal") || h.contains("tgl") -> colMap["birth_date"] = idx
                h.contains("kelas") -> colMap["class"] = idx
                h.contains("alamat") -> colMap["address"] = idx
                h.contains("hp") || h.contains("telepon") || h.contains("wa") || h.contains("ortu") -> colMap["phone"] = idx
            }
        }

        val rows = mutableListOf<ParsedStudentRow>()
        val seenNisInBatch = mutableSetOf<String>()

        for (i in (headerIndex + 1) until table.size) {
            val r = table[i]
            if (r.isEmpty() || r.all { it.isBlank() }) continue

            fun get(key: String, defaultIdx: Int): String {
                val idx = colMap[key] ?: defaultIdx
                return if (idx < r.size) r[idx].trim() else ""
            }

            // Fallback column positions if headers weren't named standard:
            // Col 0: No, Col 1: NIS, Col 2: NISN, Col 3: Nama, Col 4: JK, Col 5: Tempat, Col 6: Tgl, Col 7: Kelas, Col 8: Alamat, Col 9: HP
            val nis = get("nis", 1)
            val nisn = get("nisn", 2)
            val name = get("name", 3)
            val rawGender = get("gender", 4)
            val gender = if (rawGender.startsWith("p", ignoreCase = true) || rawGender.contains("perempuan", ignoreCase = true)) "P" else "L"
            val birthPlace = get("birth_place", 5)
            val birthDate = get("birth_date", 6)
            val className = get("class", 7)
            val address = get("address", 8)
            val phone = get("phone", 9)

            val errors = mutableListOf<String>()
            if (name.isBlank()) errors.add("Nama lengkap tidak boleh kosong")
            if (nis.isBlank()) errors.add("NIS tidak boleh kosong")

            val isDuplicate = (nis.isNotBlank() && existingNisList.contains(nis)) ||
                    (nis.isNotBlank() && seenNisInBatch.contains(nis))

            if (isDuplicate) {
                errors.add("NIS $nis sudah terdaftar atau ganda")
            }
            if (nis.isNotBlank()) {
                seenNisInBatch.add(nis)
            }

            val isValid = errors.isEmpty()
            rows.add(
                ParsedStudentRow(
                    rowNumber = i + 1,
                    nis = nis,
                    nisn = nisn,
                    fullName = name,
                    gender = gender,
                    birthPlace = birthPlace,
                    birthDate = birthDate,
                    className = className,
                    address = address,
                    parentPhone = phone,
                    isValid = isValid,
                    isDuplicate = isDuplicate,
                    validationErrors = errors
                )
            )
        }

        val validCount = rows.count { it.isValid }
        val duplicateCount = rows.count { it.isDuplicate }
        val invalidCount = rows.count { !it.isValid && !it.isDuplicate }

        return ImportResult(
            fileName = fileName,
            totalFound = rows.size,
            validCount = validCount,
            duplicateCount = duplicateCount,
            invalidCount = invalidCount,
            rows = rows,
            detectedColumns = colMap
        )
    }

    /**
     * Generates a sample CSV template with standard header and example rows
     */
    fun generateSampleTemplate(): String {
        return buildString {
            appendLine("No,NIS,NISN,Nama Lengkap,Jenis Kelamin,Tempat Lahir,Tanggal Lahir,Kelas,Alamat,Nomor HP Ortu")
            appendLine("1,002101,0081234567,Ahmad Fauzi,L,Jakarta,2008-05-12,X RPL 1,Jl. Merdeka No. 12,081234567890")
            appendLine("2,002102,0081234568,Dewi Rahmawati,P,Bandung,2008-08-20,X RPL 1,Jl. Mawar Indah No. 5,081298765432")
            appendLine("3,002103,0081234569,Budi Santoso,L,Surabaya,2008-02-17,X RPL 1,Jl. Diponegoro No. 88,081377889900")
            appendLine("4,002104,0081234570,Siti Nurhaliza,P,Semarang,2008-11-03,X RPL 1,Jl. Melati No. 45,081922334455")
            appendLine("5,002105,0081234571,Rizky Pratama,L,Makassar,2008-09-25,X RPL 1,Jl. Hasanuddin No. 10,081566778899")
        }
    }
}
