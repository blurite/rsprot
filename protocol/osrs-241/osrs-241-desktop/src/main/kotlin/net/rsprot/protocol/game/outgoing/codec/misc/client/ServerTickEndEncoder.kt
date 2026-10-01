package net.rsprot.protocol.game.outgoing.codec.misc.client

import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.client.ServerTickEnd
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.NoOpMessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class ServerTickEndEncoder : NoOpMessageEncoder<ServerTickEnd> {
    override val prot: ServerProt = GameServerProt.SERVER_TICK_END
}
