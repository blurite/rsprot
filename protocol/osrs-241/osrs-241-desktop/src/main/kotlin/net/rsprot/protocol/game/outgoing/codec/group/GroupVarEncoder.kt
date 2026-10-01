@file:Suppress("DuplicatedCode")

package net.rsprot.protocol.game.outgoing.codec.group

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.group.GroupVar
import net.rsprot.protocol.game.outgoing.group.util.GroupVariable
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class GroupVarEncoder : MessageEncoder<GroupVar> {
    override val prot: ServerProt = GameServerProt.GROUP_VAR

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: GroupVar,
    ) {
        for (update in message.updates) {
            buffer.p1(update.index)
            buffer.p4(update.packedGroupVar)
            when (val variable = update.variable) {
                is GroupVariable.IntGroupVariable -> {
                    buffer.p4(variable.value)
                }
                is GroupVariable.LongGroupVariable -> {
                    buffer.p8(variable.value)
                }
                is GroupVariable.StringGroupVariable -> {
                    buffer.pjstr(variable.value)
                }
            }
        }
    }
}
