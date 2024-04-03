package net.rsprot.protocol.game.incoming.codec

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.game.incoming.If3ButtonEvent
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.util.gCombinedId

@Consistent
public class If3ButtonDecoder(
    override val prot: GameClientProt,
    private val op: Int,
) : MessageDecoder<If3ButtonEvent> {
    override fun decode(buffer: JagByteBuf): If3ButtonEvent {
        val combinedId = buffer.gCombinedId()
        val sub = buffer.g2()
        val obj = buffer.g2()
        return If3ButtonEvent(
            combinedId,
            sub,
            obj,
            op,
        )
    }
}
