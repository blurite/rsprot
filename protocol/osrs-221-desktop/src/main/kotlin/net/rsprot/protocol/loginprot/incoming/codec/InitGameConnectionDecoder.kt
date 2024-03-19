package net.rsprot.protocol.loginprot.incoming.codec

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.loginprot.incoming.InitGameConnection
import net.rsprot.protocol.loginprot.incoming.prot.LoginClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class InitGameConnectionDecoder : MessageDecoder<InitGameConnection> {
    override val prot: ClientProt = LoginClientProt.INIT_GAME_CONNECTION

    override fun decode(buffer: JagByteBuf): InitGameConnection {
        return InitGameConnection
    }
}
