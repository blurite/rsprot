package net.rsprot.protocol.api.repositories

import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.common.js5.incoming.prot.Js5MessageDecoderRepository
import net.rsprot.protocol.common.loginprot.incoming.prot.LoginMessageDecoderRepository
import net.rsprot.protocol.common.platform.PlatformMap
import net.rsprot.protocol.common.platform.PlatformType
import net.rsprot.protocol.game.incoming.prot.DesktopGameMessageDecoderRepository
import net.rsprot.protocol.message.codec.incoming.MessageDecoderRepository
import java.math.BigInteger

@OptIn(ExperimentalStdlibApi::class)
public class MessageDecoderRepositories private constructor(
    public val loginMessageDecoderRepository: MessageDecoderRepository<ClientProt>,
    public val js5MessageDecoderRepository: MessageDecoderRepository<ClientProt>,
    public val gameMessageDecoderRepositories: PlatformMap<MessageDecoderRepository<ClientProt>>,
) {
    public constructor(
        exp: BigInteger,
        mod: BigInteger,
    ) : this(
        LoginMessageDecoderRepository.build(exp, mod),
        Js5MessageDecoderRepository.build(),
        PlatformMap.of(
            PlatformType.COUNT,
            listOf(PlatformType.DESKTOP to DesktopGameMessageDecoderRepository.build()),
        ),
    )
}
