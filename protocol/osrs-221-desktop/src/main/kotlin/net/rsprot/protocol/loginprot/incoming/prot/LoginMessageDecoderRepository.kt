package net.rsprot.protocol.loginprot.incoming.prot

import net.rsprot.protocol.ProtRepository
import net.rsprot.protocol.loginprot.incoming.GameLogin
import net.rsprot.protocol.loginprot.incoming.GameReconnect
import net.rsprot.protocol.loginprot.incoming.InitGameConnection
import net.rsprot.protocol.loginprot.incoming.InitJs5RemoteConnection
import net.rsprot.protocol.loginprot.incoming.ProofOfWorkReply
import net.rsprot.protocol.loginprot.incoming.codec.GameLoginDecoder
import net.rsprot.protocol.loginprot.incoming.codec.GameReconnectDecoder
import net.rsprot.protocol.loginprot.incoming.codec.InitGameConnectionDecoder
import net.rsprot.protocol.loginprot.incoming.codec.InitJs5RemoteConnectionDecoder
import net.rsprot.protocol.loginprot.incoming.codec.ProofOfWorkReplyDecoder
import net.rsprot.protocol.message.codec.incoming.MessageDecoderRepository
import net.rsprot.protocol.message.codec.incoming.MessageDecoderRepositoryBuilder
import java.math.BigInteger

public object LoginMessageDecoderRepository {
    @ExperimentalStdlibApi
    public fun build(
        exp: BigInteger,
        mod: BigInteger,
    ): MessageDecoderRepository {
        val protRepository = ProtRepository.of<LoginClientProt>()
        val builder =
            MessageDecoderRepositoryBuilder(protRepository).apply {
                bind(InitGameConnection::class.java, InitGameConnectionDecoder())
                bind(InitJs5RemoteConnection::class.java, InitJs5RemoteConnectionDecoder())
                bind(GameLogin::class.java, GameLoginDecoder(exp, mod))
                bind(GameReconnect::class.java, GameReconnectDecoder(exp, mod))
                bind(ProofOfWorkReply::class.java, ProofOfWorkReplyDecoder())
            }
        return builder.build()
    }
}
