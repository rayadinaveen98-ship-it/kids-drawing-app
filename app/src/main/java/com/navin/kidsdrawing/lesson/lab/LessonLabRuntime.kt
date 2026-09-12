package com.navin.kidsdrawing.lesson.lab

import android.content.Context
import com.navin.kidsdrawing.lesson.content.AndroidAssetLessonSource
import com.navin.kidsdrawing.lesson.content.LessonPackageLoader
import java.io.File

/** Android dependency adapter for the pure Lesson Lab runtime core. */
class LessonLabRuntime(context: Context) : LessonLabRuntimeCore(
    documentRoot = File(context.filesDir, "lesson-lab-documents"),
    sessionRoot = File(context.filesDir, "lesson-lab-sessions"),
    lessonPackageResult = LessonPackageLoader(
        AndroidAssetLessonSource(context.assets),
    ).load(LESSON_ROOT),
)
