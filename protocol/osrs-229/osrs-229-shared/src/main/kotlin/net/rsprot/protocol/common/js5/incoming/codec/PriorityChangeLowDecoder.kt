package net.rsprot.protocol.common.js5.incoming.codec

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.common.js5.incoming.prot.Js5ClientProt
import net.rsprot.protocol.js5.incoming.PriorityChangeLow
import net.rsprot.protocol.message.codec.MessageDecoder

public class PriorityChangeLowDecoder : MessageDecoder<PriorityChangeLow> {
    override val prot: ClientProt = Js5ClientProt.PRIORITY_CHANGE_LOW

    override fun decode(buffer: JagByteBuf): PriorityChangeLow {
        buffer.skipRead(3)
        return PriorityChangeLow
    }
}
