package net.rsprot.protocol.game.outgoing.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.CamMoveToCyclesV2
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class CamMoveToCyclesV2Encoder : MessageEncoder<CamMoveToCyclesV2> {
    override val prot: ServerProt = GameServerProt.CAM_MOVETO_CYCLES_V2

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: CamMoveToCyclesV2,
    ) {
        buffer.p2Alt1(message.x)
        buffer.p1(if (message.ignoreTerrain) 1 else 0)
        buffer.p1(message.easing.id)
        buffer.p2Alt1(message.cycles)
        buffer.p2Alt3(message.z)
        buffer.p2Alt2(message.height)
    }
}
