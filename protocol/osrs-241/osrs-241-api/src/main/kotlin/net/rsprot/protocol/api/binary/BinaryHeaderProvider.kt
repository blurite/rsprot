package net.rsprot.protocol.api.binary

import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.min

/**
 * An interface to provide the missing parts of a binary header.
 */
public fun interface BinaryHeaderProvider {
    /**
     * Provides the missing partial details of a binary header, or null if a binary blob should
     * not be generated for this user.
     *
     * Note: There are standard helpers in [BinaryHeaderProvider.Companion] to construct a RSProx-like
     * file name. This can be decorated with the player name in-front of it, for example.
     *
     * @param playerIndex the index of the player that is logging in. This function is invoked
     * right before the [net.rsprot.protocol.api.Session] object is provided to the server. This index
     * allows the server to look up the player in the player-list and yield their name, for example,
     * for cleaner binary files.
     * @param timestamp the epoch time milliseconds when the binary header is being constructed.
     * @param accountHash an SHA-256 hash of the user id and user hash.
     * @return the partial binary header, or null if the binary blob should be skipped for this user.
     */
    public fun provide(
        playerIndex: Int,
        timestamp: Long,
        accountHash: ByteArray,
    ): PartialBinaryHeader?

    public companion object {
        private const val BINARY_EXTENSION: String = "bin"
        private val FILE_NAME_DATE_FORMATTER = SimpleDateFormat("yyyyMMdd'T'HHmmss")

        @JvmStatic
        public fun fileName(
            timestamp: Long,
            accountHash: ByteArray,
        ): String {
            return "${fileNameWithoutSuffix(timestamp, accountHash)}.$BINARY_EXTENSION"
        }

        @JvmStatic
        @OptIn(ExperimentalStdlibApi::class)
        public fun fileNameWithoutSuffix(
            timestamp: Long,
            accountHash: ByteArray,
        ): String {
            val date = Date(timestamp)
            val formattedDate = FILE_NAME_DATE_FORMATTER.format(date)
            val hexHash = accountHash.toHexString(HexFormat.Default)
            val shortHash = hexHash.substring(0, min(7, hexHash.length))
            return "$formattedDate-$shortHash"
        }
    }
}
