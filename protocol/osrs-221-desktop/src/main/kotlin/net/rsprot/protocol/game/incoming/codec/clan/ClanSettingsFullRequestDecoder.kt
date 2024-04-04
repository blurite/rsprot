package net.rsprot.protocol.game.incoming.codec.clan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.clan.ClanSettingsFullRequestMessage
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools

@Consistent
public class ClanSettingsFullRequestDecoder : MessageDecoder<ClanSettingsFullRequestMessage> {
    override val prot: ClientProt = GameClientProt.CLANSETTINGS_FULL_REQUEST

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): ClanSettingsFullRequestMessage {
        val clanId = buffer.g1s()
        return ClanSettingsFullRequestMessage(clanId)
    }
}
