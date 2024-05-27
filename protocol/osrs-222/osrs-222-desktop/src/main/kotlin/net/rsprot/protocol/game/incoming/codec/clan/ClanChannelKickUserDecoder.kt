package net.rsprot.protocol.game.incoming.codec.clan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.clan.ClanChannelKickUser
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools

@Consistent
public class ClanChannelKickUserDecoder : MessageDecoder<ClanChannelKickUser> {
    override val prot: ClientProt = GameClientProt.CLANCHANNEL_KICKUSER

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): ClanChannelKickUser {
        val clanId = buffer.g1()
        val memberIndex = buffer.g2()
        val name = buffer.gjstr()
        return ClanChannelKickUser(
            name,
            clanId,
            memberIndex,
        )
    }
}
