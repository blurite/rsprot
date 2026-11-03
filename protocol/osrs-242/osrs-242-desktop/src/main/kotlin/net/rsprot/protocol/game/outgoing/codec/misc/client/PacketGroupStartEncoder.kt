package net.rsprot.protocol.game.outgoing.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.client.PacketGroupStart
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class PacketGroupStartEncoder : MessageEncoder<PacketGroupStart> {
    override val prot: ServerProt = GameServerProt.PACKET_GROUP_START

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: PacketGroupStart,
    ) {
        // The payload will be overwritten as part of the Netty handler
        buffer.p2(0)
    }
}
