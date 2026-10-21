package net.rsprot.protocol.api.repositories

import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.crypto.rsa.RsaKeyPair
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.common.client.OldSchoolClientType.DESKTOP
import net.rsprot.protocol.common.js5.incoming.prot.Js5MessageDecoderRepository
import net.rsprot.protocol.common.loginprot.incoming.prot.LoginMessageDecoderRepository
import net.rsprot.protocol.game.incoming.prot.DesktopGameMessageDecoderRepository
import net.rsprot.protocol.internal.client.ClientTypeMap
import net.rsprot.protocol.internal.login.LoginCrcDecoder
import net.rsprot.protocol.loginprot.incoming.codec.DesktopLoginCrcDecoder
import net.rsprot.protocol.message.codec.incoming.MessageDecoderRepository
import java.math.BigInteger

/**
 * The message decoder repositories for login, JS5 and game, all held in the same place.
 */
@OptIn(ExperimentalStdlibApi::class)
public class MessageDecoderRepositories(
    public val loginMessageDecoderRepository: MessageDecoderRepository<ClientProt>,
    public val js5MessageDecoderRepository: MessageDecoderRepository<ClientProt>,
    public val gameMessageDecoderRepositories: ClientTypeMap<MessageDecoderRepository<ClientProt>>,
    public val loginCrcDecoders: ClientTypeMap<LoginCrcDecoder>,
) {
    public constructor(
        clientTypes: List<OldSchoolClientType>,
        exp: BigInteger,
        mod: BigInteger,
        gameMessageDecoderRepositories: ClientTypeMap<MessageDecoderRepository<ClientProt>>,
        loginCrcDecoders: ClientTypeMap<LoginCrcDecoder>,
    ) : this(
        LoginMessageDecoderRepository.build(clientTypes, exp, mod, loginCrcDecoders),
        Js5MessageDecoderRepository.build(),
        gameMessageDecoderRepositories,
        loginCrcDecoders,
    )

    public companion object {
        public fun initialize(
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
            val crcDecoders =
                ClientTypeMap.of(
                    buildList {
                        if (DESKTOP in clientTypes) {
                            add(DesktopLoginCrcDecoder)
                        }
                    },
                    OldSchoolClientType.COUNT,
                    LoginCrcDecoder::clientType,
                )
            return MessageDecoderRepositories(
                clientTypes,
                rsaKeyPair.exponent,
                rsaKeyPair.modulus,
                clientTypeMap,
                crcDecoders,
            )
        }
    }
}
