package net.rsprot.protocol.game.incoming.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.events.EventCameraPosition
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class EventCameraPositionDecoder : MessageDecoder<EventCameraPosition> {
    override val prot: ClientProt = GameClientProt.EVENT_CAMERA_POSITION

    override fun decode(buffer: JagByteBuf): EventCameraPosition {
        val angleX = buffer.g2Alt1()
        val angleY = buffer.g2Alt1()
        return EventCameraPosition(
            angleX,
            angleY,
        )
    }
}
