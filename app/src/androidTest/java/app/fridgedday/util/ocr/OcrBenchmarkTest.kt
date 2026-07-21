package app.fridgedday.util.ocr

import android.graphics.BitmapFactory
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.services.storage.TestStorage
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class OcrBenchmarkTest {

    @Test
    fun evaluatePrivateLabelPhotosBySampleId() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val assets = instrumentation.context.assets
        val manifest = assets.open(MANIFEST_FILE).bufferedReader().use { it.readLines() }

        assertTrue("$MANIFEST_FILE must contain a header and at least one sample", manifest.size >= 2)
        assertTrue("Unexpected manifest header", manifest.first().removePrefix("\uFEFF") == MANIFEST_HEADER)

        val allSamples = manifest.drop(1).filter { it.isNotBlank() }.map(::parseSample)
        val sampleLimit = InstrumentationRegistry.getArguments().getString("sampleLimit")?.toIntOrNull() ?: 0
        val samples = if (sampleLimit > 0) allSamples.take(sampleLimit) else allSamples
        assertTrue("sample_id values must be unique", samples.map { it.sampleId }.distinct().size == samples.size)

        val output = mutableListOf(RESULT_HEADER)
        samples.forEach { sample ->
            val bitmap = assets.open(sample.imageFile).use { BitmapFactory.decodeStream(it) }
            assertNotNull("Cannot decode ${sample.imageFile} for ${sample.sampleId}", bitmap)

            val predicted = TextRecognitionHelper.extractExpiryDate(
                bitmap = bitmap!!,
                baseRotation = sample.baseRotation,
                today = sample.evaluationDate,
            )
            bitmap.recycle()

            val detected = predicted != null
            val exactMatch = predicted == sample.expectedDate
            val failureType = when {
                exactMatch -> ""
                !detected -> "no_candidate"
                else -> "wrong_date"
            }
            output += listOf(
                sample.sampleId,
                sample.expectedDate,
                predicted ?: "",
                exactMatch,
                detected,
                failureType,
                sample.lighting,
                sample.orientation,
                sample.material,
                sample.dateFormat,
                sample.evaluationDate,
                sample.baseRotation,
                appVersion(),
                csvSafe(Build.MODEL),
            ).joinToString(",")
        }

        TestStorage().openOutputFile(RESULT_FILE).bufferedWriter().use {
            it.write(output.joinToString(separator = "\n", postfix = "\n"))
        }
    }

    private fun parseSample(line: String): Sample {
        val values = line.split(',').map(String::trim)
        require(values.size == 9) { "Manifest rows must have 9 comma-free fields: $line" }
        require(values[0].matches(Regex("[A-Za-z0-9_-]+"))) { "Invalid sample_id: ${values[0]}" }
        require(values[1].matches(Regex("[A-Za-z0-9_./-]+"))) { "Invalid image_file: ${values[1]}" }
        require(values[7].toIntOrNull() in setOf(0, 90, 180, 270)) { "Invalid base_rotation: ${values[7]}" }
        return Sample(
            sampleId = values[0],
            imageFile = values[1],
            expectedDate = LocalDate.parse(values[2]),
            lighting = values[3],
            orientation = values[4],
            material = values[5],
            dateFormat = values[6],
            baseRotation = values[7].toInt(),
            evaluationDate = LocalDate.parse(values[8]),
        )
    }

    @Suppress("DEPRECATION")
    private fun appVersion(): String {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
    }

    private fun csvSafe(value: String): String = value.replace(',', ' ').replace('\n', ' ')

    private data class Sample(
        val sampleId: String,
        val imageFile: String,
        val expectedDate: LocalDate,
        val lighting: String,
        val orientation: String,
        val material: String,
        val baseRotation: Int,
        val evaluationDate: LocalDate,
        val dateFormat: String,
    )

    companion object {
        private const val MANIFEST_FILE = "manifest.csv"
        private const val RESULT_FILE = "ocr-benchmark.csv"
        private const val MANIFEST_HEADER =
            "sample_id,image_file,expected_date,lighting,orientation,material,date_format,base_rotation,evaluation_date"
        private const val RESULT_HEADER =
            "sample_id,expected_date,predicted_date,exact_match,detected,failure_type,lighting,orientation,material,date_format,evaluation_date,base_rotation,app_version,device_model"
    }
}
