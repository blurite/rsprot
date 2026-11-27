package net.rsprot.protocol.game.outgoing.map

import net.rsprot.protocol.message.OutgoingGameMessage

public sealed interface StaticRebuildMessageV2 : OutgoingGameMessage {
    public val zoneX: Int
    public val zoneZ: Int
    public val worldArea: Int
}
