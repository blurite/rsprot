package net.rsprot.protocol.game.incoming.codec.clan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.clan.AffinedClanSettingsSetMutedFromChannel
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools

@Consistent
public class AffinedClanSettingsSetMutedFromChannelDecoder :
    MessageDecoder<AffinedClanSettingsSetMutedFromChannel> {
    override val prot: ClientProt = GameClientProt.AFFINEDCLANSETTINGS_SETMUTED_FROMCHANNEL

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): AffinedClanSettingsSetMutedFromChannel {
        val clanId = buffer.g1()
        val memberIndex = buffer.g2()
        val muted = buffer.g1() == 1
        val name = buffer.gjstr()
        return AffinedClanSettingsSetMutedFromChannel(
            name,
            clanId,
            memberIndex,
            muted,
        )
    }
}
