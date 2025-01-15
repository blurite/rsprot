package net.rsprot.protocol.game.outgoing.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.client.SendPing
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class SendPingEncoder : MessageEncoder<SendPing> {
    override val prot: ServerProt = GameServerProt.SEND_PING

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: SendPing,
    ) {
        buffer.p4(message.value1)
        buffer.p4(message.value2)
    }
}
