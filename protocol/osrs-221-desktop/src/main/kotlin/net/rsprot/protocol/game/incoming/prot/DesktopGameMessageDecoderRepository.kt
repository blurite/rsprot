package net.rsprot.protocol.game.incoming.prot

import net.rsprot.protocol.ProtRepository
import net.rsprot.protocol.message.codec.incoming.MessageDecoderRepository
import net.rsprot.protocol.message.codec.incoming.MessageDecoderRepositoryBuilder

public object DesktopGameMessageDecoderRepository {
    @ExperimentalStdlibApi
    public fun build(): MessageDecoderRepository<GameClientProt> {
        val protRepository = ProtRepository.of<GameClientProt>()
        val builder =
            MessageDecoderRepositoryBuilder(
                protRepository,
            ).apply {
            }
        return builder.build()
    }
}
