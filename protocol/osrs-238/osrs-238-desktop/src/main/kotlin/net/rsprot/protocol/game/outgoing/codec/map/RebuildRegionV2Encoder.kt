package net.rsprot.protocol.game.outgoing.codec.map

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.codec.map.util.encodeRegionV2
import net.rsprot.protocol.game.outgoing.map.RebuildRegionV2
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class RebuildRegionV2Encoder : MessageEncoder<RebuildRegionV2> {
    override val prot: ServerProt = GameServerProt.REBUILD_REGION_V2

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: RebuildRegionV2,
    ) {
        buffer.p1Alt1(if (message.reload) 1 else 0)
        buffer.p2(message.zoneX)
        buffer.p2Alt3(message.zoneZ)

        encodeRegionV2(buffer, message.zones)
    }
}
