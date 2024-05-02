package net.rsprot.protocol.api.repositories

import net.rsprot.crypto.rsa.RsaKeyPair
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.common.client.ClientTypeMap
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.common.js5.incoming.prot.Js5MessageDecoderRepository
import net.rsprot.protocol.common.loginprot.incoming.prot.LoginMessageDecoderRepository
import net.rsprot.protocol.game.incoming.prot.DesktopGameMessageDecoderRepository
import net.rsprot.protocol.message.codec.incoming.MessageDecoderRepository
import java.math.BigInteger

@OptIn(ExperimentalStdlibApi::class)
public class MessageDecoderRepositories private constructor(
    public val loginMessageDecoderRepository: MessageDecoderRepository<ClientProt>,
    public val js5MessageDecoderRepository: MessageDecoderRepository<ClientProt>,
    public val gameMessageDecoderRepositories: ClientTypeMap<MessageDecoderRepository<ClientProt>>,
) {
    public constructor(
        exp: BigInteger,
        mod: BigInteger,
        gameMessageDecoderRepositories: ClientTypeMap<MessageDecoderRepository<ClientProt>>,
    ) : this(
        LoginMessageDecoderRepository.build(exp, mod),
        Js5MessageDecoderRepository.build(),
        gameMessageDecoderRepositories,
    )

    internal companion object {
        fun initialize(
            clientTypes: List<OldSchoolClientType>,
            rsaKeyPair: RsaKeyPair,
        ): MessageDecoderRepositories {
            val repositories =
                buildList {
                    if (OldSchoolClientType.DESKTOP in clientTypes) {
                        add(OldSchoolClientType.DESKTOP to DesktopGameMessageDecoderRepository.build())
                    }
                }
            val clientTypeMap =
                ClientTypeMap.of(
                    OldSchoolClientType.COUNT,
                    repositories,
                )
            return MessageDecoderRepositories(
                rsaKeyPair.exponent,
                rsaKeyPair.modulus,
                clientTypeMap,
            )
        }
    }
}
