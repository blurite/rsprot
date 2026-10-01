package net.rsprot.protocol.game.incoming.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.misc.user.ExitFreecam
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class ExitFreecamDecoder : MessageDecoder<ExitFreecam> {
    override val prot: ClientProt = GameClientProt.EXIT_FREECAM

    override fun decode(buffer: JagByteBuf): ExitFreecam = ExitFreecam
}
