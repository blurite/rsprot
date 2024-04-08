package net.rsprot.protocol.game.outgoing.map

import net.rsprot.crypto.util.XteaKey
import net.rsprot.protocol.message.OutgoingMessage

public sealed interface StaticRebuildMessage : OutgoingMessage {
    public val zoneX: Int
    public val zoneZ: Int
    public val keys: List<XteaKey>
}
