package net.rsprot.protocol.game.outgoing.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.CamLookAtCycles
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class CamLookAtCyclesEncoder : MessageEncoder<CamLookAtCycles> {
    override val prot: ServerProt = GameServerProt.CAM_LOOKAT_CYCLES

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: CamLookAtCycles,
    ) {
        buffer.p2(message.cycles)
        buffer.p1Alt3(if (message.heightRelative) 1 else 0)
        buffer.p2(message.x)
        buffer.p1Alt2(message.easing.id)
        buffer.p2(message.z)
        buffer.p2Alt2(message.height)
    }
}
