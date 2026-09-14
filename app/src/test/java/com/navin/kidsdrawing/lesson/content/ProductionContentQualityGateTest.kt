package com.navin.kidsdrawing.lesson.content

import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductionContentQualityGateTest {
    @Test
    fun bundledReleaseCatalogPassesAndEmitsDeterministicReports() {
        val snapshot = LessonCatalog(FileAssetCatalogSource(File("src/main/assets"))).load()
        val report = ContentQualityAnalyzer().analyze(snapshot)
        assertTrue("Production catalog diagnostics: ${snapshot.diagnostics}", snapshot.diagnostics.isEmpty())
        assertEquals("Content quality errors: ${report.diagnostics}", 0, report.errorCount)
        val warnings = report.diagnostics.filter { it.severity == ContentQualitySeverity.WARNING }
        val reviewed = setOf("rainbow-weather", "tree-through-seasons", "ice-cream-shop", "simple-car", "sailboat-scene")
        assertEquals("Unexpected content quality warnings: $warnings", 5, warnings.size)
        assertEquals(reviewed, warnings.mapNotNull { it.lessonId }.toSet())
        assertTrue("Only reviewed standalone-lesson warnings are allowed: $warnings", warnings.all { it.code == ContentQualityDiagnosticCode.NO_JOURNEY_MEMBERSHIP })
        assertEquals(18, report.lessonCount)
        val text = report.renderText()
        val json = report.renderJson()
        val parsed = Json.parseToJsonElement(json).jsonObject
        assertEquals(18, parsed.getValue("lessonCount").jsonPrimitive.content.toInt())
        assertEquals(0, parsed.getValue("errorCount").jsonPrimitive.content.toInt())
        assertEquals(5, parsed.getValue("warningCount").jsonPrimitive.content.toInt())
        val outputDir = File("build/reports/content-quality").apply { mkdirs() }
        val textFile = File(outputDir, "catalog-report.txt")
        val jsonFile = File(outputDir, "catalog-report.json")
        textFile.writeText(text + "\n")
        jsonFile.writeText(json + "\n")
        assertTrue(textFile.isFile && textFile.length() > 0L)
        assertTrue(jsonFile.isFile && jsonFile.length() > 0L)
        println(text)
    }
    private class FileAssetCatalogSource(private val root: File) : LessonCatalogSource {
        override fun readText(path: String): String? = File(root, path).takeIf(File::isFile)?.readText()
        override fun list(path: String): List<String>? = File(root, path).list()?.toList()
        override fun exists(path: String): Boolean = File(root, path).isFile
    }
}
