package com.navin.kidsdrawing.lesson.authoring

import com.navin.kidsdrawing.lesson.content.CatalogIndexV2EntrySource
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class EvidenceDocument(
    val schemaVersion: String,
    val validationState: String,
    val lessonId: String? = null,
    val revision: Int? = null,
    val lessonStatus: String? = null,
    val manifest: List<String>,
    val packageLoaderPassed: Boolean,
    val drawingStepCount: Int? = null,
    val strokeCount: Int? = null,
    val guideCount: Int? = null,
    val coloringRegionCount: Int? = null,
    val supportedModes: List<String>,
    val capabilities: List<EvidenceCapability>,
    val helpReady: Boolean? = null,
    val traceReady: Boolean? = null,
    val coloringCapability: String? = null,
    val reservedFields: List<EvidenceReservedField>,
    val gates: List<EvidenceGate>,
    val qualityWarningCount: Int,
    val projectedIndexEntryCount: Int? = null,
    val projectedIndexEntry: CatalogIndexV2EntrySource? = null,
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
private data class EvidenceGate(
    val gate: String,
    val status: String,
    val diagnostics: List<EvidenceGateDiagnostic>,
)

@Serializable
private data class EvidenceGateDiagnostic(
    val severity: String,
    val sourceCode: String,
    val path: String,
    val message: String,
)

@Serializable
private data class EvidenceDiagnostic(
    val code: String,
    val path: String,
    val message: String,
)

/** Machine-readable deterministic validation evidence for CI/review tooling. */
object ContentStudioEvidenceJson {
    const val SCHEMA_VERSION = "1.1"

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
            lessonStatus = evidence.lessonStatus?.name,
            manifest = evidence.manifest.sorted(),
            packageLoaderPassed = evidence.packageLoaderPassed,
            drawingStepCount = evidence.drawingStepCount,
            strokeCount = evidence.strokeCount,
            guideCount = evidence.guideCount,
            coloringRegionCount = evidence.coloringRegionCount,
            supportedModes = evidence.supportedModes.map { it.name },
            capabilities = evidence.capabilityStates
                .sortedBy { it.capability.name }
                .map { EvidenceCapability(it.capability.name, it.status.name) },
            helpReady = evidence.helpReady,
            traceReady = evidence.traceReady,
            coloringCapability = evidence.coloringCapability?.name,
            reservedFields = evidence.reservedFieldUsage
                .sortedBy { it.field.name }
                .map { usage ->
                    EvidenceReservedField(
                        field = usage.field.name,
                        disposition = usage.disposition.name,
                        paths = usage.paths.sorted(),
                    )
                },
            gates = evidence.gateEvidence.map { gate ->
                EvidenceGate(
                    gate = gate.gate.name,
                    status = gate.status.name,
                    diagnostics = gate.diagnostics
                        .sortedBy { "${it.severity}:${it.sourceCode}:${it.path}:${it.message}" }
                        .map { diagnostic ->
                            EvidenceGateDiagnostic(
                                severity = diagnostic.severity.name,
                                sourceCode = diagnostic.sourceCode,
                                path = diagnostic.path,
                                message = diagnostic.message,
                            )
                        },
                )
            },
            qualityWarningCount = evidence.qualityWarningCount,
            projectedIndexEntryCount = evidence.projectedIndexEntryCount,
            projectedIndexEntry = evidence.projectedIndexEntry,
            diagnostics = diagnostics
                .sortedBy { "${it.code}:${it.path}:${it.message}" }
                .map { EvidenceDiagnostic(it.code.name, it.path, it.message) },
        )
        return json.encodeToString(document) + "\n"
    }
}
