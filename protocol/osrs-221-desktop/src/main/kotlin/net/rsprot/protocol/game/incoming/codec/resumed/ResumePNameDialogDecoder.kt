package net.rsprot.protocol.game.incoming.codec.resumed

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.game.incoming.resumed.ResumePNameDialogMessage
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools

@Consistent
public class ResumePNameDialogDecoder : MessageDecoder<ResumePNameDialogMessage> {
    override val prot: ClientProt = GameClientProt.RESUME_P_NAMEDIALOG

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): ResumePNameDialogMessage {
        val name = buffer.gjstr()
        return ResumePNameDialogMessage(name)
    }
}
