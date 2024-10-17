package net.rsprot.protocol.game.outgoing.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.client.WorldEntityResetInteractionMode
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class WorldEntityResetInteractionModeEncoder : MessageEncoder<WorldEntityResetInteractionMode> {
    override val prot: ServerProt = GameServerProt.WORLDENTITY_RESET_INTERACTION_MODE

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: WorldEntityResetInteractionMode,
    ) {
        buffer.p2(message.worldId)
    }
}
