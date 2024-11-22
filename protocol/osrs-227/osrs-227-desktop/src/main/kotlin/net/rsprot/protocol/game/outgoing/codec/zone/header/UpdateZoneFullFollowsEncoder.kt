package net.rsprot.protocol.game.outgoing.codec.zone.header

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.zone.header.UpdateZoneFullFollows
import net.rsprot.protocol.message.codec.MessageEncoder

public class UpdateZoneFullFollowsEncoder : MessageEncoder<UpdateZoneFullFollows> {
    override val prot: ServerProt = GameServerProt.UPDATE_ZONE_FULL_FOLLOWS

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: UpdateZoneFullFollows,
    ) {
        buffer.p1Alt1(message.level)
        buffer.p1(message.zoneZ)
        buffer.p1Alt2(message.zoneX)
    }
}
