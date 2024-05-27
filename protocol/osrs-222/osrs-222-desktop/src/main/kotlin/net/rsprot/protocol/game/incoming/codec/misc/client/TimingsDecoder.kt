package net.rsprot.protocol.game.incoming.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.misc.client.Timings
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools

@Consistent
public class TimingsDecoder : MessageDecoder<Timings> {
    override val prot: ClientProt = GameClientProt.TIMINGS

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): Timings {
        val connectionLostDuration = buffer.g2()
        val loginDuration = buffer.g2()
        val unusedDuration = buffer.g2()
        check(unusedDuration == 0) {
            "Unknown duration detected: $unusedDuration"
        }
        val clientState = buffer.g2()
        val unused1 = buffer.g2()
        check(unused1 == 0) {
            "Unused1 property value detected: $unused1"
        }
        val loginCount = buffer.g2()
        val unused2 = buffer.g2()
        check(unused2 == 0) {
            "Unused2 property value detected: $unused2"
        }
        return Timings(
            connectionLostDuration,
            loginDuration,
            clientState,
            loginCount,
        )
    }
}
