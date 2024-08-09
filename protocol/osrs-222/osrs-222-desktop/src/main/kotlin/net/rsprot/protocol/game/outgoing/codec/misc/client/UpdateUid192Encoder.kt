package net.rsprot.protocol.game.outgoing.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.crypto.crc.CyclicRedundancyCheck
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.client.UpdateUid192
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class UpdateUid192Encoder : MessageEncoder<UpdateUid192> {
    override val prot: ServerProt = GameServerProt.UPDATE_UID192

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: UpdateUid192,
    ) {
        buffer.pdata(message.uid)
        buffer.p4(CyclicRedundancyCheck.computeCrc32(message.uid))
    }
}
