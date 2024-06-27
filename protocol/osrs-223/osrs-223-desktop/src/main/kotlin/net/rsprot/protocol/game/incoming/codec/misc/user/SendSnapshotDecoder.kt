package net.rsprot.protocol.game.incoming.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.misc.user.SendSnapshot
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools

@Consistent
public class SendSnapshotDecoder : MessageDecoder<SendSnapshot> {
    override val prot: ClientProt = GameClientProt.SEND_SNAPSHOT

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): SendSnapshot {
        val name = buffer.gjstr()
        val ruleId = buffer.g1()
        val mute = buffer.g1() == 1
        return SendSnapshot(
            name,
            ruleId,
            mute,
        )
    }
}
