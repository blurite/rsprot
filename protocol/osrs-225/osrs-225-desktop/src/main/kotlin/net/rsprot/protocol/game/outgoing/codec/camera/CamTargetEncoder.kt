package net.rsprot.protocol.game.outgoing.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.CamTarget
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class CamTargetEncoder : MessageEncoder<CamTarget> {
    override val prot: ServerProt = GameServerProt.CAM_TARGET

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: CamTarget,
    ) {
        when (val type = message.type) {
            is CamTarget.PlayerCamTarget -> {
                buffer.p1(0)
                buffer.p2(type.index)
                buffer.p2(-1)
            }
            is CamTarget.NpcCamTarget -> {
                buffer.p1(1)
                buffer.p2(type.index)
                buffer.p2(-1)
            }
            is CamTarget.WorldEntityTarget -> {
                buffer.p1(2)
                buffer.p2(type.index)
                buffer.p2(type.cameraLockedPlayerIndex)
            }
        }
    }
}
