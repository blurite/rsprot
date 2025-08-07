package net.rsprot.protocol.game.outgoing.codec.npcinfo

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcInfoLargeV5
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class NpcInfoLargeV5Encoder : MessageEncoder<NpcInfoLargeV5> {
    override val prot: ServerProt = GameServerProt.NPC_INFO_LARGE_V5

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: NpcInfoLargeV5,
    ) {
        // Due to message extending byte buf holder, it is automatically released by the pipeline
        buffer.buffer.writeBytes(message.content())
    }
}
