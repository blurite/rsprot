package net.rsprot.protocol.api.repositories

import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.crypto.rsa.RsaKeyPair
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.internal.client.ClientTypeMap
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.common.client.OldSchoolClientType.DESKTOP
import net.rsprot.protocol.common.js5.incoming.prot.Js5MessageDecoderRepository
import net.rsprot.protocol.common.loginprot.incoming.prot.LoginMessageDecoderRepository
import net.rsprot.protocol.game.incoming.prot.DesktopGameMessageDecoderRepository
import net.rsprot.protocol.message.codec.incoming.MessageDecoderRepository
import java.math.BigInteger

/**
 * The message decoder repositories for login, JS5 and game, all held in the same place.
 */
@OptIn(ExperimentalStdlibApi::class)
public class MessageDecoderRepositories private constructor(
    public val loginMessageDecoderRepository: MessageDecoderRepository<ClientProt>,
    public val js5MessageDecoderRepository: MessageDecoderRepository<ClientProt>,
    public val gameMessageDecoderRepositories: ClientTypeMap<MessageDecoderRepository<ClientProt>>,
) {
    public constructor(
        clientTypes: List<OldSchoolClientType>,
        exp: BigInteger,
        mod: BigInteger,
        gameMessageDecoderRepositories: ClientTypeMap<MessageDecoderRepository<ClientProt>>,
    ) : this(
        LoginMessageDecoderRepository.build(clientTypes, exp, mod),
        Js5MessageDecoderRepository.build(),
        gameMessageDecoderRepositories,
    )

    internal companion object {
        fun initialize(
            clientTypes: List<OldSchoolClientType>,
            rsaKeyPair: RsaKeyPair,
            huffmanCodecProvider: HuffmanCodecProvider,
        ): MessageDecoderRepositories {
            val repositories =
                buildList {
                    if (DESKTOP in clientTypes) {
                        add(DESKTOP to DesktopGameMessageDecoderRepository.build(huffmanCodecProvider))
                    }
                }
            val clientTypeMap =
                ClientTypeMap.of(
                    OldSchoolClientType.COUNT,
                    repositories,
                )
            return MessageDecoderRepositories(
                clientTypes,
                rsaKeyPair.exponent,
                rsaKeyPair.modulus,
                clientTypeMap,
            )
        }
    }
}
