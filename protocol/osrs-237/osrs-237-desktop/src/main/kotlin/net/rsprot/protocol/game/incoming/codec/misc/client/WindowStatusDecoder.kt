package net.rsprot.protocol.game.incoming.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.misc.client.WindowStatus
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class WindowStatusDecoder : MessageDecoder<WindowStatus> {
    override val prot: ClientProt = GameClientProt.WINDOW_STATUS

    override fun decode(buffer: JagByteBuf): WindowStatus {
        val windowMode = buffer.g1()
        val frameWidth = buffer.g2()
        val frameHeight = buffer.g2()
        return WindowStatus(
            windowMode,
            frameWidth,
            frameHeight,
        )
    }
}
