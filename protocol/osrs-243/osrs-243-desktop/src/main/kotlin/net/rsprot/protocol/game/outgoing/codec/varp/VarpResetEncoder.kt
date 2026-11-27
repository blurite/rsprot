package net.rsprot.protocol.game.outgoing.codec.varp

import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.varp.VarpReset
import net.rsprot.protocol.message.codec.NoOpMessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class VarpResetEncoder : NoOpMessageEncoder<VarpReset> {
    override val prot: ServerProt = GameServerProt.VARP_RESET
}
