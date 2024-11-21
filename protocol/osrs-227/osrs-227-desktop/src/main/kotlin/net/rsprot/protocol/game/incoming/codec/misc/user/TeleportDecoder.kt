package net.rsprot.protocol.game.incoming.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.misc.user.Teleport
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class TeleportDecoder : MessageDecoder<Teleport> {
    override val prot: ClientProt = GameClientProt.TELEPORT

    override fun decode(buffer: JagByteBuf): Teleport {
        val level = buffer.g1Alt2()
        val oculusSyncValue = buffer.g4Alt2()
        val z = buffer.g2Alt1()
        val x = buffer.g2Alt1()
        return Teleport(
            oculusSyncValue,
            x,
            z,
            level,
        )
    }
}
