package net.rsprot.protocol.game.outgoing.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.player.RunClientScript
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent
import java.nio.CharBuffer

@Consistent
public class RunClientScriptEncoder : MessageEncoder<RunClientScript> {
    override val prot: ServerProt = GameServerProt.RUNCLIENTSCRIPT

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: RunClientScript,
    ) {
        val types = message.types
        val values = message.values
        buffer.pjstr(CharBuffer.wrap(types))
        val length = types.size
        for (i in (length - 1) downTo 0) {
            val type = types[i]
            if (type == 's') {
                buffer.pjstr(values[i] as String)
            } else {
                buffer.p4(values[i] as Int)
            }
        }
        buffer.p4(message.id)
    }
}
