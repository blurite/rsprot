package net.rsprot.protocol.game.outgoing.codec.logout

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.logout.Logout
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class LogoutEncoder : MessageEncoder<Logout> {
    override val prot: ServerProt = GameServerProt.LOGOUT

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: Logout,
    ) {
    }
}
