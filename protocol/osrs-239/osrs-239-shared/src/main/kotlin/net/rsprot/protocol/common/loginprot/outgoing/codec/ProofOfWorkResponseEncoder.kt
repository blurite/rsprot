package net.rsprot.protocol.common.loginprot.outgoing.codec

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.common.loginprot.outgoing.prot.LoginServerProt
import net.rsprot.protocol.loginprot.outgoing.LoginResponse
import net.rsprot.protocol.message.codec.MessageEncoder

public class ProofOfWorkResponseEncoder : MessageEncoder<LoginResponse.ProofOfWork> {
    override val prot: ServerProt = LoginServerProt.PROOF_OF_WORK

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: LoginResponse.ProofOfWork,
    ) {
        val challenge = message.proofOfWork.challengeType
        buffer.p1(message.proofOfWork.challengeType.id)
        challenge.encode(buffer)
    }
}
