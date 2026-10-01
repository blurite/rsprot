package net.rsprot.protocol.game.outgoing.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.CamTargetV4
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class CamTargetV4Encoder : MessageEncoder<CamTargetV4> {
    override val prot: ServerProt = GameServerProt.CAM_TARGET_V4

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: CamTargetV4,
    ) {
        when (val type = message.type) {
            is CamTargetV4.PlayerCamTarget -> {
                buffer.p1(0)
                buffer.p4Alt1(type.index)
            }
            is CamTargetV4.NpcCamTarget -> {
                buffer.p1(1)
                buffer.p4Alt1(type.index)
            }
            is CamTargetV4.WorldEntityTarget -> {
                buffer.p1(2)
                buffer.p4Alt1(type.index)
            }
            is CamTargetV4.CoordGridTarget -> {
                buffer.p1(3)
                buffer.p4Alt1(type.coordGrid.packed)
            }
        }
    }
}
