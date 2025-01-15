package net.rsprot.protocol.game.outgoing.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.specific.NpcHeadIconSpecific
import net.rsprot.protocol.message.codec.MessageEncoder

public class NpcHeadIconSpecificEncoder : MessageEncoder<NpcHeadIconSpecific> {
    override val prot: ServerProt = GameServerProt.NPC_HEADICON_SPECIFIC

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: NpcHeadIconSpecific,
    ) {
        buffer.p2(message.index)
        buffer.p1Alt3(message.headIconSlot)
        buffer.p2Alt1(message.spriteIndex)
        buffer.p4Alt2(message.spriteGroup)
    }
}
