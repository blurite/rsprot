package net.rsprot.protocol.game.outgoing.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.player.SetMapFlagV1
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class SetMapFlagV1Encoder : MessageEncoder<SetMapFlagV1> {
    override val prot: ServerProt = GameServerProt.SET_MAP_FLAG_V1

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: SetMapFlagV1,
    ) {
        buffer.p1(message.xInBuildArea)
        buffer.p1(message.zInBuildArea)
    }
}
