package net.rsprot.protocol.game.outgoing.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.specific.NpcSpotAnimSpecific
import net.rsprot.protocol.message.codec.MessageEncoder

public class NpcSpotAnimSpecificEncoder : MessageEncoder<NpcSpotAnimSpecific> {
    override val prot: ServerProt = GameServerProt.NPC_SPOTANIM_SPECIFIC

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: NpcSpotAnimSpecific,
    ) {
        buffer.p1Alt1(message.slot)
        buffer.p2(message.id)
        buffer.p2Alt3(message.index)
        buffer.p4((message.height shl 16) or message.delay)
    }
}
