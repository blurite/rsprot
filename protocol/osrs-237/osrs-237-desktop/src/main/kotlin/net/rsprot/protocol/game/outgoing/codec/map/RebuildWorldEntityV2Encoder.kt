package net.rsprot.protocol.game.outgoing.codec.map

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.codec.map.util.encodeRegion
import net.rsprot.protocol.game.outgoing.map.RebuildWorldEntityV2
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class RebuildWorldEntityV2Encoder : MessageEncoder<RebuildWorldEntityV2> {
    override val prot: ServerProt = GameServerProt.REBUILD_WORLDENTITY_V2

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: RebuildWorldEntityV2,
    ) {
        buffer.p2(message.baseX)
        buffer.p2(message.baseZ)
        encodeRegion(buffer, message.zones)
    }
}
