package net.rsprot.protocol.common.js5.outgoing.prot

import net.rsprot.protocol.ProtRepository
import net.rsprot.protocol.common.js5.outgoing.codec.Js5GroupResponseEncoder
import net.rsprot.protocol.message.codec.outgoing.MessageEncoderRepository
import net.rsprot.protocol.message.codec.outgoing.MessageEncoderRepositoryBuilder

public object Js5MessageEncoderRepository {
    @ExperimentalStdlibApi
    public fun build(): MessageEncoderRepository<Js5ServerProt> {
        val protRepository = ProtRepository.of<Js5ServerProt>()
        val builder =
            MessageEncoderRepositoryBuilder(
                protRepository,
            ).apply {
                bind(Js5GroupResponseEncoder())
            }
        return builder.build()
    }
}
