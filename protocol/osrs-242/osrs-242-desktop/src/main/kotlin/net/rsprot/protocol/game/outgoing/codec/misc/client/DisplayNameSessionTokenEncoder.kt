package net.rsprot.protocol.game.outgoing.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.client.DisplayNameSessionToken
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class DisplayNameSessionTokenEncoder : MessageEncoder<DisplayNameSessionToken> {
    override val prot: ServerProt = GameServerProt.DISPLAYNAME_SESSION_TOKEN

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: DisplayNameSessionToken,
    ) {
        buffer.pjstr(message.token)
    }
}
