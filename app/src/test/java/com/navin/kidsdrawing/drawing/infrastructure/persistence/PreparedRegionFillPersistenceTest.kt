package com.navin.kidsdrawing.drawing.infrastructure.persistence

import com.navin.kidsdrawing.drawing.domain.COLORING_DOCUMENT_SCHEMA_VERSION
import com.navin.kidsdrawing.drawing.domain.ColorRegionFillRecord
import com.navin.kidsdrawing.drawing.domain.ColorRegionPoint
import com.navin.kidsdrawing.drawing.domain.DocumentOperation
import com.navin.kidsdrawing.drawing.domain.DocumentSize
import com.navin.kidsdrawing.drawing.domain.DrawingDocument
import com.navin.kidsdrawing.drawing.domain.REGION_FILL_DOCUMENT_SCHEMA_VERSION
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PreparedRegionFillPersistenceTest {
    private val codec = DrawingDocumentBinaryCodec(JvmStrokePayloadCodec)

    @Test
    fun schemaThreePreparedFillRoundTripsSelfContainedGeometry() {
        val fill = ColorRegionFillRecord(
            regionId = "roof",
            colorArgb = 0xFFE47C68.toInt(),
            points = listOf(
                ColorRegionPoint(200f, 400f),
                ColorRegionPoint(500f, 180f),
                ColorRegionPoint(800f, 400f),
            ),
        )
        val original = document(
            schema = REGION_FILL_DOCUMENT_SCHEMA_VERSION,
            operations = listOf(
                DocumentOperation.AddColorRegionFill(
                    operationId = "fill-1",
                    createdAtEpochMillis = 2L,
                    fill = fill,
                ),
            ),
        )

        val decoded = roundTrip(original)

        assertEquals(REGION_FILL_DOCUMENT_SCHEMA_VERSION, decoded.documentSchemaVersion)
        assertEquals(fill, decoded.activeColorRegionFills().single())
        assertEquals(original.operations, decoded.operations)
    }

    @Test
    fun schemaOneAndTwoDocumentsRemainReadable() {
        val schemaOne = roundTrip(document(schema = 1, operations = emptyList()))
        val schemaTwo = roundTrip(document(schema = COLORING_DOCUMENT_SCHEMA_VERSION, operations = emptyList()))

        assertEquals(1, schemaOne.documentSchemaVersion)
        assertEquals(COLORING_DOCUMENT_SCHEMA_VERSION, schemaTwo.documentSchemaVersion)
        assertTrue(schemaOne.operations.isEmpty())
        assertTrue(schemaTwo.operations.isEmpty())
    }

    private fun roundTrip(document: DrawingDocument): DrawingDocument {
        val bytes = ByteArrayOutputStream().also { codec.encode(document, it) }.toByteArray()
        return codec.decode(ByteArrayInputStream(bytes))
    }

    private fun document(
        schema: Int,
        operations: List<DocumentOperation>,
    ) = DrawingDocument(
        documentSchemaVersion = schema,
        documentId = "doc-$schema",
        logicalSize = DocumentSize(1000f, 1000f),
        createdAtEpochMillis = 1L,
        modifiedAtEpochMillis = 2L,
        operations = operations,
    )
}
