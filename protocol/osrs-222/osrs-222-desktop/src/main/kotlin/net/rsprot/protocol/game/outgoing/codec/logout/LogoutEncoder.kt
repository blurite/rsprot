package net.rsprot.protocol.game.outgoing.codec.logout

import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.logout.Logout
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.NoOpMessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class LogoutEncoder : NoOpMessageEncoder<Logout> {
    override val prot: ServerProt = GameServerProt.LOGOUT
}
