package net.rsprot.protocol.game.incoming.codec.resumed

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.game.incoming.resumed.ResumePObjDialogMessage
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class ResumePObjDialogDecoder : MessageDecoder<ResumePObjDialogMessage> {
    override val prot: ClientProt = GameClientProt.RESUME_P_OBJDIALOG

    override fun decode(buffer: JagByteBuf): ResumePObjDialogMessage {
        val obj = buffer.g2()
        return ResumePObjDialogMessage(obj)
    }
}
