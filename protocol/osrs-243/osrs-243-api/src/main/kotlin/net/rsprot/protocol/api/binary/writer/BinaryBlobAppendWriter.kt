package net.rsprot.protocol.api.binary.writer

import net.rsprot.protocol.binary.BinaryBlob

/**
 * A binary blob writer that appends to an existing file. This writer is not atomic, however, a power outage
 * will not make it completely unusable, as it would only corrupt the file ending, allowing the
 * earlier sections to still be transcribed.
 *
 * Note that this writer does not make the necessary directories if they don't yet exist,
 * this is up to the user to ensure (ideally only once on server startup, rather than per operation here).
 *
 * This writer is preferred to [BinaryBlobAtomicReplacementWriter] as it avoids keeping huge slabs
 * of data in memory for extended periods of time. With 2,000 players, it is reasonable to expect
 * the binary blobs to occupy up to ~10gb of heap under these buffers. The appending writer avoids
 * this by taking a snapshot of the buffer and resetting the pointers, avoiding the buffer
 * from ever-growing too large. This comes at the cost of losing atomicity.
 * @property retryCount the number of times to re-attempt to write the binary blob in case of
 * an error. If the count is or reaches zero, the error will be re-thrown and should be
 * handled by the user.
 */
public class BinaryBlobAppendWriter(
    private val retryCount: Int = 2,
) : BinaryBlobWriter {
    override fun write(blob: BinaryBlob): Boolean {
        val array =
            blob.stream.incrementalSnapshotOrNull()
                ?: return false
        append(blob.header.path, array, retryCount)
        return true
    }
}
