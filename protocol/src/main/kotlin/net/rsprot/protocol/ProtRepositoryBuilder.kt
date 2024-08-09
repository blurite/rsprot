package net.rsprot.protocol

public class ProtRepositoryBuilder<P : Prot>(
    capacity: Int,
) {
    private val sizes: IntArray = IntArray(capacity)

    public fun put(prot: Prot) {
        sizes[prot.opcode] = prot.size
    }

    public fun build(): ProtRepository<P> = ProtRepository(sizes.copyOf())
}
