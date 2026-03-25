package net.rsprot.protocol.game.outgoing.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.CamMode
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class CamModeEncoder : MessageEncoder<CamMode> {
    override val prot: ServerProt = GameServerProt.CAM_MODE

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: CamMode,
    ) {
        buffer.p1(message.mode)
    }
}
