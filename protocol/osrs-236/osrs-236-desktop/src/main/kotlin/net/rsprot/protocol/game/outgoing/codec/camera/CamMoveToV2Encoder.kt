package net.rsprot.protocol.game.outgoing.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.CamMoveToV2
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class CamMoveToV2Encoder : MessageEncoder<CamMoveToV2> {
    override val prot: ServerProt = GameServerProt.CAM_MOVETO_V2

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: CamMoveToV2,
    ) {
        buffer.p2Alt3(message.z)
        buffer.p2(message.x)
        buffer.p2Alt1(message.height)
        buffer.p1(message.rate2)
        buffer.p1Alt1(message.rate)
    }
}
