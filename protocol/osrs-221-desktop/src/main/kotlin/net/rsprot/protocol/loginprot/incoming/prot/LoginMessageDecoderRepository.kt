package net.rsprot.protocol.loginprot.incoming.prot

import net.rsprot.protocol.ProtRepository
import net.rsprot.protocol.loginprot.incoming.codec.GameLoginDecoder
import net.rsprot.protocol.loginprot.incoming.codec.GameReconnectDecoder
import net.rsprot.protocol.loginprot.incoming.codec.InitGameConnectionDecoder
import net.rsprot.protocol.loginprot.incoming.codec.InitJs5RemoteConnectionDecoder
import net.rsprot.protocol.loginprot.incoming.codec.ProofOfWorkReplyDecoder
import net.rsprot.protocol.message.codec.incoming.MessageDecoderRepository
import net.rsprot.protocol.message.codec.incoming.MessageDecoderRepositoryBuilder
import net.rsprot.protocol.shared.platform.PlatformType
import java.math.BigInteger

public object LoginMessageDecoderRepository {
    @ExperimentalStdlibApi
    public fun build(
        exp: BigInteger,
        mod: BigInteger,
    ): MessageDecoderRepository<LoginClientProt, PlatformType> {
        val protRepository = ProtRepository.of<LoginClientProt>()
        val builder =
            MessageDecoderRepositoryBuilder(
                PlatformType.DESKTOP,
                protRepository,
            ).apply {
                bind(InitGameConnectionDecoder())
                bind(InitJs5RemoteConnectionDecoder())
                bind(GameLoginDecoder(exp, mod))
                bind(GameReconnectDecoder(exp, mod))
                bind(ProofOfWorkReplyDecoder())
            }
        return builder.build()
    }
}
