package net.rsprot.protocol.js5.incoming.codec

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.js5.incoming.UrgentRequest
import net.rsprot.protocol.js5.incoming.prot.Js5ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class UrgentRequestDecoder : MessageDecoder<UrgentRequest> {
    override val prot: ClientProt = Js5ClientProt.URGENT_REQUEST

    override fun decode(buffer: JagByteBuf): UrgentRequest {
        val archiveId = buffer.g2()
        val groupId = buffer.g2()
        return UrgentRequest(
            archiveId.toUByte(),
            groupId.toUShort(),
        )
    }
}
