package net.rsprot.protocol.common.loginprot.incoming.codec

import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.crypto.xtea.XteaKey
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.common.loginprot.incoming.codec.shared.LoginBlockDecoder
import net.rsprot.protocol.common.loginprot.incoming.prot.LoginClientProt
import net.rsprot.protocol.loginprot.incoming.GameReconnect
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools
import java.math.BigInteger

public class GameReconnectDecoder(
    exp: BigInteger,
    mod: BigInteger,
) : MessageDecoder<GameReconnect>, LoginBlockDecoder<XteaKey>(exp, mod) {
    override val prot: ClientProt = LoginClientProt.GAMERECONNECT

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): GameReconnect {
        val copy = buffer.buffer.copy()
        // Mark the buffer as "read" as copy function doesn't do it automatically.
        buffer.buffer.readerIndex(buffer.buffer.writerIndex())
        return GameReconnect(copy.toJagByteBuf()) {
            decodeLoginBlock(it)
        }
    }

    override fun decodeAuthentication(buffer: JagByteBuf): XteaKey {
        return XteaKey(
            IntArray(4) {
                buffer.g4()
            },
        )
    }
}
