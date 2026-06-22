package net.rsprot.protocol.game.outgoing.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.client.SetInteractionMode
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class SetInteractionModeEncoder : MessageEncoder<SetInteractionMode> {
    override val prot: ServerProt = GameServerProt.SET_INTERACTION_MODE

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: SetInteractionMode,
    ) {
        buffer.p2(message.worldId)
        buffer.p1(message.tileInteractionMode)
        buffer.p1(message.entityInteractionMode)
    }
}
