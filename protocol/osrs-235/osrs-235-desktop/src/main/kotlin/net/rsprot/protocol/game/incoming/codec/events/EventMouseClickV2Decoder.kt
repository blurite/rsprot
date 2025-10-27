package net.rsprot.protocol.game.incoming.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.events.EventMouseClickV2
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class EventMouseClickV2Decoder : MessageDecoder<EventMouseClickV2> {
    override val prot: ClientProt = GameClientProt.EVENT_MOUSE_CLICK_V2

    override fun decode(buffer: JagByteBuf): EventMouseClickV2 {
        val x = buffer.g2()
        val y = buffer.g2()
        val code = buffer.g1Alt1()
        val packed = buffer.g2Alt2()
        val rightClick = packed and 0x1 != 0
        val lastTransmittedMouseClick = packed ushr 1
        return EventMouseClickV2(
            lastTransmittedMouseClick,
            code,
            rightClick,
            x,
            y,
        )
    }
}
