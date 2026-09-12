package com.navin.kidsdrawing.lesson.persistence

import com.navin.kidsdrawing.drawing.domain.TeachingPace
import com.navin.kidsdrawing.lesson.model.TeachingMode
import com.navin.kidsdrawing.lesson.session.LessonFinishReason
import com.navin.kidsdrawing.lesson.session.LessonSessionSnapshot
import com.navin.kidsdrawing.lesson.session.LessonSnapshotPhase
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put

/**
 * Stable app-owned persistence envelope for semantic Lesson Engine snapshots.
 *
 * The JSON payload is intentionally independent from Android UI/runtime objects. The outer binary
 * envelope protects it with an explicit format version and SHA-256 checksum so truncated or
 * corrupted session files fail closed and can fall back to the atomic backup.
 */
class LessonSessionSnapshotCodec(
    private val json: Json = Json,
) {
    fun encode(snapshot: LessonSessionSnapshot, output: OutputStream) {
        val payload = json.encodeToString(JsonObject.serializer(), snapshot.toJsonObject())
            .toByteArray(Charsets.UTF_8)
        require(payload.size <= MAX_PAYLOAD_BYTES) { "Lesson session snapshot is unexpectedly large." }
        val checksum = sha256(payload)

        DataOutputStream(output).use { data ->
            data.writeInt(MAGIC)
            data.writeInt(ENVELOPE_VERSION)
            data.writeInt(payload.size)
            data.writeInt(checksum.size)
            data.write(checksum)
            data.write(payload)
        }
    }

    @Throws(IOException::class)
    fun decode(input: InputStream): LessonSessionSnapshot {
        val data = DataInputStream(input)
        if (data.readInt() != MAGIC) {
            throw IOException("Invalid lesson-session envelope magic.")
        }
        val envelopeVersion = data.readInt()
        if (envelopeVersion != ENVELOPE_VERSION) {
            throw IOException("Unsupported lesson-session envelope version $envelopeVersion.")
        }
        val payloadLength = data.readInt()
        if (payloadLength <= 0 || payloadLength > MAX_PAYLOAD_BYTES) {
            throw IOException("Invalid lesson-session payload length $payloadLength.")
        }
        val checksumLength = data.readInt()
        if (checksumLength != SHA_256_BYTES) {
            throw IOException("Invalid lesson-session checksum length $checksumLength.")
        }

        val expectedChecksum = ByteArray(checksumLength)
        data.readFully(expectedChecksum)
        val payload = ByteArray(payloadLength)
        data.readFully(payload)
        if (!sha256(payload).contentEquals(expectedChecksum)) {
            throw IOException("Lesson-session checksum mismatch.")
        }
        if (data.read() != -1) {
            throw IOException("Lesson-session envelope contains trailing bytes.")
        }

        val root = runCatching {
            json.parseToJsonElement(payload.toString(Charsets.UTF_8)).jsonObject
        }.getOrElse { failure ->
            throw IOException("Lesson-session payload is not valid JSON.", failure)
        }
        return root.toSnapshot()
    }

    private fun LessonSessionSnapshot.toJsonObject(): JsonObject = buildJsonObject {
        put("formatVersion", formatVersion)
        put("sessionId", sessionId)
        put("lessonId", lessonId)
        put("lessonRevision", lessonRevision)
        put("childDocumentId", childDocumentId)
        putNullableString("mode", mode?.name)
        putNullableString("pace", pace?.name)
        put("phase", phase.name)
        putNullableString("pausedResumePhase", pausedResumePhase?.name)
        putNullableString("transientRuntimePhase", transientRuntimePhase?.name)
        if (currentStepIndex == null) put("currentStepIndex", JsonNull) else put("currentStepIndex", currentStepIndex)
        putNullableString("currentStepId", currentStepId)
        put("helpLevel", helpLevel)
        put("overviewCompleted", overviewCompleted)
        putNullableString("finishReason", finishReason?.name)
        put("runtimeGeneration", runtimeGeneration)
        put("savedAtEpochMillis", savedAtEpochMillis)
    }

    private fun JsonObject.toSnapshot(): LessonSessionSnapshot = try {
        LessonSessionSnapshot(
            formatVersion = requiredInt("formatVersion"),
            sessionId = requiredString("sessionId"),
            lessonId = requiredString("lessonId"),
            lessonRevision = requiredInt("lessonRevision"),
            childDocumentId = requiredString("childDocumentId"),
            mode = optionalEnum<TeachingMode>("mode"),
            pace = optionalEnum<TeachingPace>("pace"),
            phase = requiredEnum("phase"),
            pausedResumePhase = optionalEnum<LessonSnapshotPhase>("pausedResumePhase"),
            transientRuntimePhase = optionalEnum<LessonSnapshotPhase>("transientRuntimePhase"),
            currentStepIndex = optionalInt("currentStepIndex"),
            currentStepId = optionalString("currentStepId"),
            helpLevel = requiredInt("helpLevel"),
            overviewCompleted = requiredBoolean("overviewCompleted"),
            finishReason = optionalEnum<LessonFinishReason>("finishReason"),
            runtimeGeneration = optionalInt("runtimeGeneration") ?: 0,
            savedAtEpochMillis = requiredLong("savedAtEpochMillis"),
        )
    } catch (failure: IllegalArgumentException) {
        throw IOException("Lesson-session payload contains invalid semantic values.", failure)
    }

    private fun JsonObject.requiredString(key: String): String = this[key]
        ?.jsonPrimitive
        ?.contentOrNull
        ?.takeIf(String::isNotBlank)
        ?: throw IOException("Lesson-session payload is missing non-blank '$key'.")

    private fun JsonObject.optionalString(key: String): String? = this[key]
        ?.takeUnless { it is JsonNull }
        ?.jsonPrimitive
        ?.contentOrNull

    private fun JsonObject.requiredInt(key: String): Int = this[key]
        ?.jsonPrimitive
        ?.int
        ?: throw IOException("Lesson-session payload is missing integer '$key'.")

    private fun JsonObject.optionalInt(key: String): Int? = this[key]
        ?.takeUnless { it is JsonNull }
        ?.jsonPrimitive
        ?.int

    private fun JsonObject.requiredLong(key: String): Long = this[key]
        ?.jsonPrimitive
        ?.long
        ?: throw IOException("Lesson-session payload is missing long '$key'.")

    private fun JsonObject.requiredBoolean(key: String): Boolean = this[key]
        ?.jsonPrimitive
        ?.booleanOrNull
        ?: throw IOException("Lesson-session payload is missing boolean '$key'.")

    private inline fun <reified T : Enum<T>> JsonObject.requiredEnum(key: String): T {
        val raw = requiredString(key)
        return enumValues<T>().firstOrNull { it.name == raw }
            ?: throw IOException("Lesson-session payload has unknown $key '$raw'.")
    }

    private inline fun <reified T : Enum<T>> JsonObject.optionalEnum(key: String): T? {
        val raw = optionalString(key) ?: return null
        return enumValues<T>().firstOrNull { it.name == raw }
            ?: throw IOException("Lesson-session payload has unknown $key '$raw'.")
    }

    private fun kotlinx.serialization.json.JsonObjectBuilder.putNullableString(key: String, value: String?) {
        if (value == null) put(key, JsonNull) else put(key, JsonPrimitive(value))
    }

    private fun sha256(bytes: ByteArray): ByteArray = MessageDigest.getInstance("SHA-256").digest(bytes)

    private companion object {
        const val MAGIC = 0x4B44534C // KDSL
        const val ENVELOPE_VERSION = 1
        const val SHA_256_BYTES = 32
        const val MAX_PAYLOAD_BYTES = 256 * 1024
    }
}
