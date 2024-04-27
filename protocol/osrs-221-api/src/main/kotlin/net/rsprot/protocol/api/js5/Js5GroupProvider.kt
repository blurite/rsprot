package net.rsprot.protocol.api.js5

import io.netty.buffer.ByteBuf
import net.rsprot.protocol.api.js5.Js5GroupProvider.Js5GroupType
import net.rsprot.protocol.js5.outgoing.Js5GroupResponse
import java.io.RandomAccessFile

public sealed interface Js5GroupProvider<T : Js5GroupType> {
    public fun provide(
        archive: Int,
        group: Int,
    ): T

    public fun toJs5GroupResponse(
        input: T,
        offset: Int,
        length: Int,
    ): Js5GroupResponse

    public sealed interface Js5GroupType {
        public val length: Int
    }

    public class RandomAccessFileJs5GroupType(public val raf: RandomAccessFile) : Js5GroupType {
        override val length: Int
            get() = raf.length().toInt()
    }

    public class ByteBufJs5GroupType(public val buffer: ByteBuf) : Js5GroupType {
        override val length: Int
            get() = buffer.readableBytes()
    }
}
