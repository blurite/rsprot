package net.rsprot.protocol.game.incoming.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.misc.user.MoveGameClick
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools

public class MoveGameClickDecoder : MessageDecoder<MoveGameClick> {
    override val prot: ClientProt = GameClientProt.MOVE_GAMECLICK

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): MoveGameClick {
        val z = buffer.g2Alt1()
        val keyCombination = buffer.g1()
        val x = buffer.g2Alt2()
        return MoveGameClick(
            x,
            z,
            keyCombination,
        )
    }
}
