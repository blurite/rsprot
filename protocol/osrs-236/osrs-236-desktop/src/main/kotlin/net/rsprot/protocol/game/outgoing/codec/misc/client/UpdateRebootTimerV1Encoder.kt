package net.rsprot.protocol.game.outgoing.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.client.UpdateRebootTimerV1
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class UpdateRebootTimerV1Encoder : MessageEncoder<UpdateRebootTimerV1> {
    override val prot: ServerProt = GameServerProt.UPDATE_REBOOT_TIMER_V1

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: UpdateRebootTimerV1,
    ) {
        buffer.p2Alt3(message.gameCycles)
    }
}
