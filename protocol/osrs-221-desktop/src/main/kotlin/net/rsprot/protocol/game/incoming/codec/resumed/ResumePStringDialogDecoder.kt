package net.rsprot.protocol.game.incoming.codec.resumed

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.game.incoming.resumed.ResumePStringDialogMessage
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools

@Consistent
public class ResumePStringDialogDecoder : MessageDecoder<ResumePStringDialogMessage> {
    override val prot: ClientProt = GameClientProt.RESUME_P_STRINGDIALOG

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): ResumePStringDialogMessage {
        val string = buffer.gjstr()
        return ResumePStringDialogMessage(string)
    }
}
