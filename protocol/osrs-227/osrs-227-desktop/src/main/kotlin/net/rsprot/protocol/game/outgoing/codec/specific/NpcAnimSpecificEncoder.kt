package net.rsprot.protocol.game.outgoing.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.specific.NpcAnimSpecific
import net.rsprot.protocol.message.codec.MessageEncoder

public class NpcAnimSpecificEncoder : MessageEncoder<NpcAnimSpecific> {
    override val prot: ServerProt = GameServerProt.NPC_ANIM_SPECIFIC

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: NpcAnimSpecific,
    ) {
        buffer.p2Alt3(message.id)
        buffer.p1Alt3(message.delay)
        buffer.p2Alt3(message.index)
    }
}
