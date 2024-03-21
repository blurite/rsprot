package net.rsprot.protocol

import kotlin.enums.enumEntries

public class ProtRepository<T : Prot> internal constructor(
    private val sizes: IntArray,
) {
    public fun getSize(opcode: Int): Int {
        return sizes[opcode]
    }

    public fun capacity(): Int {
        return sizes.size
    }

    public companion object {
        @ExperimentalStdlibApi
        public inline fun <reified P> of(): ProtRepository<P> where P : Prot, P : Enum<P> {
            val entries = enumEntries<P>()
            val builder = ProtRepositoryBuilder<P>(256)
            for (entry in entries) {
                builder.put(entry)
            }
            return builder.build()
        }
    }
}
