package net.rsprot.protocol.game.outgoing.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.player.SetMapFlagV2
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class SetMapFlagV2Encoder : MessageEncoder<SetMapFlagV2> {
    override val prot: ServerProt = GameServerProt.SET_MAP_FLAG_V2

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: SetMapFlagV2,
    ) {
        buffer.p4(message.coordGrid.packed)
    }
}
