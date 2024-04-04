package net.rsprot.protocol.game.incoming.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.events.EventKeyboardMessage
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools

public class EventKeyboardDecoder : MessageDecoder<EventKeyboardMessage> {
    override val prot: ClientProt = GameClientProt.EVENT_KEYBOARD

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): EventKeyboardMessage {
        val count = buffer.readableBytes() / 4
        val keys = ByteArray(count)
        var lastTransmittedKeyPress: Int = -1
        for (i in 0..<count) {
            val delta = buffer.g3()
            val key = buffer.g1Alt3()
            if (i == 0) {
                lastTransmittedKeyPress = delta
            }
            keys[i] = key.toByte()
        }
        return EventKeyboardMessage(
            lastTransmittedKeyPress,
            EventKeyboardMessage.KeySequence(keys),
        )
    }
}
