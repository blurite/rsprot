package net.rsprot.protocol.game.outgoing.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.CamLookAtEasedCoord
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class CamLookAtEasedCoordEncoder : MessageEncoder<CamLookAtEasedCoord> {
    override val prot: ServerProt = GameServerProt.CAM_LOOKAT_EASED_COORD

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: CamLookAtEasedCoord,
    ) {
        buffer.p1(message.destinationXInBuildArea)
        buffer.p1(message.destinationZInBuildArea)
        buffer.p2(message.height)
        buffer.p2(message.cycles)
        buffer.p1(message.easing.id)
    }
}
