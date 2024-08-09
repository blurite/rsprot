package net.rsprot.protocol.game.incoming.codec.resumed

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.game.incoming.resumed.ResumePauseButton
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.util.gCombinedIdAlt2

public class ResumePauseButtonDecoder : MessageDecoder<ResumePauseButton> {
    override val prot: ClientProt = GameClientProt.RESUME_PAUSEBUTTON

    override fun decode(buffer: JagByteBuf): ResumePauseButton {
        val combinedId = buffer.gCombinedIdAlt2()
        val sub = buffer.g2Alt2()
        return ResumePauseButton(
            combinedId,
            sub,
        )
    }
}
