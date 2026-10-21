package net.rsprot.protocol.common.js5.incoming.prot

import net.rsprot.protocol.ClientProt

public enum class Js5ClientProt(
    override val opcode: Int,
    override val size: Int,
) : ClientProt {
    PREFETCH_REQUEST(Js5ClientProtId.PREFETCH_REQUEST, 3),
    URGENT_REQUEST(Js5ClientProtId.URGENT_REQUEST, 3),
    PRIORITY_CHANGE_HIGH(Js5ClientProtId.PRIORITY_CHANGE_HIGH, 3),
    PRIORITY_CHANGE_LOW(Js5ClientProtId.PRIORITY_CHANGE_LOW, 3),
    XOR_CHANGE(Js5ClientProtId.XOR_CHANGE, 3),
}
