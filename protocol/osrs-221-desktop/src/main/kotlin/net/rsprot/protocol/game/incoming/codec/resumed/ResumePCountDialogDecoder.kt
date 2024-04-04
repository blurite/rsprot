package net.rsprot.protocol.game.incoming.codec.resumed

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.game.incoming.resumed.ResumePCountDialogMessage
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class ResumePCountDialogDecoder : MessageDecoder<ResumePCountDialogMessage> {
    override val prot: ClientProt = GameClientProt.RESUME_P_COUNTDIALOG

    override fun decode(buffer: JagByteBuf): ResumePCountDialogMessage {
        val count = buffer.g4()
        return ResumePCountDialogMessage(count)
    }
}
