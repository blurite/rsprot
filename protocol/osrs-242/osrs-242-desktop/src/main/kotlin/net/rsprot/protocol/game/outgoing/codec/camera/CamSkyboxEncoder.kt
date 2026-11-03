package net.rsprot.protocol.game.outgoing.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.CamSkybox
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class CamSkyboxEncoder : MessageEncoder<CamSkybox> {
    override val prot: ServerProt = GameServerProt.CAM_SKYBOX

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: CamSkybox,
    ) {
        buffer.p4Alt3(message.model)
    }
}
