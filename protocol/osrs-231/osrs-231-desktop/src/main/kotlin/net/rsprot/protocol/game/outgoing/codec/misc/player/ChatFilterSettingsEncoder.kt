package net.rsprot.protocol.game.outgoing.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.player.ChatFilterSettings
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class ChatFilterSettingsEncoder : MessageEncoder<ChatFilterSettings> {
    override val prot: ServerProt = GameServerProt.CHAT_FILTER_SETTINGS

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: ChatFilterSettings,
    ) {
        buffer.p1Alt1(message.publicChatFilter)
        buffer.p1(message.tradeChatFilter)
    }
}
