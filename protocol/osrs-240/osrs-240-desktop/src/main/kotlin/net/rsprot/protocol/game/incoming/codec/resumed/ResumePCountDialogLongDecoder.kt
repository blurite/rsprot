package net.rsprot.protocol.game.incoming.codec.resumed

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.game.incoming.resumed.ResumePCountDialogLong
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class ResumePCountDialogLongDecoder : MessageDecoder<ResumePCountDialogLong> {
    override val prot: ClientProt = GameClientProt.RESUME_P_COUNTDIALOG_LONG

    override fun decode(buffer: JagByteBuf): ResumePCountDialogLong {
        val count = buffer.g8()
        return ResumePCountDialogLong(count)
    }
}
