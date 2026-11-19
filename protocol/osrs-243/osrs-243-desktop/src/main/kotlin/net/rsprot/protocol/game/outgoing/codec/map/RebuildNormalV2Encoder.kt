package net.rsprot.protocol.game.outgoing.codec.map

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.map.StaticRebuildMessageV2
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class RebuildNormalV2Encoder : MessageEncoder<StaticRebuildMessageV2> {
    override val prot: ServerProt = GameServerProt.REBUILD_NORMAL_V2

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: StaticRebuildMessageV2,
    ) {
        buffer.p2Alt2(message.zoneZ)
        buffer.p2Alt3(message.worldArea)
        buffer.p2Alt3(message.zoneX)
    }
}
