package net.rsprot.protocol.common.loginprot.incoming.prot

import net.rsprot.protocol.ProtRepository
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.common.loginprot.incoming.codec.GameLoginDecoder
import net.rsprot.protocol.common.loginprot.incoming.codec.GameReconnectDecoder
import net.rsprot.protocol.common.loginprot.incoming.codec.InitGameConnectionDecoder
import net.rsprot.protocol.common.loginprot.incoming.codec.InitJs5RemoteConnectionDecoder
import net.rsprot.protocol.common.loginprot.incoming.codec.ProofOfWorkReplyDecoder
import net.rsprot.protocol.common.loginprot.incoming.codec.RemainingBetaArchivesDecoder
import net.rsprot.protocol.internal.client.ClientTypeMap
import net.rsprot.protocol.internal.login.LoginCrcDecoder
import net.rsprot.protocol.message.codec.incoming.MessageDecoderRepository
import net.rsprot.protocol.message.codec.incoming.MessageDecoderRepositoryBuilder
import java.math.BigInteger

public object LoginMessageDecoderRepository {
    @ExperimentalStdlibApi
    public fun build(
        supportedClientTypes: List<OldSchoolClientType>,
        exp: BigInteger,
        mod: BigInteger,
        crcDecoders: ClientTypeMap<LoginCrcDecoder>,
    ): MessageDecoderRepository<LoginClientProt> {
        val protRepository = ProtRepository.of<LoginClientProt>()
        val builder =
            MessageDecoderRepositoryBuilder(
                protRepository,
            ).apply {
                bind(InitGameConnectionDecoder())
                bind(InitJs5RemoteConnectionDecoder())
                bind(GameLoginDecoder(supportedClientTypes, exp, mod, crcDecoders))
                bind(GameReconnectDecoder(supportedClientTypes, exp, mod, crcDecoders))
                bind(ProofOfWorkReplyDecoder())
                bind(RemainingBetaArchivesDecoder())
            }
        return builder.build()
    }
}
