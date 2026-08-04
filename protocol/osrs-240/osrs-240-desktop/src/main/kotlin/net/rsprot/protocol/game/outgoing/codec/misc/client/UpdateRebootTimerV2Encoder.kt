package net.rsprot.protocol.game.outgoing.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.client.UpdateRebootTimerV2
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class UpdateRebootTimerV2Encoder : MessageEncoder<UpdateRebootTimerV2> {
    override val prot: ServerProt = GameServerProt.UPDATE_REBOOT_TIMER_V2

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: UpdateRebootTimerV2,
    ) {
        when (val mes = message.messageType) {
            UpdateRebootTimerV2.ClearUpdateMessage -> {
                buffer.pjstr(CANCEL)
            }
            UpdateRebootTimerV2.IgnoreUpdateMessage -> {
                buffer.pjstr("")
            }
            is UpdateRebootTimerV2.SetUpdateMessage -> {
                buffer.pjstr(mes.message)
            }
        }
        buffer.p2Alt2(message.gameCycles)
    }

    private companion object {
        private const val CANCEL: String = "\u0018"
    }
}
