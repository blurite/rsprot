package net.rsprot.protocol.game.outgoing.codec.map

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.codec.map.util.encodeRegionV1
import net.rsprot.protocol.game.outgoing.map.RebuildRegionV1
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class RebuildRegionV1Encoder : MessageEncoder<RebuildRegionV1> {
    override val prot: ServerProt = GameServerProt.REBUILD_REGION_V1

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: RebuildRegionV1,
    ) {
        buffer.p2Alt1(message.zoneZ)
        buffer.p2Alt3(message.zoneX)
        buffer.p1(if (message.reload) 1 else 0)

        encodeRegionV1(buffer, message.zones)
    }
}
