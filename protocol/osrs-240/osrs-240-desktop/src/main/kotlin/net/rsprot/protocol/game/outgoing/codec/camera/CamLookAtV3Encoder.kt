package net.rsprot.protocol.game.outgoing.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.CamLookAtV3
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class CamLookAtV3Encoder : MessageEncoder<CamLookAtV3> {
    override val prot: ServerProt = GameServerProt.CAM_LOOKAT_V3

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: CamLookAtV3,
    ) {
        buffer.p1Alt2(message.rate)
        buffer.p2(message.height)
        buffer.p2Alt3(message.x)
        buffer.p2(message.z)
        buffer.p1Alt2(message.rate2)
        buffer.p1Alt3(if (message.heightRelative) 1 else 0)
    }
}
