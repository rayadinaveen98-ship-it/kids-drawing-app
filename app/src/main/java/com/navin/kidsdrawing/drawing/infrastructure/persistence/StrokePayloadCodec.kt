package com.navin.kidsdrawing.drawing.infrastructure.persistence

import com.navin.kidsdrawing.drawing.domain.InkStrokeRecord
import com.navin.kidsdrawing.drawing.domain.PointerTool
import com.navin.kidsdrawing.drawing.domain.StrokePoint
import java.io.InputStream
import java.io.OutputStream

data class DecodedStrokePayload(
    val tool: PointerTool,
    val points: List<StrokePoint>,
)

/**
 * App-owned persistence boundary for stroke sample payloads.
 *
 * Production uses AndroidX Ink's stable stream codec. Host JVM tests may inject a pure codec so
 * envelope/store correctness is testable without loading Ink's Android-native implementation.
 */
interface StrokePayloadCodec {
    fun encode(record: InkStrokeRecord, output: OutputStream)
    fun decode(input: InputStream): DecodedStrokePayload
}
