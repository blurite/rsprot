package net.rsprot.protocol.api

import java.util.concurrent.CompletableFuture
import java.util.function.Function

public interface LoginDecoderService {
    public fun <Buf, Result> decode(
        buffer: Buf,
        decoder: Function<Buf, Result>,
    ): CompletableFuture<Result>
}
