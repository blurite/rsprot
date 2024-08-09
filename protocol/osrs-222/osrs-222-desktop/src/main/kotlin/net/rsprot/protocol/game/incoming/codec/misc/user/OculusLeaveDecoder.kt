package net.rsprot.protocol.game.incoming.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.misc.user.OculusLeave
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class OculusLeaveDecoder : MessageDecoder<OculusLeave> {
    override val prot: ClientProt = GameClientProt.OCULUS_LEAVE

    override fun decode(buffer: JagByteBuf): OculusLeave = OculusLeave
}
