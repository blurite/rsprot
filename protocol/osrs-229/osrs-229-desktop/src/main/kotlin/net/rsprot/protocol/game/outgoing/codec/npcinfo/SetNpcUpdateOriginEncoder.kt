package net.rsprot.protocol.game.outgoing.codec.npcinfo

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.info.npcinfo.SetNpcUpdateOrigin
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class SetNpcUpdateOriginEncoder : MessageEncoder<SetNpcUpdateOrigin> {
    override val prot: ServerProt
        get() = GameServerProt.SET_NPC_UPDATE_ORIGIN

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: SetNpcUpdateOrigin,
    ) {
        buffer.p1(message.originX)
        buffer.p1(message.originZ)
    }
}
