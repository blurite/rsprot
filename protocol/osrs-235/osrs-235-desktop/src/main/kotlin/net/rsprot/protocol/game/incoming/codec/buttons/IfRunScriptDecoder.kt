package net.rsprot.protocol.game.incoming.codec.buttons

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.buttons.IfRunScript
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.util.gCombinedIdAlt1

public class IfRunScriptDecoder : MessageDecoder<IfRunScript> {
    override val prot: ClientProt = GameClientProt.IF_RUNSCRIPT

    override fun decode(buffer: JagByteBuf): IfRunScript {
        // Function is method(int combinedId, int sub, int obj, int script, Object[] args)
        // The order of argument does not seem to change (based on two revisions)
        val script = buffer.g4Alt1()
        val sub = buffer.g2Alt1()
        val combinedId = buffer.gCombinedIdAlt1()
        val obj = buffer.g2Alt1()

        val copy = buffer.buffer.copy()
        // Mark the buffer as "read" as copy function doesn't do it automatically.
        buffer.buffer.readerIndex(buffer.buffer.writerIndex())
        return IfRunScript(
            combinedId,
            sub,
            obj,
            script,
            copy,
        )
    }
}
