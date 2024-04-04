package net.rsprot.protocol.game.incoming.codec.buttons

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.buttons.If1Button
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprot.protocol.util.gCombinedId

@Consistent
public class If1ButtonDecoder : MessageDecoder<If1Button> {
    override val prot: ClientProt = GameClientProt.IF_BUTTON

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): If1Button {
        val combinedId = buffer.gCombinedId()
        return If1Button(combinedId)
    }
}
