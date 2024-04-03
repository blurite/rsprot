package net.rsprot.protocol.game.outgoing.prot

import net.rsprot.protocol.ProtRepository
import net.rsprot.protocol.game.outgoing.codec.playerinfo.PlayerInfoEncoder
import net.rsprot.protocol.message.codec.outgoing.MessageEncoderRepository
import net.rsprot.protocol.message.codec.outgoing.MessageEncoderRepositoryBuilder
import net.rsprot.protocol.shared.platform.PlatformType

public object GameMessageEncoderRepository {
    @ExperimentalStdlibApi
    public fun build(): MessageEncoderRepository<GameServerProt, PlatformType> {
        val protRepository = ProtRepository.of<GameServerProt>()
        val builder =
            MessageEncoderRepositoryBuilder(
                PlatformType.DESKTOP,
                protRepository,
            ).apply {
                bind(PlayerInfoEncoder())
            }
        return builder.build()
    }
}
