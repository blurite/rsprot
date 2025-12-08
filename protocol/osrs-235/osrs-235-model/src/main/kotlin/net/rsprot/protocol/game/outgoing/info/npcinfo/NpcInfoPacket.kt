package net.rsprot.protocol.game.outgoing.info.npcinfo

import io.netty.buffer.ByteBuf
import io.netty.buffer.DefaultByteBufHolder
import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.ConsumableMessage
import net.rsprot.protocol.message.OutgoingGameMessage

public abstract class NpcInfoPacket(
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
}
