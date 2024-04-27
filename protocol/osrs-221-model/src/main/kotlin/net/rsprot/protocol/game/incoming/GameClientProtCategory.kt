package net.rsprot.protocol.game.incoming

import net.rsprot.protocol.ProtCategory

public enum class GameClientProtCategory(
    override val id: Int,
    override val limit: Int,
) : ProtCategory {
    CLIENT_EVENT(0, 50),
    USER_EVENT(1, 10),
}
