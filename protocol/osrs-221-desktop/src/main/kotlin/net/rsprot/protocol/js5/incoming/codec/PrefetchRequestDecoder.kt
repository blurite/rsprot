package net.rsprot.protocol.js5.incoming.codec

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.js5.incoming.PrefetchRequest
import net.rsprot.protocol.js5.incoming.prot.Js5ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools

public class PrefetchRequestDecoder : MessageDecoder<PrefetchRequest> {
    override val prot: ClientProt = Js5ClientProt.PREFETCH_REQUEST

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): PrefetchRequest {
        val archiveId = buffer.g2()
        val groupId = buffer.g2()
        return PrefetchRequest(
            archiveId.toUByte(),
            groupId.toUShort(),
        )
    }
}
