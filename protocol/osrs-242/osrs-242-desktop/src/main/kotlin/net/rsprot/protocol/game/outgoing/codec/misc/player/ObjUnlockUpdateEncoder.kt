package net.rsprot.protocol.game.outgoing.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.player.ObjUnlockUpdate
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class ObjUnlockUpdateEncoder : MessageEncoder<ObjUnlockUpdate> {
    override val prot: ServerProt = GameServerProt.OBJUNLOCK_UPDATE

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: ObjUnlockUpdate,
    ) {
        val entries = message.entries.toSortedMap()
        var previousIndex = 0
        for (index in entries.keys) {
            require(index.toLong() - previousIndex in 0L..32767L) {
                "Obj unlock word-index delta must fit an unsigned smart: $index - $previousIndex"
            }
            previousIndex = index
        }
        previousIndex = 0
        for ((index, flags) in entries) {
            buffer.pSmart1or2(index - previousIndex)
            buffer.p8(flags)
            previousIndex = index
        }
    }
}
