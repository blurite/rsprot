@file:Suppress("DuplicatedCode")

package net.rsprot.protocol.game.outgoing.codec.group

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.group.GroupVarInt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class GroupVarIntEncoder : MessageEncoder<GroupVarInt> {
    override val prot: ServerProt = GameServerProt.GROUP_VAR_INT

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: GroupVarInt,
    ) {
        val update = message.update
        buffer.p1(update.index)
        buffer.p4(update.packedGroupVar)
        buffer.p4(update.variable.value)
    }
}
