package net.rsprot.protocol.common.js5.outgoing.prot

import net.rsprot.protocol.ProtRepository
import net.rsprot.protocol.common.platform.PlatformType
import net.rsprot.protocol.message.codec.outgoing.MessageEncoderRepository
import net.rsprot.protocol.message.codec.outgoing.MessageEncoderRepositoryBuilder

public object Js5MessageEncoderRepository {
    @ExperimentalStdlibApi
    public fun build(): MessageEncoderRepository<Js5ServerProt, PlatformType> {
        val protRepository = ProtRepository.of<Js5ServerProt>()
        val builder =
            MessageEncoderRepositoryBuilder(
                PlatformType.DESKTOP,
                protRepository,
            ).apply {
            }
        return builder.build()
    }
}
