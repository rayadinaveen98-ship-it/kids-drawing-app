package com.navin.kidsdrawing.drawing.infrastructure.persistence

import com.navin.kidsdrawing.drawing.domain.BackgroundRole
import com.navin.kidsdrawing.drawing.domain.CURRENT_DOCUMENT_SCHEMA_VERSION
import com.navin.kidsdrawing.drawing.domain.DocumentOperation
import com.navin.kidsdrawing.drawing.domain.DocumentSize
import com.navin.kidsdrawing.drawing.domain.DrawingDocument
import com.navin.kidsdrawing.drawing.domain.DrawingDocumentMetadata
import com.navin.kidsdrawing.drawing.domain.EraseMaskRecord
import com.navin.kidsdrawing.drawing.domain.InkStrokeRecord
import com.navin.kidsdrawing.drawing.domain.PointerTool
import com.navin.kidsdrawing.drawing.domain.StrokeAuthorRole
import com.navin.kidsdrawing.drawing.domain.StrokePoint
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.CRC32

/**
 * App-owned, checksummed Art Lab document envelope.
 *
 * The envelope owns schema/versioning and operation ordering. Production embeds AndroidX Ink's
 * stable input-batch payload through [InkStrokePayloadCodec], while tests can inject a host-safe
 * codec without changing envelope or store behavior.
 */
class DrawingDocumentBinaryCodec(
    private val strokePayloadCodec: StrokePayloadCodec = InkStrokePayloadCodec,
) {
    fun encode(document: DrawingDocument, output: OutputStream) {
        val bodyBytes = ByteArrayOutputStream().use { bodyBuffer ->
            val body = DataOutputStream(bodyBuffer)
            writeDocumentBody(body, document)
            body.flush()
            bodyBuffer.toByteArray()
        }
        require(bodyBytes.size <= MAX_BODY_BYTES) { "Drawing document exceeds Art Lab size limit." }

        val crc = CRC32().apply { update(bodyBytes) }.value
        val data = DataOutputStream(output)
        data.writeInt(MAGIC)
        data.writeInt(ENVELOPE_VERSION)
        data.writeInt(bodyBytes.size)
        data.write(bodyBytes)
        data.writeLong(crc)
        data.flush()
    }

    fun decode(input: InputStream): DrawingDocument {
        val data = DataInputStream(input)
        require(data.readInt() == MAGIC) { "Not a Kids Drawing document envelope." }
        val envelopeVersion = data.readInt()
        require(envelopeVersion == ENVELOPE_VERSION) {
            "Unsupported document envelope version: $envelopeVersion"
        }
        val bodyLength = data.readInt()
        require(bodyLength in 1..MAX_BODY_BYTES) { "Invalid document body length: $bodyLength" }
        val bodyBytes = ByteArray(bodyLength)
        data.readFully(bodyBytes)
        val expectedCrc = data.readLong()
        val actualCrc = CRC32().apply { update(bodyBytes) }.value
        require(expectedCrc == actualCrc) { "Document checksum mismatch." }

        return DataInputStream(ByteArrayInputStream(bodyBytes)).use(::readDocumentBody)
    }

    private fun writeDocumentBody(data: DataOutputStream, document: DrawingDocument) {
        data.writeInt(document.documentSchemaVersion)
        writeString(data, document.documentId)
        data.writeFloat(document.logicalSize.width)
        data.writeFloat(document.logicalSize.height)
        data.writeLong(document.createdAtEpochMillis)
        data.writeLong(document.modifiedAtEpochMillis)
        data.writeByte(backgroundTag(document.backgroundRole))

        writeNullableString(data, document.metadata.lessonId)
        if (document.metadata.lessonRevision != null) {
            data.writeBoolean(true)
            data.writeInt(document.metadata.lessonRevision)
        } else {
            data.writeBoolean(false)
        }

        data.writeInt(document.operations.size)
        document.operations.forEach { operation ->
            when (operation) {
                is DocumentOperation.AddInkStroke -> {
                    data.writeByte(OP_ADD_INK)
                    writeOperationHeader(data, operation.operationId, operation.createdAtEpochMillis)
                    writeInkStroke(data, operation.stroke)
                }
                is DocumentOperation.AddEraseMask -> {
                    data.writeByte(OP_ADD_ERASE_MASK)
                    writeOperationHeader(data, operation.operationId, operation.createdAtEpochMillis)
                    writeEraseMask(data, operation.mask)
                }
                is DocumentOperation.ClearDocument -> {
                    data.writeByte(OP_CLEAR)
                    writeOperationHeader(data, operation.operationId, operation.createdAtEpochMillis)
                }
            }
        }
    }

    private fun readDocumentBody(data: DataInputStream): DrawingDocument {
        val schemaVersion = data.readInt()
        require(schemaVersion in 1..CURRENT_DOCUMENT_SCHEMA_VERSION) {
            "Unsupported document schema version: $schemaVersion"
        }
        val documentId = readString(data)
        val logicalSize = DocumentSize(data.readFloat(), data.readFloat())
        val createdAt = data.readLong()
        val modifiedAt = data.readLong()
        val background = backgroundFromTag(data.readUnsignedByte())
        val lessonId = readNullableString(data)
        val lessonRevision = if (data.readBoolean()) data.readInt() else null

        val operationCount = data.readInt()
        require(operationCount in 0..MAX_OPERATIONS) { "Invalid operation count: $operationCount" }
        val operations = ArrayList<DocumentOperation>(operationCount)
        repeat(operationCount) {
            val tag = data.readUnsignedByte()
            val operationId = readString(data)
            val operationTime = data.readLong()
            operations += when (tag) {
                OP_ADD_INK -> DocumentOperation.AddInkStroke(
                    operationId = operationId,
                    createdAtEpochMillis = operationTime,
                    stroke = readInkStroke(data),
                )
                OP_ADD_ERASE_MASK -> DocumentOperation.AddEraseMask(
                    operationId = operationId,
                    createdAtEpochMillis = operationTime,
                    mask = readEraseMask(data),
                )
                OP_CLEAR -> DocumentOperation.ClearDocument(
                    operationId = operationId,
                    createdAtEpochMillis = operationTime,
                )
                else -> error("Unknown document operation tag: $tag")
            }
        }

        return DrawingDocument(
            documentSchemaVersion = schemaVersion,
            documentId = documentId,
            logicalSize = logicalSize,
            createdAtEpochMillis = createdAt,
            modifiedAtEpochMillis = modifiedAt,
            backgroundRole = background,
            metadata = DrawingDocumentMetadata(
                lessonId = lessonId,
                lessonRevision = lessonRevision,
            ),
            operations = operations,
        )
    }

    private fun writeOperationHeader(data: DataOutputStream, id: String, createdAt: Long) {
        writeString(data, id)
        data.writeLong(createdAt)
    }

    private fun writeInkStroke(data: DataOutputStream, stroke: InkStrokeRecord) {
        writeString(data, stroke.strokeId)
        writeString(data, stroke.brushPresetId)
        data.writeInt(stroke.colorArgb)
        data.writeFloat(stroke.opacity)
        data.writeFloat(stroke.baseSize)
        data.writeByte(pointerToolTag(stroke.tool))
        data.writeByte(authorRoleTag(stroke.authorRole))

        val payload = ByteArrayOutputStream().use { buffer ->
            strokePayloadCodec.encode(stroke, buffer)
            buffer.toByteArray()
        }
        require(payload.size in 1..MAX_STROKE_PAYLOAD_BYTES) { "Invalid stroke payload size." }
        data.writeInt(payload.size)
        data.write(payload)
    }

    private fun readInkStroke(data: DataInputStream): InkStrokeRecord {
        val strokeId = readString(data)
        val brushPresetId = readString(data)
        val colorArgb = data.readInt()
        val opacity = data.readFloat()
        val baseSize = data.readFloat()
        val storedTool = pointerToolFromTag(data.readUnsignedByte())
        val authorRole = authorRoleFromTag(data.readUnsignedByte())
        val payloadLength = data.readInt()
        require(payloadLength in 1..MAX_STROKE_PAYLOAD_BYTES) {
            "Invalid stroke payload length: $payloadLength"
        }
        val payloadBytes = ByteArray(payloadLength)
        data.readFully(payloadBytes)
        val decoded = strokePayloadCodec.decode(ByteArrayInputStream(payloadBytes))
        require(toPayloadCompatibleTool(storedTool) == decoded.tool) {
            "Stroke payload tool type does not match stroke metadata."
        }

        return InkStrokeRecord(
            strokeId = strokeId,
            brushPresetId = brushPresetId,
            colorArgb = colorArgb,
            opacity = opacity,
            baseSize = baseSize,
            tool = storedTool,
            points = decoded.points,
            authorRole = authorRole,
        )
    }

    private fun writeEraseMask(data: DataOutputStream, mask: EraseMaskRecord) {
        writeString(data, mask.maskId)
        data.writeFloat(mask.baseSize)
        data.writeInt(mask.points.size)
        mask.points.forEach { writePoint(data, it) }
    }

    private fun readEraseMask(data: DataInputStream): EraseMaskRecord {
        val maskId = readString(data)
        val baseSize = data.readFloat()
        val pointCount = data.readInt()
        require(pointCount in 1..MAX_POINTS_PER_MASK) { "Invalid erase-mask point count: $pointCount" }
        return EraseMaskRecord(
            maskId = maskId,
            baseSize = baseSize,
            points = List(pointCount) { readPoint(data) },
        )
    }

    private fun writePoint(data: DataOutputStream, point: StrokePoint) {
        data.writeFloat(point.x)
        data.writeFloat(point.y)
        data.writeLong(point.elapsedTimeMillis)
        data.writeFloat(point.pressure)
        writeNullableFloat(data, point.tiltRadians)
        writeNullableFloat(data, point.orientationRadians)
    }

    private fun readPoint(data: DataInputStream): StrokePoint {
        val x = data.readFloat()
        val y = data.readFloat()
        val elapsed = data.readLong()
        val pressure = data.readFloat()
        require(x.isFinite() && y.isFinite()) { "Persisted point coordinates must be finite." }
        require(elapsed >= 0L) { "Persisted point time cannot be negative." }
        require(pressure.isFinite() && pressure in 0f..1f) { "Persisted point pressure is invalid." }
        return StrokePoint(
            x = x,
            y = y,
            elapsedTimeMillis = elapsed,
            pressure = pressure,
            tiltRadians = readNullableFloat(data),
            orientationRadians = readNullableFloat(data),
        )
    }

    private fun writeString(data: DataOutputStream, value: String) {
        val bytes = value.toByteArray(StandardCharsets.UTF_8)
        require(bytes.size <= MAX_STRING_BYTES) { "String exceeds document envelope limit." }
        data.writeInt(bytes.size)
        data.write(bytes)
    }

    private fun readString(data: DataInputStream): String {
        val length = data.readInt()
        require(length in 0..MAX_STRING_BYTES) { "Invalid string length: $length" }
        val bytes = ByteArray(length)
        data.readFully(bytes)
        return String(bytes, StandardCharsets.UTF_8)
    }

    private fun writeNullableString(data: DataOutputStream, value: String?) {
        data.writeBoolean(value != null)
        if (value != null) writeString(data, value)
    }

    private fun readNullableString(data: DataInputStream): String? =
        if (data.readBoolean()) readString(data) else null

    private fun writeNullableFloat(data: DataOutputStream, value: Float?) {
        data.writeBoolean(value != null)
        if (value != null) data.writeFloat(value)
    }

    private fun readNullableFloat(data: DataInputStream): Float? {
        if (!data.readBoolean()) return null
        return data.readFloat().also { require(it.isFinite()) { "Persisted optional float is invalid." } }
    }

    private fun backgroundTag(role: BackgroundRole): Int = when (role) {
        BackgroundRole.PAPER -> 1
    }

    private fun backgroundFromTag(tag: Int): BackgroundRole = when (tag) {
        1 -> BackgroundRole.PAPER
        else -> error("Unknown background role tag: $tag")
    }

    private fun pointerToolTag(tool: PointerTool): Int = when (tool) {
        PointerTool.FINGER -> 1
        PointerTool.STYLUS -> 2
        PointerTool.STYLUS_ERASER -> 3
        PointerTool.UNKNOWN -> 4
    }

    private fun pointerToolFromTag(tag: Int): PointerTool = when (tag) {
        1 -> PointerTool.FINGER
        2 -> PointerTool.STYLUS
        3 -> PointerTool.STYLUS_ERASER
        4 -> PointerTool.UNKNOWN
        else -> error("Unknown pointer tool tag: $tag")
    }

    private fun authorRoleTag(role: StrokeAuthorRole): Int = when (role) {
        StrokeAuthorRole.CHILD -> 1
        StrokeAuthorRole.TEACHER_GENERATED -> 2
    }

    private fun authorRoleFromTag(tag: Int): StrokeAuthorRole = when (tag) {
        1 -> StrokeAuthorRole.CHILD
        2 -> StrokeAuthorRole.TEACHER_GENERATED
        else -> error("Unknown stroke author tag: $tag")
    }

    private fun toPayloadCompatibleTool(tool: PointerTool): PointerTool = when (tool) {
        PointerTool.STYLUS_ERASER -> PointerTool.STYLUS
        else -> tool
    }

    private companion object {
        const val MAGIC = 0x4B444131 // KDA1
        const val ENVELOPE_VERSION = 1
        const val OP_ADD_INK = 1
        const val OP_ADD_ERASE_MASK = 2
        const val OP_CLEAR = 3
        const val MAX_BODY_BYTES = 64 * 1024 * 1024
        const val MAX_STROKE_PAYLOAD_BYTES = 16 * 1024 * 1024
        const val MAX_STRING_BYTES = 1024 * 1024
        const val MAX_OPERATIONS = 100_000
        const val MAX_POINTS_PER_MASK = 1_000_000
    }
}
