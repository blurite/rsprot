package net.rsprot.protocol.loginprot.incoming.util

import net.rsprot.buffer.JagByteBuf

public fun interface LoginBlockDecodingFunction<T> {
    public fun decode(
        buffer: JagByteBuf,
        betaWorld: Boolean,
    ): LoginBlock<T>
}
