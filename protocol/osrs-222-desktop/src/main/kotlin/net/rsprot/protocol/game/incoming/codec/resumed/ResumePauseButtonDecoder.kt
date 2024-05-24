package net.rsprot.protocol.game.incoming.codec.resumed

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.game.incoming.resumed.ResumePauseButton
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprot.protocol.util.gCombinedId

public class ResumePauseButtonDecoder : MessageDecoder<ResumePauseButton> {
    override val prot: ClientProt = GameClientProt.RESUME_PAUSEBUTTON

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): ResumePauseButton {
        val combinedId = buffer.gCombinedId()
        val sub = buffer.g2Alt3()
        return ResumePauseButton(
            combinedId,
            sub,
        )
    }
}
