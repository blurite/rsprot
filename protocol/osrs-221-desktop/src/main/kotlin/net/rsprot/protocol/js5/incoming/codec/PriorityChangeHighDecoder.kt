package net.rsprot.protocol.js5.incoming.codec

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.js5.incoming.PriorityChangeHigh
import net.rsprot.protocol.js5.incoming.prot.Js5ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class PriorityChangeHighDecoder : MessageDecoder<PriorityChangeHigh> {
    override val prot: ClientProt = Js5ClientProt.PRIORITY_CHANGE_HIGH

    override fun decode(buffer: JagByteBuf): PriorityChangeHigh {
        return PriorityChangeHigh
    }
}
