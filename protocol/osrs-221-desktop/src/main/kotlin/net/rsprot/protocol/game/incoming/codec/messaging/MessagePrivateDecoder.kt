package net.rsprot.protocol.game.incoming.codec.messaging

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.messaging.MessagePrivateMessage
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools

@Consistent
public class MessagePrivateDecoder : MessageDecoder<MessagePrivateMessage> {
    override val prot: ClientProt = GameClientProt.MESSAGE_PRIVATE

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): MessagePrivateMessage {
        val name = buffer.gjstr()
        val message = tools.huffmanCodec.decode(buffer)
        return MessagePrivateMessage(
            name,
            message,
        )
    }
}
