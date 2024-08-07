package net.rsprot.protocol.game.outgoing.codec.misc.client

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.client.SiteSettings
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class SiteSettingsEncoder : MessageEncoder<SiteSettings> {
    override val prot: ServerProt = GameServerProt.SITE_SETTINGS

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: SiteSettings,
    ) {
        buffer.pjstr(message.settings)
    }
}
