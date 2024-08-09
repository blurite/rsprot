package net.rsprot.protocol.js5.outgoing

import io.netty.buffer.ByteBuf
import io.netty.buffer.DefaultByteBufHolder
import net.rsprot.protocol.message.OutgoingJs5Message
import java.io.RandomAccessFile

/**
 * Js5 group responses are used to feed the cache to the client through the server.
 * @property response the type of the response that is being encoded.
 */
public class Js5GroupResponse(
    public val response: Js5GroupResponseType,
) : OutgoingJs5Message {
    /**
     * A common binding interface for all the possible JS5 group responses.
     */
    public sealed interface Js5GroupResponseType {
        public val offset: Int
        public val limit: Int
    }

    /**
     * A prepared ByteBuf based JS5 group response. The byte buffer here is in the exact
     * format that the client expects, starting with the archive and group id,
     * with the blocks separated by 0xFF terminators.
     * This implementation does support XOR encryption.
     * @param buffer the byte buffer containing all the data to be written over to the client.
     * The buffer's writer index and reference count will not be changed by the JS5
     * implementation, as the buffers are intended to be written to numerous recipients.
     * @property offset the reader index offset, allowing us to only send a slice of the buffer
     */
    public class Js5ByteBufGroupResponse(
        buffer: ByteBuf,
        override val offset: Int,
        override val limit: Int,
    ) : DefaultByteBufHolder(buffer),
        Js5GroupResponseType {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            if (!super.equals(other)) return false

            other as Js5ByteBufGroupResponse

            if (offset != other.offset) return false
            if (limit != other.limit) return false

            return true
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + offset
            result = 31 * result + limit
            return result
        }

        override fun toString(): String =
            "Js5ByteBufGroupResponse(" +
                "offset=$offset, " +
                "limit=$limit" +
                ")"
    }

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
     * @property offset the file data offset, allowing us to only send a slice of the file
     */
    public class Js5FileGroupResponse(
        public val file: RandomAccessFile,
        override val offset: Int,
        override val limit: Int,
    ) : Js5GroupResponseType {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Js5FileGroupResponse

            if (file != other.file) return false
            if (offset != other.offset) return false
            if (limit != other.limit) return false

            return true
        }

        override fun hashCode(): Int {
            var result = file.hashCode()
            result = 31 * result + offset
            result = 31 * result + limit
            return result
        }

        override fun toString(): String =
            "Js5FileGroupResponse(" +
                "offset=$offset, " +
                "limit=$limit" +
                ")"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Js5GroupResponse

        return response == other.response
    }

    override fun hashCode(): Int = response.hashCode()

    override fun toString(): String = "Js5GroupResponse(response=$response)"
}
