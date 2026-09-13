package com.navin.kidsdrawing.coloring.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ColoringSessionMode {
    COLOR_WITH_ME,
    COLOR_MYSELF,
}

enum class ColoringSessionPhase {
    ACTIVE,
    FINISHED,
}

/** Append-only ordinal contract: BRUSH=0 and ERASER=1 stay compatible with schema-1 snapshots. */
enum class ColoringSessionTool {
    BRUSH,
    ERASER,
    FILL,
}

data class ColoringSessionState(
    val sessionId: String,
    val childDocumentId: String,
    val lessonId: String,
    val lessonRevision: Int,
    val mode: ColoringSessionMode,
    val phase: ColoringSessionPhase,
    val selectedTool: ColoringSessionTool,
    val selectedColorArgb: Int,
    val brushWidth: Float,
) {
    init {
        require(sessionId.isNotBlank()) { "Coloring session ID cannot be blank." }
        require(childDocumentId.isNotBlank()) { "Coloring child document ID cannot be blank." }
        require(lessonId.isNotBlank()) { "Coloring lesson ID cannot be blank." }
        require(lessonRevision > 0) { "Coloring lesson revision must be positive." }
        require(brushWidth.isFinite() && brushWidth in MIN_BRUSH_WIDTH..MAX_BRUSH_WIDTH) {
            "Coloring brush width must be within $MIN_BRUSH_WIDTH..$MAX_BRUSH_WIDTH."
        }
    }

    companion object {
        const val MIN_BRUSH_WIDTH = 6f
        const val MAX_BRUSH_WIDTH = 80f
        const val DEFAULT_BRUSH_WIDTH = 34f
        const val DEFAULT_COLOR_ARGB: Int = -0x2f789 // 0xFFFFD877
    }
}

data class ColoringSessionSnapshot(
    val schemaVersion: Int = CURRENT_COLORING_SESSION_SCHEMA_VERSION,
    val sessionId: String,
    val childDocumentId: String,
    val lessonId: String,
    val lessonRevision: Int,
    val mode: ColoringSessionMode,
    val phase: ColoringSessionPhase,
    val selectedTool: ColoringSessionTool,
    val selectedColorArgb: Int,
    val brushWidth: Float,
    val savedAtEpochMillis: Long,
) {
    init {
        require(schemaVersion == CURRENT_COLORING_SESSION_SCHEMA_VERSION) {
            "Unsupported coloring session schema: $schemaVersion"
        }
        require(savedAtEpochMillis >= 0L) { "Coloring snapshot time cannot be negative." }
    }
}

class ColoringSessionEngine private constructor(initialState: ColoringSessionState) {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<ColoringSessionState> = _state.asStateFlow()

    fun selectColor(colorArgb: Int): Boolean = mutateActive { current ->
        current.copy(
            selectedTool = if (current.selectedTool == ColoringSessionTool.FILL) {
                ColoringSessionTool.FILL
            } else {
                ColoringSessionTool.BRUSH
            },
            selectedColorArgb = colorArgb,
        )
    }

    fun selectTool(tool: ColoringSessionTool): Boolean = mutateActive { current ->
        current.copy(selectedTool = tool)
    }

    fun setBrushWidth(width: Float): Boolean {
        if (!width.isFinite()) return false
        return mutateActive { current ->
            current.copy(
                brushWidth = width.coerceIn(
                    ColoringSessionState.MIN_BRUSH_WIDTH,
                    ColoringSessionState.MAX_BRUSH_WIDTH,
                ),
            )
        }
    }

    fun finish(): Boolean {
        val current = _state.value
        if (current.phase != ColoringSessionPhase.ACTIVE) return false
        _state.value = current.copy(phase = ColoringSessionPhase.FINISHED)
        return true
    }

    fun snapshot(nowEpochMillis: Long): ColoringSessionSnapshot {
        val current = _state.value
        return ColoringSessionSnapshot(
            sessionId = current.sessionId,
            childDocumentId = current.childDocumentId,
            lessonId = current.lessonId,
            lessonRevision = current.lessonRevision,
            mode = current.mode,
            phase = current.phase,
            selectedTool = current.selectedTool,
            selectedColorArgb = current.selectedColorArgb,
            brushWidth = current.brushWidth,
            savedAtEpochMillis = nowEpochMillis.coerceAtLeast(0L),
        )
    }

    private fun mutateActive(transform: (ColoringSessionState) -> ColoringSessionState): Boolean {
        val current = _state.value
        if (current.phase != ColoringSessionPhase.ACTIVE) return false
        _state.value = transform(current)
        return true
    }

    companion object {
        fun start(
            childDocumentId: String,
            lessonId: String,
            lessonRevision: Int,
            mode: ColoringSessionMode,
            defaultColorArgb: Int = ColoringSessionState.DEFAULT_COLOR_ARGB,
            defaultBrushWidth: Float = ColoringSessionState.DEFAULT_BRUSH_WIDTH,
        ): ColoringSessionEngine = ColoringSessionEngine(
            ColoringSessionState(
                sessionId = sessionIdFor(childDocumentId),
                childDocumentId = childDocumentId,
                lessonId = lessonId,
                lessonRevision = lessonRevision,
                mode = mode,
                phase = ColoringSessionPhase.ACTIVE,
                selectedTool = ColoringSessionTool.BRUSH,
                selectedColorArgb = defaultColorArgb,
                brushWidth = defaultBrushWidth.coerceIn(
                    ColoringSessionState.MIN_BRUSH_WIDTH,
                    ColoringSessionState.MAX_BRUSH_WIDTH,
                ),
            ),
        )

        fun restore(snapshot: ColoringSessionSnapshot): ColoringSessionEngine = ColoringSessionEngine(
            ColoringSessionState(
                sessionId = snapshot.sessionId,
                childDocumentId = snapshot.childDocumentId,
                lessonId = snapshot.lessonId,
                lessonRevision = snapshot.lessonRevision,
                mode = snapshot.mode,
                phase = snapshot.phase,
                selectedTool = snapshot.selectedTool,
                selectedColorArgb = snapshot.selectedColorArgb,
                brushWidth = snapshot.brushWidth,
            ),
        )

        fun sessionIdFor(childDocumentId: String): String {
            require(childDocumentId.isNotBlank()) { "Child document ID cannot be blank." }
            return "coloring-$childDocumentId"
        }
    }
}

const val CURRENT_COLORING_SESSION_SCHEMA_VERSION: Int = 1
