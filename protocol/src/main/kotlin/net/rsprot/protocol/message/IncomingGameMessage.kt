package net.rsprot.protocol.message

import net.rsprot.protocol.ProtCategory

public interface IncomingGameMessage : IncomingMessage {
    public val category: ProtCategory
}
