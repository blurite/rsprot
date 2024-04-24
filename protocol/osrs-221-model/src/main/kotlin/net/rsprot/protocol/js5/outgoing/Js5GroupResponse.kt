package net.rsprot.protocol.js5.outgoing

import io.netty.buffer.ByteBuf
import io.netty.buffer.DefaultByteBufHolder
import net.rsprot.protocol.js5.incoming.Js5GroupRequest
import net.rsprot.protocol.message.OutgoingMessage
import java.io.File

/**
 * Js5 group responses are used to feed the cache to the client through the server.
 * @property response the type of the response that is being encoded.
 */
public class Js5GroupResponse(
    public val response: Js5GroupResponseType,
) : OutgoingMessage {
    /**
     * A common binding interface for all the possible JS5 group responses.
     */
    public sealed interface Js5GroupResponseType

    /**
     * An unprepared JS5 group response is a response that has not been spliced up
     * into 512 byte blocks, and instead just represents each buffer as it is in
     * the cache (excluding version, still).
     * These unprepared responses are less performant than the prepared counterparts,
     * as they require writing the responses in smaller blocks which requires numerous
     * calls rather than a single one.
     */
    public sealed interface UnpreparedJs5GroupResponse : Js5GroupResponseType

    /**
     * An unprepared byte buffer response.
     * This implementation does support XOR encryption.
     * @property request the request that was made, written as part of the response
     * so client can identify the initial request.
     * @param buffer the byte buffer of the group in the cache, excluding the version,
     * unless the group is part of the master index.
     * The buffer's writer index and reference count will not be changed by the JS5
     * implementation, as the buffers are intended to be written to numerous recipients.
     */
    public class UnpreparedJs5ByteBufGroupResponse(
        public val request: Js5GroupRequest,
        buffer: ByteBuf,
    ) : DefaultByteBufHolder(buffer), UnpreparedJs5GroupResponse

    /**
     * A prepared JS5 group response. These are responses which have been prepared to be
     * in the exact format that the client expects, meaning we can write the contents over
     * to the client in a single [ByteBuf.writeBytes] call, rather than needing to split
     * it into 512 byte blocks during the encoding.
     */
    public sealed interface PreparedJs5GroupResponse : Js5GroupResponseType

    /**
     * A prepared ByteBuf based JS5 group response. The byte buffer here is in the exact
     * format that the client expects, starting with the archive and group id,
     * with the blocks separated by 0xFF terminators.
     * This implementation does support XOR encryption.
     * @param buffer the byte buffer containing all the data to be written over to the client.
     * The buffer's writer index and reference count will not be changed by the JS5
     * implementation, as the buffers are intended to be written to numerous recipients.
     */
    public class PreparedJs5ByteBufGroupResponse(buffer: ByteBuf) :
        DefaultByteBufHolder(buffer), PreparedJs5GroupResponse

    /**
     * A prepared JS5 file-based group response. This response will make use of Netty's
     * FileRegion implementation, allowing for zero-copy responses.
     * This implementation is ideal for development purposes, as it avoids needing to
     * load up the entire JS5 cache into memory. It is however not the best for production,
     * as it is heavily bottlenecked by disk seek and read speeds. In-memory implementations
     * will likely outperform, even if they do need to perform a copy.
     * Furthermore, this implementation is not supported with IOUring sockets, nor
     * does it support XOR encryption.
     * @property file the file containing the fully prepared response to be written
     * to the client.
     */
    public class PreparedJs5FileGroupResponse(public val file: File) : PreparedJs5GroupResponse
}
