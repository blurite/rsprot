package net.rsprot.protocol.api.game

import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.api.NetworkService
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
        networkService.encoderRepositories.gameMessageEncoderRepositories[client]
    override val validate: Boolean = true

    override fun mapOpcode(opcode: Int): Int {
        val mapper = networkService.serverToClientOpcodeMapper ?: return opcode
        return mapper.encode(opcode)
    }
}
