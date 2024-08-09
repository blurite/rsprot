package net.rsprot.protocol.game.incoming.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.misc.user.BugReport
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class BugReportDecoder : MessageDecoder<BugReport> {
    override val prot: ClientProt = GameClientProt.BUG_REPORT

    override fun decode(buffer: JagByteBuf): BugReport {
        val type = buffer.g1()
        val description = buffer.gjstr()
        val instructions = buffer.gjstr()
        check(description.length <= 500) {
            "Bug report description length cannot exceed 500 characters."
        }
        check(instructions.length <= 500) {
            "Bug report instructions length cannot exceed 500 characters."
        }
        return BugReport(
            type,
            description,
            instructions,
        )
    }
}
