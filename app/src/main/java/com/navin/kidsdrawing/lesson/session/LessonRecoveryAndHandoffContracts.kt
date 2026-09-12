package com.navin.kidsdrawing.lesson.session

/** Retry the deterministic recovery action exposed by a RecoverableError state. */
data object RetryRecoverable : LessonCommand

/** Post-drawing choices owned by Lesson Engine. */
data object ChooseColorWithMe : LessonCommand

data object ChooseColorMyself : LessonCommand

data object FinishForNow : LessonCommand

enum class ColoringHandoffMode {
    COLOR_WITH_ME,
    COLOR_MYSELF,
}

/** Runtime acknowledgement from the Coloring Engine boundary. */
data class ColoringHandoffCompleted(
    val childDocumentId: String,
) : LessonRuntimeSignal

/** Runtime failure from the Coloring Engine boundary; artwork remains owned by Drawing Engine. */
data class ColoringHandoffFailed(
    val childDocumentId: String,
    val reason: String,
) : LessonRuntimeSignal

data class ColoringHandoffRequested(
    val mode: ColoringHandoffMode,
    val childDocumentId: String,
) : LessonSessionEvent

data class ColoringHandoffFailureObserved(
    val reason: String,
) : LessonSessionEvent

data class LessonFinished(
    val reason: LessonFinishReason,
) : LessonSessionEvent
