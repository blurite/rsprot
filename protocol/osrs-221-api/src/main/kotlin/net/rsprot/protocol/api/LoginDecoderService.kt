package net.rsprot.protocol.api

import java.util.concurrent.CompletableFuture
import java.util.function.BiFunction

public interface LoginDecoderService {
    public fun <Buf, Result> decode(
        buffer: Buf,
        betaWorld: Boolean,
        decoder: BiFunction<Buf, Boolean, Result>,
    ): CompletableFuture<Result>
}
