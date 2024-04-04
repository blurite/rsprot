package net.rsprot.protocol.game.incoming.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.events.EventMouseClickMessage
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools

@Consistent
public class EventMouseClickDecoder : MessageDecoder<EventMouseClickMessage> {
    override val prot: ClientProt = GameClientProt.EVENT_MOUSE_CLICK

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): EventMouseClickMessage {
        val packed = buffer.g2()
        val rightClick = packed and 0x1 != 0
        val lastTransmittedMouseClick = packed ushr 1
        val x = buffer.g2()
        val y = buffer.g2()
        return EventMouseClickMessage(
            lastTransmittedMouseClick,
            rightClick,
            x,
            y,
        )
    }
}
