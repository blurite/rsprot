package net.rsprot.protocol.game.outgoing.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.client.WorldEntitySetInteractionMode
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class WorldEntitySetInteractionModeEncoder : MessageEncoder<WorldEntitySetInteractionMode> {
    override val prot: ServerProt = GameServerProt.WORLDENTITY_SET_INTERACTION_MODE

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: WorldEntitySetInteractionMode,
    ) {
        buffer.p2(message.worldId)
        buffer.p1(message.tileInteractionMode)
        buffer.p1(message.entityInteractionMode)
    }
}
