package net.rsprot.protocol.game.incoming.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.misc.client.SoundJingleEnd
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools

@Consistent
public class SoundJingleEndDecoder : MessageDecoder<SoundJingleEnd> {
    override val prot: ClientProt = GameClientProt.SOUND_JINGLEEND

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): SoundJingleEnd {
        val jingle = buffer.g4()
        return SoundJingleEnd(jingle)
    }
}
