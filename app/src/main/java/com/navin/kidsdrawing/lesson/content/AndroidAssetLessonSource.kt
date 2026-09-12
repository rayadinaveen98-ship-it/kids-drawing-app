package com.navin.kidsdrawing.lesson.content

import android.content.res.AssetManager
import java.io.IOException

class AndroidAssetLessonSource(
    private val assetManager: AssetManager,
) : LessonPackageSource {
    override fun readText(path: String): String? = try {
        assetManager.open(path).bufferedReader().use { it.readText() }
    } catch (_: IOException) {
        null
    }
}
