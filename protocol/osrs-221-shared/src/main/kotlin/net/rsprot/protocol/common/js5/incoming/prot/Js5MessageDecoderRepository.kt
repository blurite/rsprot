package net.rsprot.protocol.common.js5.incoming.prot

import net.rsprot.protocol.ProtRepository
import net.rsprot.protocol.common.platform.PlatformType
import net.rsprot.protocol.message.codec.incoming.MessageDecoderRepository
import net.rsprot.protocol.message.codec.incoming.MessageDecoderRepositoryBuilder

public object Js5MessageDecoderRepository {
    @ExperimentalStdlibApi
    public fun build(): MessageDecoderRepository<Js5ClientProt, PlatformType> {
        val protRepository = ProtRepository.of<Js5ClientProt>()
        val builder =
            MessageDecoderRepositoryBuilder(
                PlatformType.DESKTOP,
                protRepository,
            ).apply {
            }
        return builder.build()
    }
}
