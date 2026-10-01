package net.rsprot.protocol.game.outgoing.codec.varp

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.varp.VarpLong
import net.rsprot.protocol.message.codec.MessageEncoder

public class VarpLongEncoder : MessageEncoder<VarpLong> {
    override val prot: ServerProt = GameServerProt.VARP_LONG

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: VarpLong,
    ) {
        buffer.p4Alt3((message.value ushr 32).toInt())
        buffer.p4Alt3(message.value.toInt())
        buffer.p2(message.id)
    }
}
