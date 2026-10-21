package net.rsprot.protocol.api.implementation

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.api.LoginDecoderService
import net.rsprot.protocol.loginprot.incoming.util.LoginBlock
import net.rsprot.protocol.loginprot.incoming.util.LoginBlockDecodingFunction
import java.util.concurrent.CompletableFuture

/**
 * The default login decoder utilizing a ForkJoinPool to decode the login block.
 */
public class DefaultLoginDecoderService : LoginDecoderService {
    override fun <Result> decode(
        buffer: JagByteBuf,
        betaWorld: Boolean,
        header: LoginBlock.Header,
        decoder: LoginBlockDecodingFunction<Result>,
    ): CompletableFuture<LoginBlock<Result>> =
        CompletableFuture<LoginBlock<Result>>().completeAsync {
            decoder.decode(header, buffer, betaWorld)
        }

    override fun decodeHeader(
        buffer: JagByteBuf,
        decoder: LoginBlockDecodingFunction<*>,
    ): LoginBlock.Header {
        return decoder.decodeHeader(buffer)
    }
}
