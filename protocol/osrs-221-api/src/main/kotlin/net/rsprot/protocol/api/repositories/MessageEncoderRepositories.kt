package net.rsprot.protocol.api.repositories

import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.common.js5.outgoing.prot.Js5MessageEncoderRepository
import net.rsprot.protocol.common.loginprot.outgoing.prot.LoginMessageEncoderRepository
import net.rsprot.protocol.common.platform.PlatformMap
import net.rsprot.protocol.common.platform.PlatformType
import net.rsprot.protocol.game.outgoing.prot.DesktopGameMessageEncoderRepository
import net.rsprot.protocol.message.codec.outgoing.MessageEncoderRepository

@OptIn(ExperimentalStdlibApi::class)
public class MessageEncoderRepositories private constructor(
    public val loginMessageDecoderRepository: MessageEncoderRepository<ServerProt>,
    public val js5MessageDecoderRepository: MessageEncoderRepository<ServerProt>,
    public val gameMessageDecoderRepositories: PlatformMap<MessageEncoderRepository<ServerProt>>,
) {
    public constructor() : this(
        LoginMessageEncoderRepository.build(),
        Js5MessageEncoderRepository.build(),
        PlatformMap.of(
            PlatformType.COUNT,
            listOf(PlatformType.DESKTOP to DesktopGameMessageEncoderRepository.build()),
        ),
    )
}
