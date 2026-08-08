package dev.local.androidtools.flaglens.internal

import dev.local.androidtools.flaglens.model.Flag
import dev.local.androidtools.flaglens.model.FlagReport
import dev.local.androidtools.flaglens.model.FlagValue
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FlagReportSerializerTest {

    private fun sampleReport() = FlagReport(
        appName = "My App",
        environment = "staging",
        generatedAtMs = 1_700_000_000_000L,
        context = emptyList(),
        flags = listOf(Flag(key = "new_checkout", actualValue = FlagValue.of(true), source = "firebase", updatedAtMs = 1L)),
    )

    @Test
    fun `markdown includes app name, environment, and grouped flags`() {
        val markdown = FlagReportSerializer.toMarkdown(sampleReport())

        assertTrue(markdown.contains("My App"))
        assertTrue(markdown.contains("staging"))
        assertTrue(markdown.contains("### firebase"))
        assertTrue(markdown.contains("new_checkout"))
    }

    @Test
    fun `json round-trips through kotlinx serialization`() {
        val original = sampleReport()
        val json = FlagReportSerializer.toJson(original)
        val decoded = Json.decodeFromString(FlagReport.serializer(), json)

        assertEquals(original, decoded)
    }
}
