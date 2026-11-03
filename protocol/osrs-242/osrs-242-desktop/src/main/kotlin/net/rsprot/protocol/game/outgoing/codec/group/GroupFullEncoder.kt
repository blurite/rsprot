@file:Suppress("DuplicatedCode")

package net.rsprot.protocol.game.outgoing.codec.group

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.group.GroupFull
import net.rsprot.protocol.game.outgoing.group.util.GroupVariable
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class GroupFullEncoder : MessageEncoder<GroupFull> {
    override val prot: ServerProt = GameServerProt.GROUP_FULL

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: GroupFull,
    ) {
        for (update in message.updates) {
            when (update) {
                is GroupFull.GroupDelete -> {
                    buffer.p1(update.index)
                    buffer.pSmart1or2null(-1)
                }
                is GroupFull.GroupAddChange -> {
                    buffer.p1(update.index)
                    buffer.pSmart1or2null(update.id)
                    buffer.p8(update.uid)

                    for (variable in update.groupVariables) {
                        when (variable) {
                            is GroupVariable.IntGroupVariable -> {
                                buffer.pVarInt2s(variable.value)
                            }
                            is GroupVariable.LongGroupVariable -> {
                                buffer.p8(variable.value)
                            }
                            is GroupVariable.StringGroupVariable -> {
                                buffer.pjstr(variable.value)
                            }
                        }
                    }

                    for (variable in update.groupMemberVariables) {
                        when (variable) {
                            is GroupVariable.IntGroupVariable -> {
                                buffer.pVarInt2s(variable.value)
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
        }
    }
}
