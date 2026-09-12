package com.navin.kidsdrawing.gallery.persistence

import com.navin.kidsdrawing.gallery.domain.CURRENT_GALLERY_CATALOG_SCHEMA_VERSION
import com.navin.kidsdrawing.gallery.domain.GalleryArtworkRecord
import com.navin.kidsdrawing.gallery.domain.GalleryArtworkSource
import com.navin.kidsdrawing.gallery.domain.GalleryCatalog
import com.navin.kidsdrawing.gallery.domain.GalleryCompletionKind
import com.navin.kidsdrawing.gallery.domain.GalleryPreviewStatus
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.CRC32

class GalleryCatalogCodec {
    fun encode(catalog: GalleryCatalog, output: OutputStream) {
        val normalized = catalog.newestFirst()
        val bodyBytes = ByteArrayOutputStream().use { buffer ->
            val body = DataOutputStream(buffer)
            body.writeInt(normalized.schemaVersion)
            body.writeInt(normalized.records.size)
            normalized.records.forEach { record ->
                writeString(body, record.entryId)
                writeString(body, record.documentId)
                writeString(body, record.title)
                body.writeByte(record.source.ordinal)
                writeNullableString(body, record.lessonId)
                if (record.lessonRevision == null) {
                    body.writeBoolean(false)
                } else {
                    body.writeBoolean(true)
                    body.writeInt(record.lessonRevision)
                }
                body.writeByte(record.completionKind.ordinal)
                body.writeLong(record.completedAtEpochMillis)
                body.writeByte(record.previewStatus.ordinal)
                writeNullableString(body, record.previewReference)
            }
            body.flush()
            buffer.toByteArray()
        }
        require(bodyBytes.size in 1..MAX_BODY_BYTES) { "Invalid Gallery catalog body size." }
        val crc = CRC32().apply { update(bodyBytes) }.value
        val data = DataOutputStream(output)
        data.writeInt(MAGIC)
        data.writeInt(ENVELOPE_VERSION)
        data.writeInt(bodyBytes.size)
        data.write(bodyBytes)
        data.writeLong(crc)
        data.flush()
    }

    fun decode(input: InputStream): GalleryCatalog {
        val data = DataInputStream(input)
        require(data.readInt() == MAGIC) { "Not a Kids Drawing Gallery catalog." }
        require(data.readInt() == ENVELOPE_VERSION) { "Unsupported Gallery catalog envelope." }
        val bodyLength = data.readInt()
        require(bodyLength in 1..MAX_BODY_BYTES) { "Invalid Gallery catalog body length." }
        val bodyBytes = ByteArray(bodyLength)
        data.readFully(bodyBytes)
        val expectedCrc = data.readLong()
        val actualCrc = CRC32().apply { update(bodyBytes) }.value
        require(expectedCrc == actualCrc) { "Gallery catalog checksum mismatch." }

        return DataInputStream(ByteArrayInputStream(bodyBytes)).use { body ->
            val schema = body.readInt()
            require(schema == CURRENT_GALLERY_CATALOG_SCHEMA_VERSION) {
                "Unsupported Gallery catalog schema: $schema"
            }
            val count = body.readInt()
            require(count in 0..MAX_RECORDS) { "Invalid Gallery record count: $count" }
            val records = List(count) {
                GalleryArtworkRecord(
                    entryId = readString(body),
                    documentId = readString(body),
                    title = readString(body),
                    source = enumValue<GalleryArtworkSource>(body.readUnsignedByte()),
                    lessonId = readNullableString(body),
                    lessonRevision = if (body.readBoolean()) body.readInt() else null,
                    completionKind = enumValue<GalleryCompletionKind>(body.readUnsignedByte()),
                    completedAtEpochMillis = body.readLong(),
                    previewStatus = enumValue<GalleryPreviewStatus>(body.readUnsignedByte()),
                    previewReference = readNullableString(body),
                )
            }
            GalleryCatalog(schemaVersion = schema, records = records).newestFirst()
        }
    }

    private inline fun <reified T : Enum<T>> enumValue(ordinal: Int): T {
        val values = enumValues<T>()
        require(ordinal in values.indices) { "Invalid ${T::class.simpleName} ordinal: $ordinal" }
        return values[ordinal]
    }

    private fun writeNullableString(data: DataOutputStream, value: String?) {
        data.writeBoolean(value != null)
        if (value != null) writeString(data, value)
    }

    private fun readNullableString(data: DataInputStream): String? =
        if (data.readBoolean()) readString(data) else null

    private fun writeString(data: DataOutputStream, value: String) {
        val bytes = value.toByteArray(StandardCharsets.UTF_8)
        require(bytes.size <= MAX_STRING_BYTES) { "Gallery string is too large." }
        data.writeInt(bytes.size)
        data.write(bytes)
    }

    private fun readString(data: DataInputStream): String {
        val length = data.readInt()
        require(length in 0..MAX_STRING_BYTES) { "Invalid Gallery string length." }
        val bytes = ByteArray(length)
        data.readFully(bytes)
        return String(bytes, StandardCharsets.UTF_8)
    }

    private companion object {
        const val MAGIC = 0x4B474331 // KGC1
        const val ENVELOPE_VERSION = 1
        const val MAX_RECORDS = 10_000
        const val MAX_BODY_BYTES = 4 * 1024 * 1024
        const val MAX_STRING_BYTES = 64 * 1024
    }
}
