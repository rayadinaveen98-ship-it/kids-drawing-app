package com.navin.kidsdrawing.drawing.demo

import com.navin.kidsdrawing.drawing.domain.InkStrokeRecord
import com.navin.kidsdrawing.drawing.domain.PointerTool
import com.navin.kidsdrawing.drawing.domain.StrokeAuthorRole
import com.navin.kidsdrawing.drawing.domain.StrokePoint
import com.navin.kidsdrawing.drawing.domain.TeacherStrokeSequence
import com.navin.kidsdrawing.drawing.domain.TeacherStrokeSource

/**
 * Small deterministic house drawing used only to prove the P1.5 teacher playback pipeline.
 * Production lessons will provide authored teacher sequences through the lesson/content system.
 */
object ArtLabTeacherDemo {
    fun sequence(): TeacherStrokeSequence = TeacherStrokeSequence(
        sequenceId = "art-lab-house-demo-v1",
        strokes = listOf(
            TeacherStrokeSource(
                startTimeMillis = 0L,
                stroke = teacherStroke(
                    id = "house-body",
                    points = listOf(
                        point(300f, 420f, 0L),
                        point(300f, 700f, 600L),
                        point(700f, 700f, 1_200L),
                        point(700f, 420f, 1_800L),
                        point(300f, 420f, 2_400L),
                    ),
                ),
            ),
            TeacherStrokeSource(
                startTimeMillis = 2_650L,
                stroke = teacherStroke(
                    id = "house-roof",
                    points = listOf(
                        point(270f, 430f, 0L),
                        point(500f, 235f, 650L),
                        point(730f, 430f, 1_300L),
                    ),
                ),
            ),
            TeacherStrokeSource(
                startTimeMillis = 4_200L,
                stroke = teacherStroke(
                    id = "house-door",
                    points = listOf(
                        point(455f, 700f, 0L),
                        point(455f, 545f, 350L),
                        point(545f, 545f, 650L),
                        point(545f, 700f, 1_000L),
                    ),
                ),
            ),
            TeacherStrokeSource(
                startTimeMillis = 5_450L,
                stroke = teacherStroke(
                    id = "house-window",
                    points = listOf(
                        point(590f, 500f, 0L),
                        point(660f, 500f, 220L),
                        point(660f, 570f, 440L),
                        point(590f, 570f, 660L),
                        point(590f, 500f, 880L),
                    ),
                ),
            ),
        ),
    )

    private fun teacherStroke(
        id: String,
        points: List<StrokePoint>,
    ): InkStrokeRecord = InkStrokeRecord(
        strokeId = id,
        brushPresetId = "teacher.pencil",
        colorArgb = 0xFF5C6F52.toInt(),
        opacity = 0.88f,
        baseSize = 12f,
        tool = PointerTool.STYLUS,
        points = points,
        authorRole = StrokeAuthorRole.TEACHER_GENERATED,
    )

    private fun point(
        x: Float,
        y: Float,
        elapsedTimeMillis: Long,
    ): StrokePoint = StrokePoint(
        x = x,
        y = y,
        elapsedTimeMillis = elapsedTimeMillis,
        pressure = 0.72f,
    )
}
