package net.rsprot.protocol.game.outgoing.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.CamTargetV1
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class CamTargetV1Encoder : MessageEncoder<CamTargetV1> {
    override val prot: ServerProt = GameServerProt.CAM_TARGET_V1

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: CamTargetV1,
    ) {
        when (val type = message.type) {
            is CamTargetV1.PlayerCamTarget -> {
                buffer.p1(0)
                buffer.p2(type.index)
            }
            is CamTargetV1.NpcCamTarget -> {
                buffer.p1(1)
                buffer.p2(type.index)
            }
            is CamTargetV1.WorldEntityTarget -> {
                buffer.p1(2)
                buffer.p2(type.index)
            }
        }
    }
}
