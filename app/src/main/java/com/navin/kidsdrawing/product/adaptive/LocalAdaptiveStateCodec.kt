package com.navin.kidsdrawing.product.adaptive

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put

class UnsupportedAdaptiveStateFormatException(
    val formatVersion: Int,
) : IOException("Unsupported adaptive-state formatVersion $formatVersion.")

/** Stable JSON codec for the bounded local adaptive projection. */
class LocalAdaptiveStateCodec(
    private val json: Json = Json,
) {
    fun encode(state: LocalAdaptiveState, output: OutputStream) {
        val payload = state.toJsonObject().toString().toByteArray(Charsets.UTF_8)
        require(payload.size <= MAX_PAYLOAD_BYTES) { "Adaptive state is unexpectedly large." }
        output.write(payload)
        output.flush()
    }

    @Throws(IOException::class)
    fun decode(input: InputStream): LocalAdaptiveState {
        val bytes = input.readBytes()
        if (bytes.isEmpty() || bytes.size > MAX_PAYLOAD_BYTES) {
            throw IOException("Invalid adaptive-state payload size ${bytes.size}.")
        }
        val root = runCatching {
            json.parseToJsonElement(bytes.toString(Charsets.UTF_8)).jsonObject
        }.getOrElse { failure ->
            throw IOException("Adaptive state is not valid JSON.", failure)
        }
        val formatVersion = root.requiredInt("formatVersion")
        if (formatVersion != LocalAdaptiveState.CURRENT_FORMAT_VERSION) {
            throw UnsupportedAdaptiveStateFormatException(formatVersion)
        }
        return try {
            LocalAdaptiveState(
                formatVersion = formatVersion,
                revision = root.requiredLong("revision"),
                completedLessons = root.requiredArray("completedLessons").map { element ->
                    element.jsonObject.toIdentity()
                },
                skillExposureCounts = root.requiredCounterMap("skillExposureCounts"),
                recentCompletions = root.requiredArray("recentCompletions").map { element ->
                    element.jsonObject.toIdentity()
                },
                helpRequestCounts = root.requiredCounterMap("helpRequestCounts"),
                processedEventKeys = root.requiredArray("processedEventKeys").map { element ->
                    element.jsonPrimitive.content
                },
            )
        } catch (failure: IllegalArgumentException) {
            throw IOException("Adaptive state contains invalid semantic values.", failure)
        }
    }

    private fun LocalAdaptiveState.toJsonObject(): JsonObject = buildJsonObject {
        put("formatVersion", formatVersion)
        put("revision", revision)
        put("completedLessons", identitiesJson(completedLessons))
        put("skillExposureCounts", counterMapJson(skillExposureCounts))
        put("recentCompletions", identitiesJson(recentCompletions))
        put("helpRequestCounts", counterMapJson(helpRequestCounts))
        put("processedEventKeys", buildJsonArray {
            processedEventKeys.forEach { add(JsonPrimitive(it)) }
        })
    }

    private fun identitiesJson(identities: List<AdaptiveLessonIdentity>): JsonArray = buildJsonArray {
        identities.forEach { identity ->
            add(buildJsonObject {
                put("lessonId", identity.lessonId)
                put("revision", identity.revision)
            })
        }
    }

    private fun counterMapJson(counters: Map<String, Int>): JsonObject = buildJsonObject {
        counters.toSortedMap().forEach { (key, value) -> put(key, value) }
    }

    private fun JsonObject.toIdentity(): AdaptiveLessonIdentity = AdaptiveLessonIdentity(
        lessonId = requiredString("lessonId"),
        revision = requiredInt("revision"),
    )

    private fun JsonObject.requiredCounterMap(key: String): Map<String, Int> =
        this[key]?.jsonObject
            ?.entries
            ?.sortedBy { it.key }
            ?.associate { (counterKey, value) -> counterKey to value.jsonPrimitive.int }
            ?: throw IOException("Adaptive state is missing object '$key'.")

    private fun JsonObject.requiredArray(key: String): JsonArray = this[key]?.jsonArray
        ?: throw IOException("Adaptive state is missing array '$key'.")

    private fun JsonObject.requiredString(key: String): String = this[key]
        ?.jsonPrimitive
        ?.content
        ?.takeIf(String::isNotBlank)
        ?: throw IOException("Adaptive state is missing non-blank '$key'.")

    private fun JsonObject.requiredInt(key: String): Int = this[key]
        ?.jsonPrimitive
        ?.int
        ?: throw IOException("Adaptive state is missing integer '$key'.")

    private fun JsonObject.requiredLong(key: String): Long = this[key]
        ?.jsonPrimitive
        ?.long
        ?: throw IOException("Adaptive state is missing long '$key'.")

    private companion object {
        const val MAX_PAYLOAD_BYTES = 128 * 1024
    }
}
