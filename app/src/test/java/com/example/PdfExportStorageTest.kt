package com.example

import android.content.Context
import android.os.Environment
import androidx.test.core.app.ApplicationProvider
import com.example.ui.screens.academics.saveSchoolPdf
import java.io.File
import java.io.IOException
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowEnvironment

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class PdfExportStorageTest {
  @Before
  fun mountStorage() {
    ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED)
  }

  @Test
  fun `older Android saves PDF inside app downloads and sanitizes the name`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val bytes = "%PDF-1.4 demo".toByteArray()
    val uri = saveSchoolPdf(context, "../../report.pdf") { it.write(bytes) }
    assertNotNull(uri)
    val saved = File(requireNotNull(uri!!.path))
    val allowed = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "Revenex")
    assertEquals(allowed.canonicalPath, saved.parentFile!!.canonicalPath)
    assertArrayEquals(bytes, saved.readBytes())
    assertTrue(saved.name.endsWith(".pdf"))
  }

  @Test
  fun `failed PDF writer does not leave a partial export`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val directory = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "Revenex")
    val before = directory.listFiles()?.map { it.name }?.toSet().orEmpty()
    try {
      saveSchoolPdf(context, "failure.pdf") {
        it.write(byteArrayOf(1, 2))
        throw IOException("Writer failed")
      }
      fail("Failed exports must not report success")
    } catch (expected: IOException) {
      assertEquals("Writer failed", expected.message)
    }
    assertEquals(before, directory.listFiles()?.map { it.name }?.toSet().orEmpty())
  }
}
