package net.rsprot.protocol.game.incoming.codec.resumed

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.game.incoming.resumed.ResumePNameDialog
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class ResumePNameDialogDecoder : MessageDecoder<ResumePNameDialog> {
    override val prot: ClientProt = GameClientProt.RESUME_P_NAMEDIALOG

    override fun decode(buffer: JagByteBuf): ResumePNameDialog {
        val name = buffer.gjstr()
        return ResumePNameDialog(name)
    }
}
