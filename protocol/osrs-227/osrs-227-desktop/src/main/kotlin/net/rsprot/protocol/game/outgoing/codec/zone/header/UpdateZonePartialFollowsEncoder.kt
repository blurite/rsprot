package net.rsprot.protocol.game.outgoing.codec.zone.header

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.zone.header.UpdateZonePartialFollows
import net.rsprot.protocol.message.codec.MessageEncoder

public class UpdateZonePartialFollowsEncoder : MessageEncoder<UpdateZonePartialFollows> {
    override val prot: ServerProt = GameServerProt.UPDATE_ZONE_PARTIAL_FOLLOWS

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: UpdateZonePartialFollows,
    ) {
        buffer.p1Alt3(message.level)
        buffer.p1Alt1(message.zoneX)
        buffer.p1(message.zoneZ)
    }
}
