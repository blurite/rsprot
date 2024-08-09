package net.rsprot.protocol.common.loginprot.incoming.codec

import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.crypto.xtea.XteaKey
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.common.loginprot.incoming.codec.shared.LoginBlockDecoder
import net.rsprot.protocol.common.loginprot.incoming.prot.LoginClientProt
import net.rsprot.protocol.loginprot.incoming.GameReconnect
import net.rsprot.protocol.message.codec.MessageDecoder
import java.math.BigInteger

public class GameReconnectDecoder(
    exp: BigInteger,
    mod: BigInteger,
) : LoginBlockDecoder<XteaKey>(exp, mod),
    MessageDecoder<GameReconnect> {
    override val prot: ClientProt = LoginClientProt.GAMERECONNECT

    override fun decode(buffer: JagByteBuf): GameReconnect {
        val copy = buffer.buffer.copy()
        // Mark the buffer as "read" as copy function doesn't do it automatically.
        buffer.buffer.readerIndex(buffer.buffer.writerIndex())
        return GameReconnect(copy.toJagByteBuf()) { jagByteBuf, betaWorld ->
            decodeLoginBlock(jagByteBuf, betaWorld)
        }
    }

    override fun decodeAuthentication(buffer: JagByteBuf): XteaKey =
        XteaKey(
            IntArray(4) {
                buffer.g4()
            },
        )
}
