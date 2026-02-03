package net.rsprot.protocol.game.outgoing.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.CamLookAtV2
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class CamLookAtV2Encoder : MessageEncoder<CamLookAtV2> {
    override val prot: ServerProt = GameServerProt.CAM_LOOKAT_V2

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: CamLookAtV2,
    ) {
        buffer.p2Alt2(message.z)
        buffer.p1Alt1(message.rate2)
        buffer.p1(message.rate)
        buffer.p2(message.x)
        buffer.p2(message.height)
    }
}
