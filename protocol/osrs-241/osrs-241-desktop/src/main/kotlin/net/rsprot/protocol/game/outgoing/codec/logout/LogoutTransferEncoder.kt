package net.rsprot.protocol.game.outgoing.codec.logout

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.logout.LogoutTransfer
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class LogoutTransferEncoder : MessageEncoder<LogoutTransfer> {
    override val prot: ServerProt = GameServerProt.LOGOUT_TRANSFER

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: LogoutTransfer,
    ) {
        buffer.pjstr(message.host)
        buffer.p2(message.id)
        buffer.p4(message.properties)
    }
}
