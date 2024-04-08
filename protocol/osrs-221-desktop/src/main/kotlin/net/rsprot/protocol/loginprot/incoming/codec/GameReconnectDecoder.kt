package net.rsprot.protocol.loginprot.incoming.codec

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.util.XteaKey
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.loginprot.incoming.GameReconnect
import net.rsprot.protocol.loginprot.incoming.codec.shared.LoginBlockDecoder
import net.rsprot.protocol.loginprot.incoming.prot.LoginClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools
import java.math.BigInteger

public class GameReconnectDecoder(
    exp: BigInteger,
    mod: BigInteger,
) : MessageDecoder<GameReconnect>, LoginBlockDecoder<XteaKey>(exp, mod) {
    override val prot: ClientProt = LoginClientProt.GAMELOGIN

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): GameReconnect {
        return GameReconnect(decodeLoginBlock(buffer))
    }

    override fun decodeAuthentication(buffer: JagByteBuf): XteaKey {
        return XteaKey(
            IntArray(4) {
                buffer.g4()
            },
        )
    }
}
