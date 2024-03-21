package net.rsprot.protocol.js5.outgoing

import io.netty.buffer.ByteBuf
import io.netty.buffer.DefaultByteBufHolder
import net.rsprot.protocol.js5.incoming.Js5GroupRequest
import net.rsprot.protocol.message.OutgoingMessage

public class Js5GroupResponse(
    public val request: Js5GroupRequest,
    buffer: ByteBuf,
) : OutgoingMessage, DefaultByteBufHolder(buffer)
