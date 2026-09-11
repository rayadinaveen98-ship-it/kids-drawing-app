package com.navin.kidsdrawing.drawing.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class DocumentViewportMapperTest {
    private val mapper = DocumentViewportMapper(DocumentSize(width = 1000f, height = 1000f))

    @Test
    fun `fit center keeps logical document square inside landscape viewport`() {
        val transform = requireNotNull(mapper.transformFor(1200f, 800f))

        assertEquals(0.8f, transform.scale, 0.0001f)
        assertEquals(200f, transform.offsetX, 0.0001f)
        assertEquals(0f, transform.offsetY, 0.0001f)
    }

    @Test
    fun `document point round trips through viewport transform`() {
        val source = DocumentPoint(250f, 750f)
        val transform = requireNotNull(mapper.transformFor(1200f, 800f))

        val viewportPoint = mapper.documentToViewport(source, transform)
        val restored = mapper.viewportToDocument(viewportPoint.x, viewportPoint.y, transform)

        assertEquals(source.x, restored.x, 0.0001f)
        assertEquals(source.y, restored.y, 0.0001f)
    }

    @Test
    fun `same logical point survives viewport resize and rotation`() {
        val source = DocumentPoint(330f, 610f)
        val portrait = requireNotNull(mapper.transformFor(800f, 1200f))
        val landscape = requireNotNull(mapper.transformFor(1200f, 800f))

        val portraitViewport = mapper.documentToViewport(source, portrait)
        val landscapeViewport = mapper.documentToViewport(source, landscape)

        val portraitRestored = mapper.viewportToDocument(
            portraitViewport.x,
            portraitViewport.y,
            portrait,
        )
        val landscapeRestored = mapper.viewportToDocument(
            landscapeViewport.x,
            landscapeViewport.y,
            landscape,
        )

        assertEquals(source.x, portraitRestored.x, 0.0001f)
        assertEquals(source.y, portraitRestored.y, 0.0001f)
        assertEquals(source.x, landscapeRestored.x, 0.0001f)
        assertEquals(source.y, landscapeRestored.y, 0.0001f)
    }

    @Test
    fun `letterboxed coordinates outside paper are rejected`() {
        val transform = requireNotNull(mapper.transformFor(1200f, 800f))

        assertNull(mapper.viewportToDocumentOrNull(100f, 400f, transform))
        assertNotNull(mapper.viewportToDocumentOrNull(600f, 400f, transform))
    }

    @Test
    fun `invalid viewport does not produce transform`() {
        assertNull(mapper.transformFor(0f, 800f))
        assertNull(mapper.transformFor(800f, -1f))
        assertNull(mapper.transformFor(Float.NaN, 800f))
    }
}
