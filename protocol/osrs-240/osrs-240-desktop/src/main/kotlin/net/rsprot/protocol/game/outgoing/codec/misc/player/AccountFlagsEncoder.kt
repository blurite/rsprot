package net.rsprot.protocol.game.outgoing.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.player.AccountFlags
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class AccountFlagsEncoder : MessageEncoder<AccountFlags> {
    override val prot: ServerProt = GameServerProt.ACCOUNT_FLAGS

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: AccountFlags,
    ) {
        buffer.p8(message.flags)
    }
}
