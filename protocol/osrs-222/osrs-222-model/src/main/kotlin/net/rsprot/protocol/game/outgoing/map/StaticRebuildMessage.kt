package net.rsprot.protocol.game.outgoing.map

import net.rsprot.crypto.xtea.XteaKey
import net.rsprot.protocol.message.OutgoingGameMessage

public sealed interface StaticRebuildMessage : OutgoingGameMessage {
    public val zoneX: Int
    public val zoneZ: Int
    public val keys: List<XteaKey>
}
