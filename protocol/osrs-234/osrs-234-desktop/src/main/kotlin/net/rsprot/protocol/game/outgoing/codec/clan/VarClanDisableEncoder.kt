package net.rsprot.protocol.game.outgoing.codec.clan

import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.clan.VarClanDisable
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.NoOpMessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class VarClanDisableEncoder : NoOpMessageEncoder<VarClanDisable> {
    override val prot: ServerProt = GameServerProt.VARCLAN_DISABLE
}
