package net.rsprot.protocol.game.outgoing.codec.clan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.clan.VarClanDisable
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class VarClanDisableEncoder : MessageEncoder<VarClanDisable> {
    override val prot: ServerProt = GameServerProt.VARCLAN_DISABLE

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: VarClanDisable,
    ) {
    }
}
