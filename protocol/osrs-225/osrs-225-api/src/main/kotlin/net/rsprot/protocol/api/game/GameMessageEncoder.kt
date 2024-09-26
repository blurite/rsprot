package net.rsprot.protocol.api.game

import io.netty.channel.ChannelHandlerContext
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.api.NetworkService
import net.rsprot.protocol.api.channel.inetAddress
import net.rsprot.protocol.api.encoder.OutgoingMessageEncoder
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.message.codec.outgoing.MessageEncoderRepository

/**
 * The game messages encoder, following the traditional outgoing message encoder.
 */
public class GameMessageEncoder(
    public val networkService: NetworkService<*>,
    override val cipher: StreamCipher,
    client: OldSchoolClientType,
) : OutgoingMessageEncoder() {
    override val repository: MessageEncoderRepository<*> =
        networkService.encoderRepositories.gameMessageDecoderRepositories[client]
    override val validate: Boolean = true

    override fun onMessageWritten(
        ctx: ChannelHandlerContext,
        opcode: Int,
        payloadSize: Int,
    ) {
        networkService
            .trafficHandler
            .gameChannelTrafficHandler
            .incrementOutgoingPackets(ctx.inetAddress(), opcode, payloadSize)
    }
}
