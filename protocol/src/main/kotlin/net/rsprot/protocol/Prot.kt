package net.rsprot.protocol

public sealed interface Prot {
    public val opcode: Int
    public val size: Int

    public companion object {
        public const val VAR_BYTE: Int = -1
        public const val VAR_SHORT: Int = -2
    }
}
