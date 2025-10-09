package net.rsprot.protocol.game.outgoing.info.worldentityinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.game.outgoing.info.AvatarExtendedInfoWriter
import net.rsprot.protocol.internal.RSProtFlags
import net.rsprot.protocol.internal.checkCommunicationThread
import net.rsprot.protocol.internal.game.outgoing.info.precompute
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.VisibleOps
import net.rsprot.protocol.internal.game.outgoing.info.worldentityinfo.encoder.WorldEntityExtendedInfoEncoders

public typealias WorldEntityAvatarExtendedInfoWriter =
    AvatarExtendedInfoWriter<WorldEntityExtendedInfoEncoders, WorldEntityAvatarExtendedInfoBlocks>

/**
 * World entity avatar extended info is a data structure used to keep track of all the extended info
 * properties of the given avatar.
 */
public class WorldEntityAvatarExtendedInfo(
    private var avatarIndex: Int,
    extendedInfoWriters: List<WorldEntityAvatarExtendedInfoWriter>,
    private val allocator: ByteBufAllocator,
    private val huffmanCodec: HuffmanCodecProvider,
) {
    /**
     * The extended info blocks enabled on this World Entity in a given cycle.
     */
    internal var flags: Int = 0

    /**
     * Extended info blocks used to transmit changes to the client,
     * wrapped in its own class as we must pass this onto the client-specific
     * implementations.
     */
    private val blocks: WorldEntityAvatarExtendedInfoBlocks = WorldEntityAvatarExtendedInfoBlocks(extendedInfoWriters)

    /**
     * The client-specific extended info writers, indexed by the respective [OldSchoolClientType]'s id.
     * All clients in use must be registered, or an exception will occur during world entity info encoding.
     */
    private val writers: Array<WorldEntityAvatarExtendedInfoWriter?> =
        buildClientWriterArray(extendedInfoWriters)

    /**
     * Sets the visible ops flag of this World Entity to the provided value.
     * @param flag the bit flag to set. Only the 5 lowest bits are used,
     * and an enabled bit implies the option at that index should render.
     * Note that this extended info block is not transient and will be transmitted to
     * future players as well.
     *
     * Use [net.rsprot.protocol.game.outgoing.util.OpFlags] class to build the flag.
     */
    public fun setVisibleOps(flag: Byte) {
        checkCommunicationThread()
        blocks.visibleOps.ops = flag.toUByte()
        flags = flags or VISIBLE_OPS
    }

    /**
     * Clears any transient information and resets the flag to zero at the end of the cycle.
     */
    internal fun postUpdate() {
        clearTransientExtendedInformation()
        flags = 0
    }

    /**
     * Resets all the properties of this extended info object, making it ready for use
     * by another avatar.
     */
    internal fun reset() {
        flags = 0
        blocks.visibleOps.clear()
    }

    /**
     * Checks if the avatar has any extended info flagged.
     * @return whether any extended info flags are set.
     */
    internal fun hasExtendedInfo(): Boolean {
        return this.flags != 0
    }

    /**
     * Pre-computes all the buffers for this avatar.
     * Pre-computation is done, so we don't have to calculate these extended info blocks
     * for every avatar that observes us. Instead, we can do more performance-efficient
     * operations of native memory copying to get the latest extended info blocks.
     */
    internal fun precompute() {
        precomputeCached()
    }

    /**
     * Precomputes the extended info blocks which are cached and potentially transmitted
     * to any players who newly observe this world entity. The full list of extended info blocks
     * which must be placed in here is seen in [getLowToHighResChangeExtendedInfoFlags].
     * Every condition there must be among this function, else it is possible to run into
     * scenarios where a block isn't computed but is required in the future.
     */
    internal fun precomputeCached() {
        if (flags and VISIBLE_OPS != 0) {
            blocks.visibleOps.precompute(allocator, huffmanCodec)
        }
    }

    /**
     * Writes the extended info block of this avatar for the given observer.
     * @param oldSchoolClientType the client that the observer is using.
     * @param buffer the buffer into which the extended info block should be written.
     * @param observerIndex index of the player avatar that is observing us.
     */
    internal fun pExtendedInfo(
        oldSchoolClientType: OldSchoolClientType,
        buffer: JagByteBuf,
        observerIndex: Int,
        extraFlag: Int,
    ) {
        val flag = this.flags or extraFlag
        val writer =
            requireNotNull(writers[oldSchoolClientType.id]) {
                "Extended info writer missing for client $oldSchoolClientType"
            }

        writer.pExtendedInfo(
            buffer,
            avatarIndex,
            observerIndex,
            flag,
            blocks,
        )
    }

    /**
     * Gets the set of extended info blocks that were previously set but also
     * need to be transmitted to any new users.
     * @return the bit flag of all the non-transient extended info blocks that were previously flagged.
     */
    internal fun getLowToHighResChangeExtendedInfoFlags(): Int {
        var flag = 0
        if (this.flags and VISIBLE_OPS == 0 &&
            blocks.visibleOps.ops != VisibleOps.DEFAULT_OPS
        ) {
            flag = flag or VISIBLE_OPS
        }
        return flag
    }

    /**
     * Clears any transient extended info that was flagged in this cycle.
     */
    private fun clearTransientExtendedInformation() {
        val flags = this.flags
        if (flags == 0) return
        // None right now
    }

    public companion object {
        public const val UNKNOWN: Int = 0x1
        public const val VISIBLE_OPS: Int = 0x2

        /**
         * Executes the [block] if input verification is enabled,
         * otherwise does nothing. Verification should be enabled for
         * development environments, to catch problems mid-development.
         * In production, or during benchmarking, verification should be disabled,
         * as there is still some overhead to running verifications.
         */
        private inline fun verify(crossinline block: () -> Unit) {
            if (RSProtFlags.extendedInfoInputVerification) {
                block()
            }
        }

        /**
         * Builds an extended info writer array indexed by provided client types.
         * All client types which are utilized must be registered to avoid runtime errors.
         */
        private fun buildClientWriterArray(
            extendedInfoWriters: List<WorldEntityAvatarExtendedInfoWriter>,
        ): Array<WorldEntityAvatarExtendedInfoWriter?> {
            val array =
                arrayOfNulls<WorldEntityAvatarExtendedInfoWriter>(
                    OldSchoolClientType.COUNT,
                )
            for (writer in extendedInfoWriters) {
                array[writer.oldSchoolClientType.id] = writer
            }
            return array
        }
    }
}
