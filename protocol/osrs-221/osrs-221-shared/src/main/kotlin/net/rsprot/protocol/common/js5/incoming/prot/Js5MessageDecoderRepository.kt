package net.rsprot.protocol.common.js5.incoming.prot

import net.rsprot.protocol.ProtRepository
import net.rsprot.protocol.common.js5.incoming.codec.PrefetchRequestDecoder
import net.rsprot.protocol.common.js5.incoming.codec.PriorityChangeHighDecoder
import net.rsprot.protocol.common.js5.incoming.codec.PriorityChangeLowDecoder
import net.rsprot.protocol.common.js5.incoming.codec.UrgentRequestDecoder
import net.rsprot.protocol.common.js5.incoming.codec.XorChangeDecoder
import net.rsprot.protocol.message.codec.incoming.MessageDecoderRepository
import net.rsprot.protocol.message.codec.incoming.MessageDecoderRepositoryBuilder

public object Js5MessageDecoderRepository {
    @ExperimentalStdlibApi
    public fun build(): MessageDecoderRepository<Js5ClientProt> {
        val protRepository = ProtRepository.of<Js5ClientProt>()
        val builder =
            MessageDecoderRepositoryBuilder(protRepository).apply {
                bind(PrefetchRequestDecoder())
                bind(PriorityChangeHighDecoder())
                bind(PriorityChangeLowDecoder())
                bind(UrgentRequestDecoder())
                bind(XorChangeDecoder())
            }
        return builder.build()
    }
}
