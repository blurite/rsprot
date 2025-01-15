package net.rsprot.protocol.game.outgoing.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetRotateSpeed
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.util.pCombinedId

public class IfSetRotateSpeedEncoder : MessageEncoder<IfSetRotateSpeed> {
    override val prot: ServerProt = GameServerProt.IF_SETROTATESPEED

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: IfSetRotateSpeed,
    ) {
        // Note: xSpeed is shifted left by 16 bits (xSpeed << 16) in the client
        buffer.pCombinedId(message.combinedId)
        buffer.p2(message.ySpeed)
        buffer.p2Alt1(message.xSpeed)
    }
}
