package net.rsprot.protocol.game.incoming.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.events.EventCameraPositionMessage
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class EventCameraPositionDecoder : MessageDecoder<EventCameraPositionMessage> {
    override val prot: ClientProt = GameClientProt.EVENT_CAMERA_POSITION

    override fun decode(buffer: JagByteBuf): EventCameraPositionMessage {
        val angleX = buffer.g2Alt1()
        val angleY = buffer.g2Alt1()
        return EventCameraPositionMessage(
            angleX,
            angleY,
        )
    }
}
