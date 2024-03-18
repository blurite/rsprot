package net.rsprot.protocol

public sealed interface Prot {
    public val opcode: Int
    public val size: Int
}
