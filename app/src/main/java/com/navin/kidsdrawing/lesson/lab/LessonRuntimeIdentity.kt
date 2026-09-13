package com.navin.kidsdrawing.lesson.lab

/**
 * Immutable storage/runtime identity for one lesson execution chain.
 *
 * Session/document identifiers are infrastructure keys only; lesson semantics continue to come
 * from the loaded package. Keeping this object explicit lets product routing scale to multiple
 * lessons without changing the verified Lesson Engine state machine.
 */
data class LessonRuntimeIdentity(
    val sessionId: String,
    val documentId: String,
) {
    init {
        require(sessionId.isNotBlank()) { "sessionId cannot be blank." }
        require(documentId.isNotBlank()) { "documentId cannot be blank." }
    }
}
