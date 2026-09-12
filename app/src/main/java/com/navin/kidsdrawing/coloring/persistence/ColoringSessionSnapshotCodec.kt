package com.navin.kidsdrawing.coloring.persistence

import com.navin.kidsdrawing.coloring.session.CURRENT_COLORING_SESSION_SCHEMA_VERSION
import com.navin.kidsdrawing.coloring.session.ColoringSessionMode
import com.navin.kidsdrawing.coloring.session.ColoringSessionPhase
import com.navin.kidsdrawing.coloring.session.ColoringSessionSnapshot
import com.navin.kidsdrawing.coloring.session.ColoringSessionTool
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.CRC32

class ColoringSessionSnapshotCodec {
    fun encode(snapshot: ColoringSessionSnapshot, output: OutputStream) {
        val bodyBytes = ByteArrayOutputStream().use { buffer ->
            val body = DataOutputStream(buffer)
            body.writeInt(snapshot.schemaVersion)
            writeString(body, snapshot.sessionId)
            writeString(body, snapshot.childDocumentId)
            writeString(body, snapshot.lessonId)
            body.writeInt(snapshot.lessonRevision)
            body.writeByte(snapshot.mode.ordinal)
            body.writeByte(snapshot.phase.ordinal)
            body.writeByte(snapshot.selectedTool.ordinal)
            body.writeInt(snapshot.selectedColorArgb)
            body.writeFloat(snapshot.brushWidth)
            body.writeLong(snapshot.savedAtEpochMillis)
            body.flush()
            buffer.toByteArray()
        }
        require(bodyBytes.size in 1..MAX_BODY_BYTES) { "Invalid coloring snapshot body size." }
        val crc = CRC32().apply { update(bodyBytes) }.value
        val data = DataOutputStream(output)
        data.writeInt(MAGIC)
        data.writeInt(ENVELOPE_VERSION)
        data.writeInt(bodyBytes.size)
        data.write(bodyBytes)
        data.writeLong(crc)
        data.flush()
    }

    fun decode(input: InputStream): ColoringSessionSnapshot {
        val data = DataInputStream(input)
        require(data.readInt() == MAGIC) { "Not a Kids Drawing coloring session." }
        require(data.readInt() == ENVELOPE_VERSION) { "Unsupported coloring session envelope." }
        val bodyLength = data.readInt()
        require(bodyLength in 1..MAX_BODY_BYTES) { "Invalid coloring snapshot body length." }
        val bodyBytes = ByteArray(bodyLength)
        data.readFully(bodyBytes)
        val expectedCrc = data.readLong()
        val actualCrc = CRC32().apply { update(bodyBytes) }.value
        require(expectedCrc == actualCrc) { "Coloring snapshot checksum mismatch." }

        return DataInputStream(ByteArrayInputStream(bodyBytes)).use { body ->
            val schema = body.readInt()
            require(schema == CURRENT_COLORING_SESSION_SCHEMA_VERSION) {
                "Unsupported coloring session schema: $schema"
            }
            ColoringSessionSnapshot(
                schemaVersion = schema,
                sessionId = readString(body),
                childDocumentId = readString(body),
                lessonId = readString(body),
                lessonRevision = body.readInt(),
                mode = enumValue<ColoringSessionMode>(body.readUnsignedByte()),
                phase = enumValue<ColoringSessionPhase>(body.readUnsignedByte()),
                selectedTool = enumValue<ColoringSessionTool>(body.readUnsignedByte()),
                selectedColorArgb = body.readInt(),
                brushWidth = body.readFloat(),
                savedAtEpochMillis = body.readLong(),
            )
        }
    }

    private inline fun <reified T : Enum<T>> enumValue(ordinal: Int): T {
        val entries = enumValues<T>()
        require(ordinal in entries.indices) { "Invalid ${T::class.simpleName} ordinal: $ordinal" }
        return entries[ordinal]
    }

    private fun writeString(data: DataOutputStream, value: String) {
        val bytes = value.toByteArray(StandardCharsets.UTF_8)
        require(bytes.size <= MAX_STRING_BYTES) { "Coloring snapshot string is too large." }
        data.writeInt(bytes.size)
        data.write(bytes)
    }

    private fun readString(data: DataInputStream): String {
        val length = data.readInt()
        require(length in 0..MAX_STRING_BYTES) { "Invalid coloring snapshot string length." }
        val bytes = ByteArray(length)
        data.readFully(bytes)
        return String(bytes, StandardCharsets.UTF_8)
    }

    private companion object {
        const val MAGIC = 0x4B435331 // KCS1
        const val ENVELOPE_VERSION = 1
        const val MAX_BODY_BYTES = 256 * 1024
        const val MAX_STRING_BYTES = 64 * 1024
    }
}
