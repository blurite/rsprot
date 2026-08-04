package net.rsprot.protocol.game.outgoing.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.client.ResetInteractionMode
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class ResetInteractionModeEncoder : MessageEncoder<ResetInteractionMode> {
    override val prot: ServerProt = GameServerProt.RESET_INTERACTION_MODE

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: ResetInteractionMode,
    ) {
        buffer.p2(message.worldId)
    }
}
