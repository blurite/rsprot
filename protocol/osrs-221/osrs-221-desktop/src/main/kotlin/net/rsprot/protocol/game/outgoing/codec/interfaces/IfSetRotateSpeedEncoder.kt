package net.rsprot.protocol.game.outgoing.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetRotateSpeed
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class IfSetRotateSpeedEncoder : MessageEncoder<IfSetRotateSpeed> {
    override val prot: ServerProt = GameServerProt.IF_SETROTATESPEED

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: IfSetRotateSpeed,
    ) {
        buffer.p2Alt1(message.ySpeed)
        buffer.p2(message.xSpeed)
        buffer.p4Alt3(message.combinedId.combinedId)
    }
}
