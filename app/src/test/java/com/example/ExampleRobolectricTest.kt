package com.example

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.data.util.ExcelParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SMKN 3 TANA TORAJA", appName)
  }

  @Test
  fun `test sample template parsing and leading zero preservation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val sampleCsv = ExcelParser.generateSampleTemplate()
    val tempFile = File(context.cacheDir, "sample_test.csv")
    FileOutputStream(tempFile).use { it.write(sampleCsv.toByteArray()) }

    val uri = Uri.fromFile(tempFile)
    val result = ExcelParser.parseFile(context, uri, "sample_test.csv", emptySet())
    assertEquals(5, result.totalFound)
    assertEquals(5, result.validCount)
    assertEquals(0, result.duplicateCount)

    // Check first student NIS preserves leading zeros
    val firstStudent = result.rows.first()
    assertEquals("002101", firstStudent.nis)
    assertEquals("Ahmad Fauzi", firstStudent.fullName)
    assertEquals("L", firstStudent.gender)
    assertTrue(firstStudent.isValid)
  }
}
