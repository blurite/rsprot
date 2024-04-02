package net.rsprot.protocol.game.outgoing.prot

import net.rsprot.protocol.Prot
import net.rsprot.protocol.ServerProt

public enum class GameServerProt(
    override val opcode: Int,
    override val size: Int,
) : ServerProt {
    PLAYER_INFO(GameServerProtId.PLAYER_INFO, Prot.VAR_SHORT),
}
