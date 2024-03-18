package net.rsprot.protocol

import kotlin.enums.enumEntries

public class ProtRepository internal constructor(
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
        public inline fun <reified T> of(): ProtRepository where T : Prot, T : Enum<T> {
            val entries = enumEntries<T>()
            val builder = ProtRepositoryBuilder(256)
            for (entry in entries) {
                builder.put(entry)
            }
            return builder.build()
        }
    }
}
