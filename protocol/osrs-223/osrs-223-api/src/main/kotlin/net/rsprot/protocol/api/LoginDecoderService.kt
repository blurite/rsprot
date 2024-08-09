package net.rsprot.protocol.api

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.loginprot.incoming.util.LoginBlock
import net.rsprot.protocol.loginprot.incoming.util.LoginBlockDecodingFunction
import java.util.concurrent.CompletableFuture

/**
 * The service behind decoding login blocks.
 * This is needed as the login blocks take a noticeable amount of time to decode,
 * and it may not be ideal to block the Netty threads from doing any work during
 * that time. Most of the time is taken up by the RSA deciphering,
 * which can take around a millisecond for a secure key.
 */
public interface LoginDecoderService {
    /**
     * Decodes a login block buffer using the [decoder] implementation.
     * The default implementation for login decoder utilizes a ForkJoinPool.
     * @param buffer the buffer to decode
     * @param betaWorld whether the login connection came from a beta world,
     * which means the decoding is only partial
     * @param decoder the decoder function used to turn the buffer into a login block
     * @return a completable future instance that may be completed on a different thread,
     * to avoid blocking Netty threads.
     */
    public fun <Result> decode(
        buffer: JagByteBuf,
        betaWorld: Boolean,
        decoder: LoginBlockDecodingFunction<Result>,
    ): CompletableFuture<LoginBlock<Result>>
}
