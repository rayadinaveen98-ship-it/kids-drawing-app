package com.navin.kidsdrawing.product.lesson

import android.content.Context
import com.navin.kidsdrawing.lesson.content.AndroidAssetLessonSource
import com.navin.kidsdrawing.lesson.content.LessonCatalog
import com.navin.kidsdrawing.lesson.content.LessonPackageLoader
import com.navin.kidsdrawing.lesson.model.LessonRuntimePackage
import kotlinx.serialization.decodeFromString

/**
 * Local-only resolver for validated lesson string assets.
 *
 * Missing/unreadable text never mutates lesson state and never invents replacement lesson content;
 * callers retain their existing generic product guidance when resolution returns null.
 */
class ProductLessonTextRepository(
    context: Context,
    private val packageData: LessonRuntimePackage?,
) {
    private val source = AndroidAssetLessonSource(context.applicationContext.assets)

    private val defaultStrings: Map<String, String> by lazy {
        loadDefaultStrings()
    }

    fun resolve(key: String?): String? = key
        ?.takeIf(String::isNotBlank)
        ?.let(defaultStrings::get)
        ?.trim()
        ?.takeIf(String::isNotBlank)

    private fun loadDefaultStrings(): Map<String, String> {
        val packageData = packageData ?: return emptyMap()
        val relativePath = packageData.lesson.assets.strings[LessonCatalog.DEFAULT_LOCALE]
            ?: return emptyMap()
        val path = "${packageData.packageRoot.trimEnd('/')}/${relativePath.trimStart('/')}"
        val text = source.readText(path) ?: return emptyMap()
        return runCatching {
            LessonPackageLoader.DEFAULT_JSON.decodeFromString<Map<String, String>>(text)
        }.getOrDefault(emptyMap())
    }
}
