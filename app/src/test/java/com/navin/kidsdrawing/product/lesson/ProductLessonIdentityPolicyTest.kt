package com.navin.kidsdrawing.product.lesson

import com.navin.kidsdrawing.lesson.content.LessonCatalogIdentity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ProductLessonIdentityPolicyTest {
    @Test
    fun cuteCatRevisionOneKeepsExactPhase3Identity() {
        val identity = ProductLessonIdentityPolicy.forLesson(
            LessonCatalogIdentity("cute-cat", 1),
        )

        assertEquals("lesson-lab-cute-cat-session", identity.sessionId)
        assertEquals("lesson-lab-cute-cat-document", identity.documentId)
    }

    @Test
    fun nonLegacyIdentitiesAreDeterministicAndCollisionSafeForAcceptedIds() {
        val dotted = ProductLessonIdentityPolicy.forLesson(
            LessonCatalogIdentity("shape.cat", 2),
        )
        val dashed = ProductLessonIdentityPolicy.forLesson(
            LessonCatalogIdentity("shape-cat", 2),
        )
        val repeated = ProductLessonIdentityPolicy.forLesson(
            LessonCatalogIdentity("shape.cat", 2),
        )
        val newRevision = ProductLessonIdentityPolicy.forLesson(
            LessonCatalogIdentity("shape.cat", 3),
        )

        assertEquals(dotted, repeated)
        assertNotEquals(dotted, dashed)
        assertNotEquals(dotted, newRevision)
        assertEquals("lesson-shape_2ecat-r2-session", dotted.sessionId)
        assertEquals("lesson-shape_2ecat-r2-document", dotted.documentId)
    }
}
