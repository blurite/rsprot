package net.rsprot.protocol.loginprot.incoming.codec

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.loginprot.incoming.ProofOfWorkReply
import net.rsprot.protocol.loginprot.incoming.prot.LoginClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class ProofOfWorkReplyDecoder : MessageDecoder<ProofOfWorkReply> {
    override val prot: ClientProt = LoginClientProt.POW_REPLY

    override fun decode(buffer: JagByteBuf): ProofOfWorkReply {
        val result = buffer.g8()
        return ProofOfWorkReply(result)
    }
}
