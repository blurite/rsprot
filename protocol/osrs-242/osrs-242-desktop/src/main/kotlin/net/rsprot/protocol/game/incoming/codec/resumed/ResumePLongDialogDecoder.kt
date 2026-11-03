package net.rsprot.protocol.game.incoming.codec.resumed

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.game.incoming.resumed.ResumePLongDialog
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class ResumePLongDialogDecoder : MessageDecoder<ResumePLongDialog> {
    override val prot: ClientProt = GameClientProt.RESUME_P_LONGDIALOG

    override fun decode(buffer: JagByteBuf): ResumePLongDialog {
        val count = buffer.g8()
        return ResumePLongDialog(count)
    }
}
