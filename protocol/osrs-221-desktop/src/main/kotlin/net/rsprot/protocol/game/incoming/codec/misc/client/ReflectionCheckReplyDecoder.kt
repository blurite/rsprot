package net.rsprot.protocol.game.incoming.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.misc.client.ReflectionCheckReply
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools

@Consistent
public class ReflectionCheckReplyDecoder : MessageDecoder<ReflectionCheckReply> {
    override val prot: ClientProt = GameClientProt.REFLECTION_CHECK_REPLY

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): ReflectionCheckReply {
        val id = buffer.g4()
        val slice = buffer.buffer.retainedSlice()
        return ReflectionCheckReply(
            id,
            slice,
        )
    }
}
