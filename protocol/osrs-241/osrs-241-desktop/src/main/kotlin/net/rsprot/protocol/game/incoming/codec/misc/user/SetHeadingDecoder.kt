package net.rsprot.protocol.game.incoming.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.misc.user.SetHeading
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class SetHeadingDecoder : MessageDecoder<SetHeading> {
    override val prot: ClientProt = GameClientProt.SET_HEADING

    override fun decode(buffer: JagByteBuf): SetHeading {
        val heading = buffer.g1Alt1()
        return SetHeading(heading)
    }
}
