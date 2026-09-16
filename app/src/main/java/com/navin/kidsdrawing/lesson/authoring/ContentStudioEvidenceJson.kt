package com.navin.kidsdrawing.lesson.authoring

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class EvidenceDocument(
    val schemaVersion: String,
    val validationState: String,
    val lessonId: String? = null,
    val revision: Int? = null,
    val manifest: List<String>,
    val capabilities: List<EvidenceCapability>,
    val reservedFields: List<EvidenceReservedField>,
    val qualityWarningCount: Int,
    val projectedIndexEntryCount: Int? = null,
    val diagnostics: List<EvidenceDiagnostic>,
)

@Serializable
private data class EvidenceCapability(
    val capability: String,
    val status: String,
)

@Serializable
private data class EvidenceReservedField(
    val field: String,
    val disposition: String,
    val paths: List<String>,
)

@Serializable
private data class EvidenceDiagnostic(
    val code: String,
    val path: String,
    val message: String,
)

/** Machine-readable deterministic validation evidence for CI/review tooling. */
object ContentStudioEvidenceJson {
    const val SCHEMA_VERSION = "1.0"

    private val json = Json {
        encodeDefaults = true
        explicitNulls = false
        prettyPrint = true
    }

    fun render(result: ContentStudioValidationResult): String {
        val evidence = when (result) {
            is ContentStudioValidationResult.Ready -> result.evidence
            is ContentStudioValidationResult.Blocked -> result.evidence
        }
        val diagnostics = when (result) {
            is ContentStudioValidationResult.Ready -> emptyList()
            is ContentStudioValidationResult.Blocked -> result.diagnostics
        }
        val document = EvidenceDocument(
            schemaVersion = SCHEMA_VERSION,
            validationState = when (result) {
                is ContentStudioValidationResult.Ready -> "ready"
                is ContentStudioValidationResult.Blocked -> "blocked"
            },
            lessonId = evidence.lessonId,
            revision = evidence.revision,
            manifest = evidence.manifest.sorted(),
            capabilities = evidence.capabilityStates
                .sortedBy { it.capability.name }
                .map { EvidenceCapability(it.capability.name, it.status.name) },
            reservedFields = evidence.reservedFieldUsage
                .sortedBy { it.field.name }
                .map { usage ->
                    EvidenceReservedField(
                        field = usage.field.name,
                        disposition = usage.disposition.name,
                        paths = usage.paths.sorted(),
                    )
                },
            qualityWarningCount = evidence.qualityWarningCount,
            projectedIndexEntryCount = evidence.projectedIndexEntryCount,
            diagnostics = diagnostics
                .sortedBy { "${it.code}:${it.path}:${it.message}" }
                .map { EvidenceDiagnostic(it.code.name, it.path, it.message) },
        )
        return json.encodeToString(document) + "\n"
    }
}
