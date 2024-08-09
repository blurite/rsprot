package net.rsprot.protocol.game.outgoing.codec.clan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.clan.VarClanEnable
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class VarClanEnableEncoder : MessageEncoder<VarClanEnable> {
    override val prot: ServerProt = GameServerProt.VARCLAN_ENABLE

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: VarClanEnable,
    ) {
    }
}
