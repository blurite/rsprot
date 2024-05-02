package net.rsprot.protocol.message

import net.rsprot.protocol.ClientProtCategory

public interface IncomingGameMessage : IncomingMessage {
    public val category: ClientProtCategory
}
