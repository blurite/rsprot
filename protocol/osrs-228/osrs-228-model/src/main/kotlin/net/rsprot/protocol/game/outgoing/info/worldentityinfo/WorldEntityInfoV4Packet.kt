package net.rsprot.protocol.game.outgoing.info.worldentityinfo

import io.netty.buffer.ByteBuf
import io.netty.buffer.DefaultByteBufHolder
import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.ConsumableMessage
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * World entity info packet is used to update the coordinate, angle and move speed of all
 * the world entities near a player.
 */
public class WorldEntityInfoV4Packet(
    buffer: ByteBuf,
) : DefaultByteBufHolder(buffer),
    OutgoingGameMessage,
    ConsumableMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT

    private var consumed: Boolean = false

    override fun consume() {
        this.consumed = true
    }

    override fun isConsumed(): Boolean = this.consumed

    override fun toString(): String = "WorldEntityInfoV4Packet()"
}
