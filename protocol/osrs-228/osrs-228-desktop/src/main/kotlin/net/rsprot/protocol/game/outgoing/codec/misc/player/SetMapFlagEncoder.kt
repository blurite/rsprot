package net.rsprot.protocol.game.outgoing.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.player.SetMapFlag
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class SetMapFlagEncoder : MessageEncoder<SetMapFlag> {
    override val prot: ServerProt = GameServerProt.SET_MAP_FLAG

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: SetMapFlag,
    ) {
        buffer.p1(message.xInBuildArea)
        buffer.p1(message.zInBuildArea)
    }
}
