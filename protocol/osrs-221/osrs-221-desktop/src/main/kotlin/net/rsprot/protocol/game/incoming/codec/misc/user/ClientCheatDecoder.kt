package net.rsprot.protocol.game.incoming.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.misc.user.ClientCheat
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools

@Consistent
public class ClientCheatDecoder : MessageDecoder<ClientCheat> {
    override val prot: ClientProt = GameClientProt.CLIENT_CHEAT

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): ClientCheat {
        val command = buffer.gjstr()
        return ClientCheat(command)
    }
}
