package net.rsprot.protocol

import kotlin.enums.enumEntries

public class ProtRepository<T : Prot> internal constructor(
    private val sizes: IntArray,
) {
    public fun getSize(opcode: Int): Int = sizes[opcode]

    public fun capacity(): Int = sizes.size

    public companion object {
        @ExperimentalStdlibApi
        public inline fun <reified P> of(): ProtRepository<P> where P : Prot, P : Enum<P> {
            val entries = enumEntries<P>()
            val builder = ProtRepositoryBuilder<P>(256)
            for (entry in entries) {
                // Skip the packets with opcode -1 - this implies the packet cannot be sent directly,
                // and must be part of another packet (like partial enclosed zone updates)
                if (entry.opcode == -1) continue
                builder.put(entry)
            }
            return builder.build()
        }
    }
}
