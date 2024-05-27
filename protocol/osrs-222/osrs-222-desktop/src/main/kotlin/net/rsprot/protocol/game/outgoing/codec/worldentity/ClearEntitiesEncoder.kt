package net.rsprot.protocol.game.outgoing.codec.worldentity

import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.worldentity.ClearEntities
import net.rsprot.protocol.message.codec.NoOpMessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class ClearEntitiesEncoder : NoOpMessageEncoder<ClearEntities> {
    override val prot: ServerProt = GameServerProt.CLEAR_ENTITIES
}
