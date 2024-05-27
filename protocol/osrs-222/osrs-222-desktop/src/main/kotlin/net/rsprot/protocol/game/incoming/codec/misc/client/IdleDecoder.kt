package net.rsprot.protocol.game.incoming.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.misc.client.Idle
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools

@Consistent
public class IdleDecoder : MessageDecoder<Idle> {
    override val prot: ClientProt = GameClientProt.IDLE

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): Idle {
        return Idle
    }
}
