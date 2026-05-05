package net.rsprot.protocol.common.js5.outgoing.prot

import net.rsprot.protocol.ServerProt

public enum class Js5ServerProt(
    override val opcode: Int,
    override val size: Int,
) : ServerProt {
    // Js5 responses have no actual opcode,
    // but we do need to have something to identify it by within the lib
    JS5_GROUP_RESPONSE(0, -2),
}
