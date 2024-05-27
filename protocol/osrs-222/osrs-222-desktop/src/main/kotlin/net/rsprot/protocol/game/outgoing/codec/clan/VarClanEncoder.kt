package net.rsprot.protocol.game.outgoing.codec.clan

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.clan.VarClan
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class VarClanEncoder : MessageEncoder<VarClan> {
    override val prot: ServerProt = GameServerProt.VARCLAN

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: VarClan,
    ) {
        buffer.p2(message.id)
        // Note that there is another clause for 'serializable' types,
        // however none currently exist.
        when (val value = message.value) {
            is VarClan.VarClanIntData -> {
                buffer.p4(value.value)
            }
            is VarClan.VarClanLongData -> {
                buffer.p8(value.value)
            }
            is VarClan.VarClanStringData -> {
                buffer.pjstr2(value.value)
            }
        }
    }
}
