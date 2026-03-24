package net.rsprot.protocol.game.outgoing.codec.map

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.codec.map.util.encodeRegionV1
import net.rsprot.protocol.game.outgoing.map.RebuildWorldEntityV3
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class RebuildWorldEntityV3Encoder : MessageEncoder<RebuildWorldEntityV3> {
    override val prot: ServerProt = GameServerProt.REBUILD_WORLDENTITY_V3

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: RebuildWorldEntityV3,
    ) {
        buffer.p2(message.baseX)
        buffer.p2(message.baseZ)
        encodeRegionV1(buffer, message.zones)
    }
}
