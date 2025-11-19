package net.rsprot.protocol.api.binary.writer

import net.rsprot.protocol.binary.BinaryBlob

/**
 * A binary blob writer that always writes the full stream, starting with the header and ending with
 * the last packet, no matter how many times it has previously been written.
 * It first writes into a temporary file in the same directory, with a '.' prefix, then attempts
 * to atomic-move it into the real path.
 *
 * Note that this writer does not make the necessary directories if they don't yet exist,
 * this is up to the user to ensure (ideally only once on server startup, rather than per operation here).
 *
 * @property retryCount the number of times to re-attempt to write the binary blob in case of
 * an error. If the count is or reaches zero, the error will be re-thrown and should be
 * handled by the user.
 */
public class BinaryBlobAtomicReplacementWriter(
    private val retryCount: Int = 2,
) : BinaryBlobWriter {
    override fun write(blob: BinaryBlob): Boolean {
        val array =
            blob.stream.fullSnapshotOrNull()
                ?: return false
        tempWriteAndAtomicReplace(blob.header.path, array, retryCount)
        return true
    }
}
