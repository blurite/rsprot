package net.rsprot.protocol.game.outgoing.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.player.SetPlayerOp
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class SetPlayerOpEncoder : MessageEncoder<SetPlayerOp> {
    override val prot: ServerProt = GameServerProt.SET_PLAYER_OP

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: SetPlayerOp,
    ) {
        buffer.pjstr(message.op ?: "null")
        buffer.p1(if (message.priority) 1 else 0)
        buffer.p1Alt1(message.id)
    }
}
