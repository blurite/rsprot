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
        // We have to use the same encoder as it relies on the prot
        // under the hood to map the encoders down
        // if (message is RebuildLogin) {
        //     val gpiInitBlock = message.gpiInitBlock
        //     buffer.buffer.writeBytes(
        //         gpiInitBlock,
        //         gpiInitBlock.readerIndex(),
        //         gpiInitBlock.readableBytes(),
        //     )
        // }
        buffer.p2(message.worldArea)
        buffer.p2Alt1(message.zoneX)
        buffer.p2Alt1(message.zoneZ)
    }
}
