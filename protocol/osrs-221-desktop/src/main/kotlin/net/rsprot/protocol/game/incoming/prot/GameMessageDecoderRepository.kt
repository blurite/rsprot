package net.rsprot.protocol.game.incoming.prot

import net.rsprot.protocol.ProtRepository
import net.rsprot.protocol.message.codec.incoming.MessageDecoderRepository
import net.rsprot.protocol.message.codec.incoming.MessageDecoderRepositoryBuilder
import net.rsprot.protocol.common.platform.PlatformType

public object GameMessageDecoderRepository {
    @ExperimentalStdlibApi
    public fun build(): MessageDecoderRepository<GameClientProt, PlatformType> {
        val protRepository = ProtRepository.of<GameClientProt>()
        val builder =
            MessageDecoderRepositoryBuilder(
                PlatformType.DESKTOP,
                protRepository,
            ).apply {
            }
        return builder.build()
    }
}
