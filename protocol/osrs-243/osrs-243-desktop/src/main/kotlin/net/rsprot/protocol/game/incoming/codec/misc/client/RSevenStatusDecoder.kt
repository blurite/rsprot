package net.rsprot.protocol.game.incoming.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.misc.client.RSevenStatus
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class RSevenStatusDecoder : MessageDecoder<RSevenStatus> {
    override val prot: ClientProt = GameClientProt.RSEVEN_STATUS

    override fun decode(buffer: JagByteBuf): RSevenStatus {
        val packed = buffer.g1()
        return RSevenStatus(packed)
    }
}
