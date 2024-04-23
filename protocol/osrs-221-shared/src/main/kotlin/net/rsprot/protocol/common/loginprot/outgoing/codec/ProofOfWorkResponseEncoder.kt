package net.rsprot.protocol.common.loginprot.outgoing.codec

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.common.loginprot.outgoing.prot.LoginServerProt
import net.rsprot.protocol.loginprot.incoming.pow.ProofOfWork
import net.rsprot.protocol.loginprot.outgoing.LoginResponse
import net.rsprot.protocol.message.codec.MessageEncoder

public class ProofOfWorkResponseEncoder : MessageEncoder<LoginResponse.ProofOfWork> {
    override val prot: ServerProt = LoginServerProt.PROOF_OF_WORK

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: LoginResponse.ProofOfWork,
    ) {
        val challenge = message.proofOfWork.challengeType
        buffer.p1(message.proofOfWork.challengeType.id)
        challenge.encode(buffer)
        ctx.channel()
            .attr(ProofOfWork.PROOF_OF_WORK)
            .set(message.proofOfWork)
    }
}
