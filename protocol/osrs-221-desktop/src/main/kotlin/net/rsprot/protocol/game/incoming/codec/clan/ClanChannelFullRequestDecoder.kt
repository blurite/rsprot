package net.rsprot.protocol.game.incoming.codec.clan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.clan.ClanChannelFullRequestMessage
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools

@Consistent
public class ClanChannelFullRequestDecoder : MessageDecoder<ClanChannelFullRequestMessage> {
    override val prot: ClientProt = GameClientProt.CLANCHANNEL_FULL_REQUEST

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): ClanChannelFullRequestMessage {
        val clanId = buffer.g1s()
        return ClanChannelFullRequestMessage(clanId)
    }
}
