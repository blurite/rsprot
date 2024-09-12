package net.rsprot.protocol.game.outgoing.codec.map

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.codec.map.util.encodeRegion
import net.rsprot.protocol.game.outgoing.map.RebuildWorldEntity
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class RebuildWorldEntityEncoder : MessageEncoder<RebuildWorldEntity> {
    override val prot: ServerProt = GameServerProt.REBUILD_WORLDENTITY

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: RebuildWorldEntity,
    ) {
        buffer.p2(message.index)
        buffer.p2(message.baseX)
        buffer.p2(message.baseZ)
        encodeRegion(buffer, message.zones)
    }
}
