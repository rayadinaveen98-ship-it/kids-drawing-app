package com.navin.kidsdrawing.lesson.content

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductionLessonCapabilityGateTest {
    @Test
    fun everyBundledReleaseLessonIsCapabilityComplete() {
        val snapshot = LessonCatalog(FileAssetCatalogSource(File("src/main/assets"))).load()

        assertTrue("Catalog diagnostics: ${snapshot.diagnostics}", snapshot.diagnostics.isEmpty())
        assertEquals("Content V2.5 release catalog must contain exactly 36 lessons.", 36, snapshot.entries.size)

        val failures = snapshot.entries.flatMap { entry ->
            val packageData = snapshot.runtimePackage(entry.identity)
            assertNotNull("Missing runtime package for ${entry.identity}", packageData)
            LessonCapabilityValidator.validate(checkNotNull(packageData)).map { diagnostic ->
                "${entry.identity.lessonId} r${entry.identity.revision} · ${diagnostic.code} · ${diagnostic.path} · ${diagnostic.message}"
            }
        }

        assertTrue(
            buildString {
                appendLine("Release lesson capability failures must be fixed before library expansion:")
                failures.forEach(::appendLine)
            },
            failures.isEmpty(),
        )
    }

    private class FileAssetCatalogSource(private val root: File) : LessonCatalogSource {
        override fun readText(path: String): String? = File(root, path).takeIf(File::isFile)?.readText()
        override fun list(path: String): List<String>? = File(root, path).list()?.toList()
        override fun exists(path: String): Boolean = File(root, path).isFile
    }
}
