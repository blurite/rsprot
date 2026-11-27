package net.rsprot.protocol.game.incoming

import net.rsprot.protocol.ClientProtCategory

public enum class GameClientProtCategory(
    override val id: Int,
) : ClientProtCategory {
    CLIENT_EVENT(0),
    USER_EVENT(1),
}
