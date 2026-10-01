package net.rsprot.protocol.game.outgoing.codec.misc.player

import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.player.TriggerOnDialogAbort
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.NoOpMessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class TriggerOnDialogAbortEncoder : NoOpMessageEncoder<TriggerOnDialogAbort> {
    override val prot: ServerProt = GameServerProt.TRIGGER_ONDIALOGABORT
}
