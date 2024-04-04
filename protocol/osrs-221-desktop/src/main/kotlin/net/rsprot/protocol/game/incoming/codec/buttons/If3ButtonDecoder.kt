package net.rsprot.protocol.game.incoming.codec.buttons

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.game.incoming.buttons.If3ButtonMessage
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprot.protocol.util.gCombinedId

@Consistent
public class If3ButtonDecoder(
    override val prot: GameClientProt,
    private val op: Int,
) : MessageDecoder<If3ButtonMessage> {
    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): If3ButtonMessage {
        val combinedId = buffer.gCombinedId()
        val sub = buffer.g2()
        val obj = buffer.g2()
        return If3ButtonMessage(
            combinedId,
            sub,
            obj,
            op,
        )
    }
}
