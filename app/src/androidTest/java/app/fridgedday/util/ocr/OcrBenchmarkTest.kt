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
import java.security.MessageDigest
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class OcrBenchmarkTest {

    @Test
    fun evaluatePrivateLabelPhotosBySampleId() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val assets = instrumentation.context.assets
        val manifest = assets.open(MANIFEST_FILE).bufferedReader().use { it.readLines() }

        assertTrue("$MANIFEST_FILE must contain a header and at least one sample", manifest.size >= 2)
        val header = manifest.first().removePrefix("\uFEFF").split(',').map(String::trim)
        assertTrue("Manifest is missing required columns", header.containsAll(REQUIRED_MANIFEST_COLUMNS))

        val allSamples = manifest.drop(1).filter { it.isNotBlank() }.map { parseSample(header, it) }
        val sampleLimit = InstrumentationRegistry.getArguments().getString("sampleLimit")?.toIntOrNull() ?: 0
        val evaluationOffsetArgument = InstrumentationRegistry.getArguments().getString("evaluationOffsetDays")
        val evaluationOffsetDays = evaluationOffsetArgument?.let { rawValue ->
            require(rawValue.matches(Regex("[0-9]+"))) {
                "evaluationOffsetDays must be a positive integer: $rawValue"
            }
            rawValue.toLong()
        }
        require(evaluationOffsetDays == null || evaluationOffsetDays in 1..MAX_EVALUATION_OFFSET_DAYS) {
            "evaluationOffsetDays must be between 1 and $MAX_EVALUATION_OFFSET_DAYS"
        }
        val samples = if (sampleLimit > 0) allSamples.take(sampleLimit) else allSamples
        assertTrue("sample_id values must be unique", samples.map { it.sampleId }.distinct().size == samples.size)

        val output = mutableListOf(RESULT_HEADER)
        samples.forEach { sample ->
            val imageBytes = assets.open(sample.imageFile).use { it.readBytes() }
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            assertNotNull("Cannot decode ${sample.imageFile} for ${sample.sampleId}", bitmap)
            val evaluationDate = evaluationOffsetDays
                ?.let { sample.expectedDate.minusDays(it) }
                ?: sample.evaluationDate
            val evaluationScenario = evaluationOffsetDays
                ?.let { "expected_minus_${it}d" }
                ?: "manifest"

            val evaluation = TextRecognitionHelper.evaluateExpiryDate(
                bitmap = bitmap!!,
                baseRotation = sample.baseRotation,
                today = evaluationDate,
            )
            bitmap.recycle()

            val predicted = evaluation.selectedDate
            val detected = evaluation.candidateCount > 0
            val expectedCandidateDetected = sample.expectedDate in evaluation.candidateDates
            val exactMatch = predicted == sample.expectedDate
            val failureType = when {
                exactMatch -> ""
                evaluation.processedVariantCount == 0 -> "ocr_error"
                !evaluation.hasRecognizedText -> "no_text"
                evaluation.candidateCount == 0 -> "no_date_candidate"
                predicted == null -> "candidate_out_of_window"
                else -> "wrong_date"
            }
            output += listOf(
                sample.sampleId,
                sha256(imageBytes),
                sample.expectedDate,
                predicted ?: "",
                exactMatch,
                detected,
                expectedCandidateDetected,
                evaluation.candidateCount,
                evaluation.failedVariantCount,
                failureType,
                sample.lighting,
                sample.orientation,
                sample.material,
                sample.dateFormat,
                sample.printQuality,
                sample.independenceKey,
                sample.cohort,
                evaluationDate,
                evaluationScenario,
                sample.baseRotation,
                appVersion(),
                csvSafe(Build.MODEL),
            ).joinToString(",")
        }

        TestStorage().openOutputFile(RESULT_FILE).bufferedWriter().use {
            it.write(output.joinToString(separator = "\n", postfix = "\n"))
        }
    }

    private fun parseSample(header: List<String>, line: String): Sample {
        val values = line.split(',').map(String::trim)
        require(values.size == header.size) { "Manifest row has ${values.size} fields but header has ${header.size}: $line" }
        val row = header.zip(values).toMap()
        val sampleId = row.getValue("sample_id")
        val imageFile = row.getValue("image_file")
        val baseRotation = row.getValue("base_rotation")
        require(sampleId.matches(Regex("[A-Za-z0-9_-]+"))) { "Invalid sample_id: $sampleId" }
        require(imageFile.matches(Regex("[A-Za-z0-9_./-]+"))) { "Invalid image_file: $imageFile" }
        require(baseRotation.toIntOrNull() in setOf(0, 90, 180, 270)) { "Invalid base_rotation: $baseRotation" }
        return Sample(
            sampleId = sampleId,
            imageFile = imageFile,
            expectedDate = LocalDate.parse(row.getValue("expected_date")),
            lighting = row.getValue("lighting"),
            orientation = row.getValue("orientation"),
            material = row.getValue("material"),
            dateFormat = row.getValue("date_format"),
            printQuality = row["print_quality"].orEmpty(),
            independenceKey = row["independence_key"].orEmpty(),
            cohort = row["cohort"].orEmpty(),
            baseRotation = baseRotation.toInt(),
            evaluationDate = LocalDate.parse(row.getValue("evaluation_date")),
        )
    }

    private fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
        .digest(bytes)
        .joinToString("") { "%02x".format(it) }

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
        val printQuality: String,
        val independenceKey: String,
        val cohort: String,
    )

    companion object {
        private const val MANIFEST_FILE = "manifest.csv"
        private const val RESULT_FILE = "ocr-benchmark.csv"
        private const val MAX_EVALUATION_OFFSET_DAYS = 1825L
        private val REQUIRED_MANIFEST_COLUMNS = listOf(
            "sample_id",
            "image_file",
            "expected_date",
            "lighting",
            "orientation",
            "material",
            "date_format",
            "base_rotation",
            "evaluation_date",
        )
        private const val RESULT_HEADER =
            "sample_id,image_sha256,expected_date,predicted_date,exact_match,detected,expected_candidate_detected,candidate_count,failed_variant_count,failure_type,lighting,orientation,material,date_format,print_quality,independence_key,cohort,evaluation_date,evaluation_scenario,base_rotation,app_version,device_model"
    }
}
