package net.rsprot.protocol.loginprot.incoming.util

import net.rsprot.buffer.JagByteBuf

public interface LoginBlockDecodingFunction<T> {
    public fun decode(
        header: LoginBlock.Header,
        buffer: JagByteBuf,
        betaWorld: Boolean,
    ): LoginBlock<T>

    public fun decodeHeader(buffer: JagByteBuf): LoginBlock.Header
}
