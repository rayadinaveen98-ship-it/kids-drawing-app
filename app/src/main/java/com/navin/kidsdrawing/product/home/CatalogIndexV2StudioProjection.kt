package com.navin.kidsdrawing.product.home

import com.navin.kidsdrawing.lesson.content.CatalogIndexV2Entry
import com.navin.kidsdrawing.lesson.session.LessonSessionSnapshot
import com.navin.kidsdrawing.lesson.session.LessonSnapshotPhase
import com.navin.kidsdrawing.product.profile.AgeBand
import com.navin.kidsdrawing.product.profile.ChildInterest
import com.navin.kidsdrawing.product.profile.ChildProfile
import com.navin.kidsdrawing.lesson.model.AgeBand as LessonAgeBand

/** Metadata-only projection used by Home. No LessonRuntimePackage is loaded here. */
internal fun CatalogIndexV2Entry.toStudioRecommendation(profile: ChildProfile): LessonRecommendation {
    require(supportedModes.isNotEmpty()) { "Catalog entry requires at least one supported mode." }

    val expectedAgeBand = profile.ageBand.toLessonAgeBand()
    val ageFit = if (expectedAgeBand in ageBands) LessonAgeFit.EXACT else LessonAgeFit.FALLBACK
    val interestMatched = profile.interests.any { interest ->
        val keys = interest.matchKeys()
        categoryIds.any(keys::contains) || tags.any(keys::contains)
    }
    val reason = when {
        interestMatched -> RecommendationReason.INTEREST_MATCH
        ageFit == LessonAgeFit.EXACT -> RecommendationReason.AGE_MATCH
        else -> RecommendationReason.STARTER_PICK
    }
    val mode = profile.teachingMode.takeIf { it in supportedModes } ?: supportedModes.first()

    return LessonRecommendation(
        lessonId = identity.lessonId,
        lessonRevision = identity.revision,
        title = title,
        summary = summary,
        estimatedMinutes = estimatedMinutes,
        difficulty = difficulty,
        defaultMode = mode,
        defaultPace = profile.pace,
        ageFit = ageFit,
        reason = reason,
        primarySkillIds = skillIds.sorted().take(3),
        journeyIds = journeyIds.sorted(),
        categoryIds = categoryIds.sorted(),
        tags = tags.sorted(),
        prerequisiteLessonIds = prerequisiteLessonIds.sorted(),
    )
}

internal fun CatalogIndexV2Entry.resumeCandidate(
    snapshot: LessonSessionSnapshot,
): ResumeLessonCandidate? {
    if (snapshot.lessonId != identity.lessonId || snapshot.lessonRevision != identity.revision) return null
    if (snapshot.phase == LessonSnapshotPhase.READY || snapshot.phase == LessonSnapshotPhase.FINISHED) return null
    val mode = snapshot.mode ?: return null
    val pace = snapshot.pace ?: return null

    return ResumeLessonCandidate(
        sessionId = snapshot.sessionId,
        lessonId = snapshot.lessonId,
        lessonRevision = snapshot.lessonRevision,
        childDocumentId = snapshot.childDocumentId,
        phase = snapshot.phase,
        mode = mode,
        pace = pace,
        currentStepIndex = snapshot.currentStepIndex,
        currentStepId = snapshot.currentStepId,
        totalSteps = drawingStepCount,
        savedAtEpochMillis = snapshot.savedAtEpochMillis,
    )
}

private fun AgeBand.toLessonAgeBand(): LessonAgeBand = when (this) {
    AgeBand.LITTLE_ARTIST -> LessonAgeBand.LITTLE_ARTISTS
    AgeBand.CREATIVE_EXPLORER -> LessonAgeBand.CREATIVE_EXPLORERS
    AgeBand.GROWING_ARTIST -> LessonAgeBand.GROWING_ARTISTS
    AgeBand.YOUNG_ARTIST -> LessonAgeBand.YOUNG_ARTISTS
}

private fun ChildInterest.matchKeys(): Set<String> = when (this) {
    ChildInterest.ANIMALS -> setOf("animals", "pets", "cat")
    ChildInterest.VEHICLES -> setOf("vehicles", "transport", "cars")
    ChildInterest.NATURE -> setOf("nature", "plants", "landscape")
    ChildInterest.CHARACTERS -> setOf("characters", "people", "faces")
    ChildInterest.SPACE -> setOf("space", "astronomy")
    ChildInterest.FANTASY -> setOf("fantasy", "magic")
}
