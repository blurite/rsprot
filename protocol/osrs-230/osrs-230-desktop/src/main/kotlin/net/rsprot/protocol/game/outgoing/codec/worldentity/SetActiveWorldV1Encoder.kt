@file:Suppress("DEPRECATION")

package net.rsprot.protocol.game.outgoing.codec.worldentity

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.worldentity.SetActiveWorldV1
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class SetActiveWorldV1Encoder : MessageEncoder<SetActiveWorldV1> {
    override val prot: ServerProt = GameServerProt.SET_ACTIVE_WORLD_V1

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: SetActiveWorldV1,
    ) {
        when (val type = message.worldType) {
            is SetActiveWorldV1.RootWorldType -> {
                // Prefix 0 implies a root world update
                buffer.p1(0)
                // The slot is ignored for root world updates
                buffer.p2(0)
                buffer.p1(type.activeLevel)
            }
            is SetActiveWorldV1.DynamicWorldType -> {
                // Prefix 1 implies a dynamic world update
                buffer.p1(1)
                buffer.p2(type.index)
                buffer.p1(type.activeLevel)
            }
        }
    }
}
