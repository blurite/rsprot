package net.rsprot.protocol.game.incoming.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.events.EventAppletFocus
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools

@Consistent
public class EventAppletFocusDecoder : MessageDecoder<EventAppletFocus> {
    override val prot: ClientProt = GameClientProt.EVENT_APPLET_FOCUS

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): EventAppletFocus {
        val inFocus = buffer.g1() == 1
        return EventAppletFocus(inFocus)
    }
}
