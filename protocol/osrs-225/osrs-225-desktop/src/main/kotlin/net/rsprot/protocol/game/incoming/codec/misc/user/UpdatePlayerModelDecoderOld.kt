package net.rsprot.protocol.game.incoming.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.misc.user.UpdatePlayerModelOld
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class UpdatePlayerModelDecoderOld : MessageDecoder<UpdatePlayerModelOld> {
    override val prot: ClientProt = GameClientProt.UPDATE_PLAYER_MODEL_OLD

    override fun decode(buffer: JagByteBuf): UpdatePlayerModelOld {
        val bodyType = buffer.g1()
        val identKit = ByteArray(7)
        for (i in identKit.indices) {
            identKit[i] = buffer.g1().toByte()
        }
        val colours = ByteArray(5)
        for (i in colours.indices) {
            colours[i] = buffer.g1().toByte()
        }
        return UpdatePlayerModelOld(
            bodyType,
            identKit,
            colours,
        )
    }
}
