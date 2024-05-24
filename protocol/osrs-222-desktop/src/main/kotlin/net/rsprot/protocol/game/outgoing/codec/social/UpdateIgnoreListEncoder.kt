package net.rsprot.protocol.game.outgoing.codec.social

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.social.UpdateIgnoreList
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class UpdateIgnoreListEncoder : MessageEncoder<UpdateIgnoreList> {
    override val prot: ServerProt = GameServerProt.UPDATE_IGNORELIST

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: UpdateIgnoreList,
    ) {
        for (ignore in message.ignores) {
            when (ignore) {
                is UpdateIgnoreList.AddedIgnoredEntry -> {
                    buffer.p1(if (ignore.added) 0x1 else 0)
                    buffer.pjstr(ignore.name)
                    buffer.pjstr(ignore.previousName ?: "")
                    buffer.pjstr(ignore.note)
                }
                is UpdateIgnoreList.RemovedIgnoredEntry -> {
                    buffer.p1(0x4)
                    buffer.pjstr(ignore.name)
                }
            }
        }
    }
}
