package net.rsprot.protocol.game.outgoing.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.CamReset
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class CamResetEncoder : MessageEncoder<CamReset> {
    override val prot: ServerProt = GameServerProt.CAM_RESET

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: CamReset,
    ) {
    }
}
