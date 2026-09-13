package com.navin.kidsdrawing.lesson.content

import android.content.res.AssetManager
import java.io.IOException

class AndroidAssetLessonSource(
    private val assetManager: AssetManager,
) : LessonCatalogSource {
    override fun readText(path: String): String? = try {
        assetManager.open(path).bufferedReader().use { it.readText() }
    } catch (_: IOException) {
        null
    }

    override fun list(path: String): List<String>? = try {
        assetManager.list(path)?.toList()
    } catch (_: IOException) {
        null
    }

    override fun exists(path: String): Boolean = try {
        assetManager.open(path).use { }
        true
    } catch (_: IOException) {
        false
    }
}
