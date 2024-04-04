package net.rsprot.protocol.loginprot.incoming.codec

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.loginprot.incoming.ProofOfWorkReply
import net.rsprot.protocol.loginprot.incoming.prot.LoginClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools

public class ProofOfWorkReplyDecoder : MessageDecoder<ProofOfWorkReply> {
    override val prot: ClientProt = LoginClientProt.POW_REPLY

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): ProofOfWorkReply {
        val result = buffer.g8()
        return ProofOfWorkReply(result)
    }
}
