package net.rsprot.protocol.game.incoming.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.events.EventMouseClickV1
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class EventMouseClickV1Decoder : MessageDecoder<EventMouseClickV1> {
    override val prot: ClientProt = GameClientProt.EVENT_MOUSE_CLICK_V1

    override fun decode(buffer: JagByteBuf): EventMouseClickV1 {
        val packed = buffer.g2()
        val rightClick = packed and 0x1 != 0
        val lastTransmittedMouseClick = packed ushr 1
        val x = buffer.g2()
        val y = buffer.g2()
        return EventMouseClickV1(
            lastTransmittedMouseClick,
            rightClick,
            x,
            y,
        )
    }
}
