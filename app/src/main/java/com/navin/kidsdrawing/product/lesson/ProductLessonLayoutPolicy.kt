package com.navin.kidsdrawing.product.lesson

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.navin.kidsdrawing.product.profile.AgeBand

data class ProductLessonLayoutPolicy(
    val minimumControlHeight: Dp,
    val optionCardMinHeight: Dp,
    val sectionGap: Dp,
    val horizontalGutter: Dp,
    val maxCompactActionColumns: Int,
)

fun lessonLayoutPolicyFor(ageBand: AgeBand): ProductLessonLayoutPolicy = when (ageBand) {
    AgeBand.LITTLE_ARTIST -> ProductLessonLayoutPolicy(
        minimumControlHeight = 68.dp,
        optionCardMinHeight = 72.dp,
        sectionGap = 20.dp,
        horizontalGutter = 20.dp,
        maxCompactActionColumns = 2,
    )
    AgeBand.CREATIVE_EXPLORER -> ProductLessonLayoutPolicy(
        minimumControlHeight = 64.dp,
        optionCardMinHeight = 68.dp,
        sectionGap = 18.dp,
        horizontalGutter = 22.dp,
        maxCompactActionColumns = 2,
    )
    AgeBand.GROWING_ARTIST -> ProductLessonLayoutPolicy(
        minimumControlHeight = 58.dp,
        optionCardMinHeight = 62.dp,
        sectionGap = 16.dp,
        horizontalGutter = 22.dp,
        maxCompactActionColumns = 3,
    )
    AgeBand.YOUNG_ARTIST -> ProductLessonLayoutPolicy(
        minimumControlHeight = 54.dp,
        optionCardMinHeight = 58.dp,
        sectionGap = 14.dp,
        horizontalGutter = 24.dp,
        maxCompactActionColumns = 3,
    )
}
