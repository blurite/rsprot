package net.rsprot.protocol.api.game

import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.api.NetworkService
import net.rsprot.protocol.api.encoder.OutgoingMessageEncoder
import net.rsprot.protocol.common.platform.PlatformType
import net.rsprot.protocol.message.codec.outgoing.MessageEncoderRepository

public class GameMessageEncoder(
    public val networkService: NetworkService<*, *>,
    override val cipher: StreamCipher,
    platform: PlatformType,
) : OutgoingMessageEncoder() {
    override val repository: MessageEncoderRepository<*> =
        networkService.encoderRepositories.gameMessageDecoderRepositories[platform]
}
