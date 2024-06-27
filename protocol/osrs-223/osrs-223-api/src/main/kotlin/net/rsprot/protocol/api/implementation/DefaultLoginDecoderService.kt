package net.rsprot.protocol.api.implementation

import net.rsprot.protocol.api.LoginDecoderService
import java.util.concurrent.CompletableFuture
import java.util.function.BiFunction

/**
 * The default login decoder utilizing a ForkJoinPool to decode the login block.
 */
public class DefaultLoginDecoderService : LoginDecoderService {
    override fun <Buf, Result> decode(
        buffer: Buf,
        betaWorld: Boolean,
        decoder: BiFunction<Buf, Boolean, Result>,
    ): CompletableFuture<Result> {
        return CompletableFuture<Result>().completeAsync {
            decoder.apply(buffer, betaWorld)
        }
    }
}
