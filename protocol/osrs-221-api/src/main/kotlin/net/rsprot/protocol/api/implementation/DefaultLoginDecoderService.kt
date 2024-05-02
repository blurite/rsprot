package net.rsprot.protocol.api.implementation

import net.rsprot.protocol.api.LoginDecoderService
import java.util.concurrent.CompletableFuture
import java.util.function.Function

public class DefaultLoginDecoderService : LoginDecoderService {
    override fun <Buf, Result> decode(
        buffer: Buf,
        decoder: Function<Buf, Result>,
    ): CompletableFuture<Result> {
        return CompletableFuture<Result>().completeAsync {
            decoder.apply(buffer)
        }
    }
}
