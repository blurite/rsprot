package net.rsprot.protocol.game.outgoing.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.CamMoveToV3
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class CamMoveToV3Encoder : MessageEncoder<CamMoveToV3> {
    override val prot: ServerProt = GameServerProt.CAM_MOVETO_V3

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: CamMoveToV3,
    ) {
        buffer.p2(message.z)
        buffer.p1(if (message.heightRelative) 1 else 0)
        buffer.p1(message.rate)
        buffer.p2Alt2(message.height)
        buffer.p2Alt1(message.x)
        buffer.p1Alt1(message.rate2)
    }
}
