package net.rsprot.protocol.api.decoder

/**
 * An enum containing the possible states for decoding messages from client.
 */
internal enum class DecoderState {
    READ_OPCODE,
    READ_LENGTH,
    READ_PAYLOAD,
}
