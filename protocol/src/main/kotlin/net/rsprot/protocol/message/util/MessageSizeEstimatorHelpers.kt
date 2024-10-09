package net.rsprot.protocol.message.util

import net.rsprot.protocol.message.OutgoingMessage

public fun OutgoingMessage.estimateTextSize(text: String): Int {
    return text.length + Byte.SIZE_BYTES
}

public fun OutgoingMessage.estimateHuffmanEncodedTextSize(text: String): Int {
    val worstCaseBitsCount = 22 * text.length
    val worstCaseByteCount = (worstCaseBitsCount + 7) ushr 3
    val headerSize = if (text.length >= 0x80) Short.SIZE_BYTES else Byte.SIZE_BYTES
    return headerSize + worstCaseByteCount
}
