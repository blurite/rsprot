package net.rsprot.protocol.game.incoming.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.misc.client.SendPingReply
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class SendPingReplyDecoder : MessageDecoder<SendPingReply> {
    override val prot: ClientProt = GameClientProt.SEND_PING_REPLY

    override fun decode(buffer: JagByteBuf): SendPingReply {
        val gcPercentTime = buffer.g1()
        val fps = buffer.g1Alt2()
        val value1 = buffer.g4Alt1()
        val value2 = buffer.g4Alt1()
        return SendPingReply(
            fps,
            gcPercentTime,
            value1,
            value2,
        )
    }
}
