package net.rsprot.protocol.common.js5.incoming.prot

import net.rsprot.protocol.ClientProt

public enum class Js5ClientProt(
    override val opcode: Int,
    override val size: Int,
) : ClientProt {
    PREFETCH_REQUEST(Js5ClientProtId.PREFETCH_REQUEST, 4),
    URGENT_REQUEST(Js5ClientProtId.URGENT_REQUEST, 4),
    PRIORITY_CHANGE_HIGH(Js5ClientProtId.PRIORITY_CHANGE_HIGH, 4),
    PRIORITY_CHANGE_LOW(Js5ClientProtId.PRIORITY_CHANGE_LOW, 4),
    XOR_CHANGE(Js5ClientProtId.XOR_CHANGE, 4),
}
