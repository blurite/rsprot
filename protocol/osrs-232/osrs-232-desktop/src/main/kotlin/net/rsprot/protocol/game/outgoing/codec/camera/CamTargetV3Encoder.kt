package net.rsprot.protocol.game.outgoing.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.CamTargetV3
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class CamTargetV3Encoder : MessageEncoder<CamTargetV3> {
    override val prot: ServerProt = GameServerProt.CAM_TARGET_V3

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: CamTargetV3,
    ) {
        when (val type = message.type) {
            is CamTargetV3.PlayerCamTarget -> {
                buffer.p1(0)
                buffer.p2(type.worldEntityIndex)
                buffer.p2(type.targetIndex)
            }
            is CamTargetV3.NpcCamTarget -> {
                buffer.p1(1)
                buffer.p2(type.worldEntityIndex)
                buffer.p2(type.targetIndex)
            }
            is CamTargetV3.WorldEntityTarget -> {
                buffer.p1(2)
                buffer.p2(-1)
                buffer.p2(type.targetIndex)
            }
        }
    }
}
