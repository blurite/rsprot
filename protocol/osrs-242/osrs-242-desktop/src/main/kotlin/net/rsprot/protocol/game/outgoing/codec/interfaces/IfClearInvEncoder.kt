package net.rsprot.protocol.game.outgoing.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfClearInv
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.util.pCombinedId

public class IfClearInvEncoder : MessageEncoder<IfClearInv> {
    override val prot: ServerProt = GameServerProt.IF_CLEARINV

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: IfClearInv,
    ) {
        buffer.pCombinedId(message.combinedId)
    }
}
