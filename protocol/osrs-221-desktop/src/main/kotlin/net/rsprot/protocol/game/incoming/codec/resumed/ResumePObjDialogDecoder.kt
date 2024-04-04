package net.rsprot.protocol.game.incoming.codec.resumed

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.game.incoming.resumed.ResumePObjDialog
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools

@Consistent
public class ResumePObjDialogDecoder : MessageDecoder<ResumePObjDialog> {
    override val prot: ClientProt = GameClientProt.RESUME_P_OBJDIALOG

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): ResumePObjDialog {
        val obj = buffer.g2()
        return ResumePObjDialog(obj)
    }
}
