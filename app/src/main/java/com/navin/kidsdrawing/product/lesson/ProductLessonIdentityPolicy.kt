package com.navin.kidsdrawing.product.lesson

import com.navin.kidsdrawing.lesson.content.LessonCatalogIdentity
import com.navin.kidsdrawing.lesson.lab.LessonRuntimeIdentity

/** Stable infrastructure identity policy for product lesson sessions. */
object ProductLessonIdentityPolicy {
    const val LEGACY_CUTE_CAT_SESSION_ID = "lesson-lab-cute-cat-session"
    const val LEGACY_CUTE_CAT_DOCUMENT_ID = "lesson-lab-cute-cat-document"

    fun forLesson(identity: LessonCatalogIdentity): LessonRuntimeIdentity {
        if (identity.lessonId == LEGACY_CUTE_CAT_ID && identity.revision == LEGACY_CUTE_CAT_REVISION) {
            return LessonRuntimeIdentity(
                sessionId = LEGACY_CUTE_CAT_SESSION_ID,
                documentId = LEGACY_CUTE_CAT_DOCUMENT_ID,
            )
        }
        val encodedLessonId = encodeLessonId(identity.lessonId)
        val prefix = "lesson-$encodedLessonId-r${identity.revision}"
        return LessonRuntimeIdentity(
            sessionId = "$prefix-session",
            documentId = "$prefix-document",
        )
    }

    private fun encodeLessonId(lessonId: String): String = buildString {
        lessonId.forEach { character ->
            when (character) {
                in 'a'..'z', in '0'..'9' -> append(character)
                else -> {
                    append('_')
                    append(character.code.toString(16).padStart(2, '0'))
                }
            }
        }
    }

    private const val LEGACY_CUTE_CAT_ID = "cute-cat"
    private const val LEGACY_CUTE_CAT_REVISION = 1
}
