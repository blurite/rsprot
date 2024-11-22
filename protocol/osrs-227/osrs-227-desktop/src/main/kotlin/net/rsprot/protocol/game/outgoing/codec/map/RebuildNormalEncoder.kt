package net.rsprot.protocol.game.outgoing.codec.map

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.map.StaticRebuildMessage
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class RebuildNormalEncoder : MessageEncoder<StaticRebuildMessage> {
    override val prot: ServerProt = GameServerProt.REBUILD_NORMAL

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: StaticRebuildMessage,
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
        buffer.p2Alt1(message.worldArea)
        buffer.p2Alt1(message.zoneX)
        buffer.p2Alt2(message.zoneZ)
        buffer.p2(message.keys.size)
        for (xteaKey in message.keys) {
            for (intKey in xteaKey.key) {
                buffer.p4(intKey)
            }
        }
    }
}
