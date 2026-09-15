package com.navin.kidsdrawing.product.parent

import android.content.Context
import com.navin.kidsdrawing.gallery.domain.GalleryListResult
import com.navin.kidsdrawing.product.adaptive.LocalAdaptiveReadResult
import com.navin.kidsdrawing.product.adaptive.LocalAdaptiveStateRepository
import com.navin.kidsdrawing.product.freedraw.ProductFreeDrawRuntime
import com.navin.kidsdrawing.product.home.StudioHomeModel
import com.navin.kidsdrawing.product.home.StudioHomeRepository
import com.navin.kidsdrawing.product.profile.ChildProfile
import java.io.File

/**
 * Read-only Android adapter for Parent Zone learning visibility.
 *
 * It joins already accepted local truth stores. It owns no progress mutation path and creates no
 * analytics/history database.
 */
class ParentProgressRepository(context: Context) {
    private val appContext = context.applicationContext
    private val homeRepository = StudioHomeRepository(appContext)
    private val adaptiveRepository = LocalAdaptiveStateRepository(
        File(appContext.filesDir, LocalAdaptiveStateRepository.DIRECTORY_NAME),
    )
    private val galleryRuntime = ProductFreeDrawRuntime(appContext).galleryRuntime

    suspend fun load(profile: ChildProfile): ParentProgressModel {
        val home = runCatching { homeRepository.load(profile) }
            .getOrElse {
                StudioHomeModel(
                    recommendation = null,
                    resumeCandidate = null,
                    coloringResumeCandidate = null,
                    contentMessage = "Curriculum details are temporarily unavailable.",
                    recommendations = emptyList(),
                    categories = emptyList(),
                    journeys = emptyList(),
                )
            }
        val adaptiveRead = runCatching { adaptiveRepository.loadReadOnly() }
            .getOrElse { LocalAdaptiveReadResult.Unavailable(it.message) }
        val gallery = runCatching { galleryRuntime.listArtwork() }
            .getOrElse {
                GalleryListResult.Unavailable(
                    "Saved artwork activity could not be read. No artwork was changed.",
                )
            }

        return ParentProgressProjection.project(
            home = home,
            adaptiveRead = adaptiveRead,
            galleryResult = gallery,
        )
    }
}
