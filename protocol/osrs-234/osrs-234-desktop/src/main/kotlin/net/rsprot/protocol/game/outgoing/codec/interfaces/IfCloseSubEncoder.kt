package net.rsprot.protocol.game.outgoing.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfCloseSub
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.util.pCombinedId

@Consistent
public class IfCloseSubEncoder : MessageEncoder<IfCloseSub> {
    override val prot: ServerProt = GameServerProt.IF_CLOSESUB

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: IfCloseSub,
    ) {
        buffer.pCombinedId(message.combinedId)
    }
}
