package net.rsprot.protocol.api.js5

import io.netty.buffer.ByteBuf
import net.rsprot.protocol.api.js5.Js5GroupProvider.Js5GroupType
import net.rsprot.protocol.js5.outgoing.Js5GroupResponse
import java.io.RandomAccessFile

/**
 * The group provider interface for JS5.
 */
public sealed interface Js5GroupProvider<T : Js5GroupType> {
    /**
     * Provides a JS5 group based on the input archive and group
     * @param archive the archive id requested by the client
     * @param group the group in that archive requested
     * @return a full JS5 group to be written to the client
     */
    public fun provide(
        archive: Int,
        group: Int,
    ): T

    /**
     * Turns a subsection of the input group type into a group response,
     * allowing only a section of the file to be written out, instead
     * of the whole thing.
     * @param input the input full JS5 group
     * @param offset the offset in number of bytes to begin the response at
     * @param length the number of bytes that this full JS5 group supports
     * @return a group response that can be written to the client
     */
    public fun toJs5GroupResponse(
        input: T,
        offset: Int,
        length: Int,
    ): Js5GroupResponse

    /**
     * A full JS5 group type interface
     */
    public sealed interface Js5GroupType {
        public val length: Int
    }

    /**
     * A JS5 group type based on a random access file, allowing the use of File Regions
     * in Netty to perform zero-copy at the cost of disk IO. This is helpful for local
     * development, where one can skip the opening of the JS5 cache altogether.
     * @property raf the random access file behind this group
     */
    public class RandomAccessFileJs5GroupType(public val raf: RandomAccessFile) : Js5GroupType {
        override val length: Int
            get() = raf.length().toInt()
    }

    /**
     * A traditional ByteBuf based Js5 group response.
     * @property buffer the byte buffer behind this group
     */
    public class ByteBufJs5GroupType(public val buffer: ByteBuf) : Js5GroupType {
        override val length: Int
            get() = buffer.readableBytes()
    }
}
