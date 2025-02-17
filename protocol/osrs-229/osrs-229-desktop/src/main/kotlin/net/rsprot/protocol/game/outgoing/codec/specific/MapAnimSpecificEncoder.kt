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
        buffer.p3Alt2(message.coordInBuildAreaPacked)
        buffer.p2(message.delay)
        buffer.p1Alt3(message.height)
        buffer.p2(message.id)
    }
}
