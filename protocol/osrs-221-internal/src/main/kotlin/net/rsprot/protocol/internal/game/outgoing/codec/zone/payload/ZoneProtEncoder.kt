package net.rsprot.protocol.internal.game.outgoing.codec.zone.payload

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.message.codec.MessageEncoder

/**
 * Zone prot encoder is an extension on message encoders, with the intent being
 * that this encoder can be passed on-to
 * [net.rsprot.protocol.game.outgoing.codec.zone.header.DesktopUpdateZonePartialEnclosedEncoder],
 * as that packet combines multiple zone payloads into a single packet.
 */
public interface ZoneProtEncoder<T : ZoneProt> : MessageEncoder<T> {
    public fun encode(
        buffer: JagByteBuf,
        message: T,
    )

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: T,
    ) {
        encode(buffer, message)
    }
}
