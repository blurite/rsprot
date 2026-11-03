@file:Suppress("DuplicatedCode")

package net.rsprot.protocol.game.outgoing.codec.group

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.group.GroupVarLong
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class GroupVarLongEncoder : MessageEncoder<GroupVarLong> {
    override val prot: ServerProt = GameServerProt.GROUP_VAR_LONG

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: GroupVarLong,
    ) {
        val update = message.update
        buffer.p1(update.index)
        buffer.p4(update.packedGroupVar)
        buffer.p8(update.variable.value)
    }
}
