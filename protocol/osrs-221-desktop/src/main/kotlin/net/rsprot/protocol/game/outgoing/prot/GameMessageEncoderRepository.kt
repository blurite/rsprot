package net.rsprot.protocol.game.outgoing.prot

import net.rsprot.protocol.ProtRepository
import net.rsprot.protocol.game.outgoing.codec.playerinfo.PlayerInfoEncoder
import net.rsprot.protocol.message.codec.outgoing.MessageEncoderRepository
import net.rsprot.protocol.message.codec.outgoing.MessageEncoderRepositoryBuilder

public object GameMessageEncoderRepository {
    @ExperimentalStdlibApi
    public fun build(): MessageEncoderRepository<GameServerProt> {
        val protRepository = ProtRepository.of<GameServerProt>()
        val builder =
            MessageEncoderRepositoryBuilder(protRepository).apply {
                bind(PlayerInfoEncoder())
            }
        return builder.build()
    }
}
