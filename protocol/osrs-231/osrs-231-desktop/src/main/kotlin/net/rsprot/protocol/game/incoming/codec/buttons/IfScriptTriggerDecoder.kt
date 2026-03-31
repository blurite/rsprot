package net.rsprot.protocol.game.incoming.codec.buttons

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.buttons.IfScriptTrigger
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.util.gCombinedIdAlt3

public class IfScriptTriggerDecoder : MessageDecoder<IfScriptTrigger> {
    override val prot: ClientProt = GameClientProt.IF_SCRIPT_TRIGGER

    override fun decode(buffer: JagByteBuf): IfScriptTrigger {
        // Function is method(int combinedId, int sub, int obj, int crc, Object[] args)
        val sub = buffer.g2()
        val combinedId = buffer.gCombinedIdAlt3()
        val obj = buffer.g2Alt3()
        val crc = buffer.g4Alt1()

        val copy = buffer.buffer.copy()
        // Mark the buffer as "read" as copy function doesn't do it automatically.
        buffer.buffer.readerIndex(buffer.buffer.writerIndex())
        return IfScriptTrigger(
            combinedId,
            sub,
            obj,
            crc,
            copy,
        )
    }
}
