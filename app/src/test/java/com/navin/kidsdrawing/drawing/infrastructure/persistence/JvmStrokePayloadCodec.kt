package com.navin.kidsdrawing.drawing.infrastructure.persistence

import com.navin.kidsdrawing.drawing.domain.InkStrokeRecord
import com.navin.kidsdrawing.drawing.domain.PointerTool
import com.navin.kidsdrawing.drawing.domain.StrokePoint
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream

/** Test-only codec for exercising the app-owned envelope/store on the host JVM without Ink JNI. */
internal object JvmStrokePayloadCodec : StrokePayloadCodec {
    override fun encode(record: InkStrokeRecord, output: OutputStream) {
        val data = DataOutputStream(output)
        data.writeByte(toolTag(record.tool))
        data.writeInt(record.points.size)
        record.points.forEach { point ->
            data.writeFloat(point.x)
            data.writeFloat(point.y)
            data.writeLong(point.elapsedTimeMillis)
            data.writeFloat(point.pressure)
            writeNullableFloat(data, point.tiltRadians)
            writeNullableFloat(data, point.orientationRadians)
        }
        data.flush()
    }

    override fun decode(input: InputStream): DecodedStrokePayload {
        val data = DataInputStream(input)
        val tool = toolFromTag(data.readUnsignedByte())
        val count = data.readInt()
        require(count > 0) { "Test stroke payload cannot be empty." }
        return DecodedStrokePayload(
            tool = tool,
            points = List(count) {
                StrokePoint(
                    x = data.readFloat(),
                    y = data.readFloat(),
                    elapsedTimeMillis = data.readLong(),
                    pressure = data.readFloat(),
                    tiltRadians = readNullableFloat(data),
                    orientationRadians = readNullableFloat(data),
                )
            },
        )
    }

    private fun writeNullableFloat(data: DataOutputStream, value: Float?) {
        data.writeBoolean(value != null)
        if (value != null) data.writeFloat(value)
    }

    private fun readNullableFloat(data: DataInputStream): Float? =
        if (data.readBoolean()) data.readFloat() else null

    private fun toolTag(tool: PointerTool): Int = when (tool) {
        PointerTool.FINGER -> 1
        PointerTool.STYLUS,
        PointerTool.STYLUS_ERASER,
        -> 2
        PointerTool.UNKNOWN -> 3
    }

    private fun toolFromTag(tag: Int): PointerTool = when (tag) {
        1 -> PointerTool.FINGER
        2 -> PointerTool.STYLUS
        3 -> PointerTool.UNKNOWN
        else -> error("Unknown test payload tool tag: $tag")
    }
}
