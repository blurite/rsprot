package net.rsprot.protocol.game.incoming.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.events.EventMouseScrollMessage
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools

@Consistent
public class EventMouseScrollDecoder : MessageDecoder<EventMouseScrollMessage> {
    override val prot: ClientProt = GameClientProt.EVENT_MOUSE_SCROLL

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): EventMouseScrollMessage {
        val rotation = buffer.g2s()
        return EventMouseScrollMessage(rotation)
    }
}
