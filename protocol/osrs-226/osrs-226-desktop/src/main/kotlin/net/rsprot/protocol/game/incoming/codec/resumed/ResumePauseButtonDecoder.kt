package net.rsprot.protocol.game.incoming.codec.resumed

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.game.incoming.resumed.ResumePauseButton
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.util.gCombinedIdAlt3

public class ResumePauseButtonDecoder : MessageDecoder<ResumePauseButton> {
    override val prot: ClientProt = GameClientProt.RESUME_PAUSEBUTTON

    override fun decode(buffer: JagByteBuf): ResumePauseButton {
        val sub = buffer.g2Alt3()
        val combinedId = buffer.gCombinedIdAlt3()
        return ResumePauseButton(
            combinedId,
            sub,
        )
    }
}
