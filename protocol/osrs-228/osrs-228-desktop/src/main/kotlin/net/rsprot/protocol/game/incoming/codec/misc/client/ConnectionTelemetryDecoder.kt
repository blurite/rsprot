package net.rsprot.protocol.game.incoming.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.misc.client.ConnectionTelemetry
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class ConnectionTelemetryDecoder : MessageDecoder<ConnectionTelemetry> {
    override val prot: ClientProt = GameClientProt.CONNECTION_TELEMETRY

    override fun decode(buffer: JagByteBuf): ConnectionTelemetry {
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
        return ConnectionTelemetry(
            connectionLostDuration,
            loginDuration,
            clientState,
            loginCount,
        )
    }
}
