package net.rsprot.protocol.game.outgoing.codec.worldentity

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.info.worldentityinfo.WorldEntityInfoV4Packet
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class WorldEntityInfoV5Encoder : MessageEncoder<WorldEntityInfoV4Packet> {
    override val prot: ServerProt = GameServerProt.WORLDENTITY_INFO_V5

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: WorldEntityInfoV4Packet,
    ) {
        // Due to message extending byte buf holder, it is automatically released by the pipeline
        buffer.buffer.writeBytes(message.content())
    }
}
