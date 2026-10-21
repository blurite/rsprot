package net.rsprot.protocol.common.js5.incoming.codec

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.common.js5.incoming.prot.Js5ClientProt
import net.rsprot.protocol.js5.incoming.XorChange
import net.rsprot.protocol.message.codec.MessageDecoder

public class XorChangeDecoder : MessageDecoder<XorChange> {
    override val prot: ClientProt = Js5ClientProt.XOR_CHANGE

    override fun decode(buffer: JagByteBuf): XorChange {
        val key = buffer.g1()
        buffer.skipRead(2)
        return XorChange(key)
    }
}
