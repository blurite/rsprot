package net.rsprot.protocol.api.binary.writer

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import kotlin.io.path.Path

/**
 * Writes the [array] into a file named ".path", then atomic-moves (replaces) it over to "path".
 * @param path the path to write the final file to.
 * @param array the data to write into the file.
 * @param retryCount the number of retry attempts to go through when an exception is thrown.
 * If the count reaches zero, the exception is rethrown to the caller.
 */
public fun tempWriteAndAtomicReplace(
    path: Path,
    array: ByteArray,
    retryCount: Int,
) {
    try {
        val file = path.toFile()
        val parent = path.parent
        val tempFileName = ".${file.name}"
        val tempPath =
            if (parent == null) {
                val root = path.root
                if (root == null) {
                    Path(tempFileName)
                } else {
                    root.resolve(tempFileName)
                }
            } else {
                parent.resolve(tempFileName)
            }
        Files.write(
            tempPath,
            array,
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
            StandardOpenOption.SYNC,
        )
        Files.move(tempPath, path, StandardCopyOption.ATOMIC_MOVE)
    } catch (t: Throwable) {
        if (retryCount > 0) {
            return tempWriteAndAtomicReplace(path, array, retryCount - 1)
        }
        throw t
    }
}

/**
 * Appends the [array] onto the [path] if some data already exists there, otherwise creates a new file with it.
 * This operation is NOT atomic, and it is possible for a power outage to corrupt the end of the file.
 * It will however not corrupt the earlier sections of it, keeping it still relatively usable,
 * as the file is a continuous stream of packets.
 * @param retryCount the number of retry attempts to go through when an exception is thrown.
 * If the count reaches zero, the exception is rethrown to the caller.
 */
public fun append(
    path: Path,
    array: ByteArray,
    retryCount: Int,
) {
    try {
        Files.write(
            path,
            array,
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND,
            StandardOpenOption.SYNC,
        )
    } catch (t: Throwable) {
        if (retryCount > 0) {
            return append(path, array, retryCount - 1)
        }
        throw t
    }
}
