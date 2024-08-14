package net.rsprot.protocol.game.outgoing.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.player.UpdateRunEnergy
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class UpdateRunEnergyEncoder : MessageEncoder<UpdateRunEnergy> {
    override val prot: ServerProt = GameServerProt.UPDATE_RUNENERGY

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: UpdateRunEnergy,
    ) {
        buffer.p2(message.runenergy)
    }
}
