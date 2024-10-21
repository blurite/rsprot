package net.rsprot.protocol.api.repositories

import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.common.client.ClientTypeMap
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.common.js5.outgoing.prot.Js5MessageEncoderRepository
import net.rsprot.protocol.common.loginprot.outgoing.prot.LoginMessageEncoderRepository
import net.rsprot.protocol.game.outgoing.prot.DesktopGameMessageEncoderRepository
import net.rsprot.protocol.message.codec.outgoing.MessageEncoderRepository

/**
 * The message encoder repository for all outgoing messages, for JS5, login and game.
 */
@OptIn(ExperimentalStdlibApi::class)
public class MessageEncoderRepositories private constructor(
    public val loginMessageEncoderRepository: MessageEncoderRepository<ServerProt>,
    public val js5MessageEncoderRepository: MessageEncoderRepository<ServerProt>,
    public val gameMessageEncoderRepositories: ClientTypeMap<MessageEncoderRepository<ServerProt>>,
) {
    public constructor(
        huffmanCodecProvider: HuffmanCodecProvider,
    ) : this(
        LoginMessageEncoderRepository.build(),
        Js5MessageEncoderRepository.build(),
        ClientTypeMap.of(
            OldSchoolClientType.COUNT,
            listOf(OldSchoolClientType.DESKTOP to DesktopGameMessageEncoderRepository.build(huffmanCodecProvider)),
        ),
    )
}
