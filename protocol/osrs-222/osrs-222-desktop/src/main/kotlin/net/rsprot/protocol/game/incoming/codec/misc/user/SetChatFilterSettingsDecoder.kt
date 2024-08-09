package net.rsprot.protocol.game.incoming.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.misc.user.SetChatFilterSettings
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class SetChatFilterSettingsDecoder : MessageDecoder<SetChatFilterSettings> {
    override val prot: ClientProt = GameClientProt.SET_CHATFILTERSETTINGS

    override fun decode(buffer: JagByteBuf): SetChatFilterSettings {
        val publicChatFilter = buffer.g1()
        val privateChatFilter = buffer.g1()
        val tradeChatFilter = buffer.g1()
        return SetChatFilterSettings(
            publicChatFilter,
            privateChatFilter,
            tradeChatFilter,
        )
    }
}
