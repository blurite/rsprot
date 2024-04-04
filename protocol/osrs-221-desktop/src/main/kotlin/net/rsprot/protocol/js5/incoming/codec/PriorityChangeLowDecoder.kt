package net.rsprot.protocol.js5.incoming.codec

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.js5.incoming.PriorityChangeLow
import net.rsprot.protocol.js5.incoming.prot.Js5ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools

public class PriorityChangeLowDecoder : MessageDecoder<PriorityChangeLow> {
    override val prot: ClientProt = Js5ClientProt.PRIORITY_CHANGE_LOW

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): PriorityChangeLow {
        return PriorityChangeLow
    }
}
