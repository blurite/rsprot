package net.rsprot.protocol.message

import net.rsprot.protocol.ServerProtCategory

public interface OutgoingGameMessage : OutgoingMessage {
    public val category: ServerProtCategory
}
