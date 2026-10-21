package net.rsprot.protocol.game.outgoing.codec.camera

import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.camera.CamReset
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.NoOpMessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class CamResetEncoder : NoOpMessageEncoder<CamReset> {
    override val prot: ServerProt = GameServerProt.CAM_RESET
}
