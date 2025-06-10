package net.rsprot.protocol.game.incoming.codec.buttons

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.buttons.IfRunScript
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.util.gCombinedIdAlt3

public class IfRunScriptDecoder : MessageDecoder<IfRunScript> {
    override val prot: ClientProt = GameClientProt.IF_RUNSCRIPT

    override fun decode(buffer: JagByteBuf): IfRunScript {
        val sub = buffer.g2()
        val combinedId = buffer.gCombinedIdAlt3()
        val obj = buffer.g2Alt3()
        val script = buffer.g4Alt1()

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
