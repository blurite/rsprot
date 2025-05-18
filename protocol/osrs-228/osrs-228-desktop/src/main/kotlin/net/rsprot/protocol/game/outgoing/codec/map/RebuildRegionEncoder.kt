package net.rsprot.protocol.game.outgoing.codec.map

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.codec.map.util.encodeRegion
import net.rsprot.protocol.game.outgoing.map.RebuildRegion
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class RebuildRegionEncoder : MessageEncoder<RebuildRegion> {
    override val prot: ServerProt = GameServerProt.REBUILD_REGION

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: RebuildRegion,
    ) {
        buffer.p2Alt1(message.zoneX)
        buffer.p2(message.zoneZ)
        buffer.p1Alt1(if (message.reload) 1 else 0)

        encodeRegion(buffer, message.zones)
    }
}
