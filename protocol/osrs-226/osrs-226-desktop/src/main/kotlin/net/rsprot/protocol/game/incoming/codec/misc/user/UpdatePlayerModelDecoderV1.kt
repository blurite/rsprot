package net.rsprot.protocol.game.incoming.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.misc.user.UpdatePlayerModelV1
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class UpdatePlayerModelDecoderV1 : MessageDecoder<UpdatePlayerModelV1> {
    override val prot: ClientProt = GameClientProt.UPDATE_PLAYER_MODEL_V1

    override fun decode(buffer: JagByteBuf): UpdatePlayerModelV1 {
        val bodyType = buffer.g1()
        val identKit = ByteArray(7)
        for (i in identKit.indices) {
            identKit[i] = buffer.g1().toByte()
        }
        val colours = ByteArray(5)
        for (i in colours.indices) {
            colours[i] = buffer.g1().toByte()
        }
        return UpdatePlayerModelV1(
            bodyType,
            identKit,
            colours,
        )
    }
}
