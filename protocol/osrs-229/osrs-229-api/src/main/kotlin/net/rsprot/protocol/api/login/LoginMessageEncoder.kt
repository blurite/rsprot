package net.rsprot.protocol.api.login

import io.netty.channel.ChannelHandlerContext
import net.rsprot.crypto.cipher.NopStreamCipher
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.api.NetworkService
import net.rsprot.protocol.api.channel.inetAddress
import net.rsprot.protocol.api.encoder.OutgoingMessageEncoder
import net.rsprot.protocol.api.handlers.OutgoingMessageSizeEstimator
import net.rsprot.protocol.message.codec.outgoing.MessageEncoderRepository

/**
 * The encoder for any login messages.
 */
public class LoginMessageEncoder(
    public val networkService: NetworkService<*>,
) : OutgoingMessageEncoder() {
    override val cipher: StreamCipher = NopStreamCipher
    override val repository: MessageEncoderRepository<*> =
        networkService.encoderRepositories.loginMessageEncoderRepository
    override val validate: Boolean = false
    override val estimator: OutgoingMessageSizeEstimator = networkService.messageSizeEstimator

    override fun onMessageWritten(
        ctx: ChannelHandlerContext,
        opcode: Int,
        payloadSize: Int,
    ) {
        networkService
            .trafficMonitor
            .loginChannelTrafficMonitor
            .incrementOutgoingPackets(ctx.inetAddress(), opcode, payloadSize)
    }
}
