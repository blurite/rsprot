package net.rsprot.protocol.binary

/**
 * A class for holding .bin file header and stream of packets.
 * @property header the header of the binary file.
 * @property stream the stream of unencrypted binary packets.
 */
public data class BinaryBlob(
    public val header: BinaryHeader,
    public val stream: BinaryStream,
) {
    /**
     * @return the number of readable bytes in this buffer.
     */
    public fun readableBytes(): Int {
        return stream.readableBytes()
    }
}
