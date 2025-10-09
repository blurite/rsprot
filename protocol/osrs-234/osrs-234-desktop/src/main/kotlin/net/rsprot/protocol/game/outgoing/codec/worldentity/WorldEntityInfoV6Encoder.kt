package net.rsprot.protocol.game.outgoing.codec.worldentity

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.info.worldentityinfo.WorldEntityInfoV6Packet
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class WorldEntityInfoV6Encoder : MessageEncoder<WorldEntityInfoV6Packet> {
    override val prot: ServerProt = GameServerProt.WORLDENTITY_INFO_V6

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: WorldEntityInfoV6Packet,
    ) {
        // Due to message extending byte buf holder, it is automatically released by the pipeline
        buffer.buffer.writeBytes(message.content())
    }
}
