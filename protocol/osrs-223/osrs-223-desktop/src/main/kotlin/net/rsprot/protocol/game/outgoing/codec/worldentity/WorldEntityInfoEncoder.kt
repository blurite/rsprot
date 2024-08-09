package net.rsprot.protocol.game.outgoing.codec.worldentity

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.info.worldentityinfo.WorldEntityInfoPacket
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class WorldEntityInfoEncoder : MessageEncoder<WorldEntityInfoPacket> {
    override val prot: ServerProt = GameServerProt.WORLDENTITY_INFO

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: WorldEntityInfoPacket,
    ) {
        // Due to message extending byte buf holder, it is automatically released by the pipeline
        buffer.buffer.writeBytes(message.content())
    }
}
