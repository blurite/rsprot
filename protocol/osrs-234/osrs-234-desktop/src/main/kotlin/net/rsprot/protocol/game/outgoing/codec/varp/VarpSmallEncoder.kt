package net.rsprot.protocol.game.outgoing.codec.varp

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.varp.VarpSmall
import net.rsprot.protocol.message.codec.MessageEncoder

public class VarpSmallEncoder : MessageEncoder<VarpSmall> {
    override val prot: ServerProt = GameServerProt.VARP_SMALL

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: VarpSmall,
    ) {
        buffer.p1Alt2(message.value)
        buffer.p2Alt1(message.id)
    }
}
