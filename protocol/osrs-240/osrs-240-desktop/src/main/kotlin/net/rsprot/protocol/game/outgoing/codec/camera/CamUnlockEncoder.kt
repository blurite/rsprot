package net.rsprot.protocol.game.outgoing.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.CamUnlock
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class CamUnlockEncoder : MessageEncoder<CamUnlock> {
    override val prot: ServerProt = GameServerProt.CAM_UNLOCK

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: CamUnlock,
    ) {
        buffer.p1Alt1(if (message.unlock) 1 else 0)
    }
}
