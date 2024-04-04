package net.rsprot.protocol.game.incoming.codec.clan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.clan.AffinedClanSettingsSetMutedFromChannelMessage
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class AffinedClanSettingsSetMutedFromChannelDecoder :
    MessageDecoder<AffinedClanSettingsSetMutedFromChannelMessage> {
    override val prot: ClientProt = GameClientProt.AFFINEDCLANSETTINGS_SETMUTED_FROMCHANNEL

    override fun decode(buffer: JagByteBuf): AffinedClanSettingsSetMutedFromChannelMessage {
        val clanId = buffer.g1()
        val memberIndex = buffer.g2()
        val muted = buffer.g1() == 1
        val name = buffer.gjstr()
        return AffinedClanSettingsSetMutedFromChannelMessage(
            name,
            clanId,
            memberIndex,
            muted,
        )
    }
}
