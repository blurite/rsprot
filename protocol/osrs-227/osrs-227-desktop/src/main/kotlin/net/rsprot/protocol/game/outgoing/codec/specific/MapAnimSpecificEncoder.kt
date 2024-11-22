package net.rsprot.protocol.game.outgoing.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.specific.MapAnimSpecific
import net.rsprot.protocol.message.codec.MessageEncoder

public class MapAnimSpecificEncoder : MessageEncoder<MapAnimSpecific> {
    override val prot: ServerProt = GameServerProt.MAP_ANIM_SPECIFIC

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: MapAnimSpecific,
    ) {
        buffer.p1Alt1(message.height)
        buffer.p2Alt2(message.id)
        buffer.p2Alt1(message.delay)
        buffer.p3Alt2(message.coordInBuildAreaPacked)
    }
}
