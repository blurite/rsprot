package net.rsprot.protocol.game.outgoing.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.CamLookAtEasedCoordV2
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class CamLookAtEasedCoordV2Encoder : MessageEncoder<CamLookAtEasedCoordV2> {
    override val prot: ServerProt = GameServerProt.CAM_LOOKAT_EASED_COORD_V2

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: CamLookAtEasedCoordV2,
    ) {
        buffer.p2Alt1(message.height)
        buffer.p2Alt3(message.z)
        buffer.p2Alt2(message.x)
        buffer.p1Alt3(message.easing.id)
        buffer.p2(message.cycles)
    }
}
