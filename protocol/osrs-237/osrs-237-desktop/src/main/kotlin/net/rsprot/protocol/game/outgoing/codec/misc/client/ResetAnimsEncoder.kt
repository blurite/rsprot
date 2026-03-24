package net.rsprot.protocol.game.outgoing.codec.misc.client

import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.client.ResetAnims
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.NoOpMessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class ResetAnimsEncoder : NoOpMessageEncoder<ResetAnims> {
    override val prot: ServerProt = GameServerProt.RESET_ANIMS
}
