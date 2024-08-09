package net.rsprot.protocol.game.incoming.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.events.EventNativeMouseClick
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class EventNativeMouseClickDecoder : MessageDecoder<EventNativeMouseClick> {
    override val prot: ClientProt = GameClientProt.EVENT_NATIVE_MOUSE_CLICK

    override fun decode(buffer: JagByteBuf): EventNativeMouseClick {
        val lastTransmittedMouseClick = buffer.g2Alt2()
        val packedCoord = buffer.g4Alt1()
        val code = buffer.g1()
        return EventNativeMouseClick(
            lastTransmittedMouseClick,
            code,
            packedCoord and 0xFFFF,
            packedCoord ushr 16 and 0xFFFF,
        )
    }
}
