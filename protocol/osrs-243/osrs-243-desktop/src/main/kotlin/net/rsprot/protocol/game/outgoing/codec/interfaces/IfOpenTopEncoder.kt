package net.rsprot.protocol.game.outgoing.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfOpenTop
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class IfOpenTopEncoder : MessageEncoder<IfOpenTop> {
    override val prot: ServerProt = GameServerProt.IF_OPENTOP

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: IfOpenTop,
    ) {
        buffer.p2Alt3(message.interfaceId)
    }
}
