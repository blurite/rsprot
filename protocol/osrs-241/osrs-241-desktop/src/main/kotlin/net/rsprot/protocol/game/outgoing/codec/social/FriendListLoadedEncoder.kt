package net.rsprot.protocol.game.outgoing.codec.social

import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.social.FriendListLoaded
import net.rsprot.protocol.message.codec.NoOpMessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class FriendListLoadedEncoder : NoOpMessageEncoder<FriendListLoaded> {
    override val prot: ServerProt = GameServerProt.FRIENDLIST_LOADED
}
