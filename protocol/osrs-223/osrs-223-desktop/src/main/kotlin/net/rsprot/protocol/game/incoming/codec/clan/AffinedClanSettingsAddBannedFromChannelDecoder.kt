package net.rsprot.protocol.game.incoming.codec.clan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.clan.AffinedClanSettingsAddBannedFromChannel
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class AffinedClanSettingsAddBannedFromChannelDecoder :
    MessageDecoder<AffinedClanSettingsAddBannedFromChannel> {
    override val prot: ClientProt = GameClientProt.AFFINEDCLANSETTINGS_ADDBANNED_FROMCHANNEL

    override fun decode(buffer: JagByteBuf): AffinedClanSettingsAddBannedFromChannel {
        val clanId = buffer.g1()
        val memberIndex = buffer.g2()
        val name = buffer.gjstr()
        return AffinedClanSettingsAddBannedFromChannel(
            name,
            clanId,
            memberIndex,
        )
    }
}
