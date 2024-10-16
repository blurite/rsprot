package net.rsprot.protocol.game.outgoing.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.CamTargetV2
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class CamTargetV2Encoder : MessageEncoder<CamTargetV2> {
    override val prot: ServerProt = GameServerProt.CAM_TARGET_V2

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: CamTargetV2,
    ) {
        when (val type = message.type) {
            is CamTargetV2.PlayerCamTarget -> {
                buffer.p1(0)
                buffer.p2(type.index)
                buffer.p2(-1)
            }
            is CamTargetV2.NpcCamTarget -> {
                buffer.p1(1)
                buffer.p2(type.index)
                buffer.p2(-1)
            }
            is CamTargetV2.WorldEntityTarget -> {
                buffer.p1(2)
                buffer.p2(type.index)
                buffer.p2(type.cameraLockedPlayerIndex)
            }
        }
    }
}
