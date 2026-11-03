package net.rsprot.protocol.game.incoming.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.misc.user.HiscoreRequest
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class HiscoreRequestDecoder : MessageDecoder<HiscoreRequest> {
    override val prot: ClientProt = GameClientProt.HISCORE_REQUEST

    override fun decode(buffer: JagByteBuf): HiscoreRequest {
        val requestId = buffer.g1()
        val type = buffer.g1()
        val name = buffer.gjstr()
        return HiscoreRequest(
            type,
            requestId,
            name,
        )
    }
}
