package net.rsprot.protocol.game.outgoing.codec.worldentity

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.worldentity.SetActiveWorldV2
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class SetActiveWorldV2Encoder : MessageEncoder<SetActiveWorldV2> {
    override val prot: ServerProt = GameServerProt.SET_ACTIVE_WORLD_V2

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: SetActiveWorldV2,
    ) {
        when (val type = message.worldType) {
            is SetActiveWorldV2.RootWorldType -> {
                // The slot is ignored for root world updates
                buffer.p2(-1)
                buffer.p1(type.activeLevel)
            }
            is SetActiveWorldV2.DynamicWorldType -> {
                buffer.p2(type.index)
                buffer.p1(type.activeLevel)
            }
        }
    }
}
