package net.rsprot.protocol.common.js5.incoming.codec

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.common.js5.incoming.prot.Js5ClientProt
import net.rsprot.protocol.js5.incoming.UrgentRequest
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools

public class UrgentRequestDecoder : MessageDecoder<UrgentRequest> {
    override val prot: ClientProt = Js5ClientProt.URGENT_REQUEST

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): UrgentRequest {
        val archiveId = buffer.g1()
        val groupId = buffer.g2()
        return UrgentRequest(
            archiveId.toUByte(),
            groupId.toUShort(),
        )
    }
}
