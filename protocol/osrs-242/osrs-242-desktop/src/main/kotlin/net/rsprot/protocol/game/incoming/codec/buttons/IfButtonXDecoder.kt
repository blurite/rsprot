package net.rsprot.protocol.game.incoming.codec.buttons

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.buttons.If3Button
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.util.gCombinedId

@Consistent
public class IfButtonXDecoder : MessageDecoder<If3Button> {
    override val prot: ClientProt = GameClientProt.IF_BUTTONX

    override fun decode(buffer: JagByteBuf): If3Button {
        val combinedId = buffer.gCombinedId()
        val sub = buffer.g2()
        val obj = buffer.g2()
        val op = buffer.g1()
        return If3Button(
            combinedId,
            sub,
            obj,
            op,
        )
    }
}
