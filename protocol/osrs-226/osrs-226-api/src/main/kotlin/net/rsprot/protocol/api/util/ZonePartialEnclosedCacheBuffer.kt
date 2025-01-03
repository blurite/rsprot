package net.rsprot.protocol.api.util

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.PooledByteBufAllocator
import net.rsprot.protocol.common.client.ClientTypeMap
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.game.outgoing.codec.zone.header.DesktopUpdateZonePartialEnclosedEncoder
import net.rsprot.protocol.message.ZoneProt
import net.rsprot.protocol.message.codec.UpdateZonePartialEnclosedCache
import java.util.EnumMap
import java.util.LinkedList

public class ZonePartialEnclosedCacheBuffer
    @JvmOverloads
    public constructor(
        public val supportedClients: List<OldSchoolClientType> = OldSchoolClientType.entries,
        private val byteBufAllocator: ByteBufAllocator = PooledByteBufAllocator.DEFAULT,
        internal var activeCachedBuffers: LinkedList<ByteBuf> = LinkedList(),
        public val zoneCountBeforeLeakWarning: Int = DEFAULT_ZONE_COUNT_BEFORE_LEAK_WARNING,
        public val bufRetentionCountBeforeRelease: Int = DEFAULT_BUF_RETENTION_COUNT_BEFORE_RELEASE,
    ) {
        /**
         * Contains lists of buffers from [computeZone] calls that were unable to be released due to their [ByteBuf.refCnt].
         */
        internal val retainedBufferReferences = ArrayDeque<LinkedList<ByteBuf>>()

        /**
         * The amount of [computeZone] calls that have been made before calling [releaseBuffers]. In other words, tracks
         * the amount of zones that have been computed in a single tick. (or multiple ticks if the consumer does not
         * properly call [releaseBuffers])
         */
        internal var currentZoneComputationCount: Int = 0

        /**
         * Computes the expected [net.rsprot.protocol.game.outgoing.zone.header.UpdateZonePartialEnclosed.payload] for each
         * client type in [supportedClients] and returns them in map with the [OldSchoolClientType] as key and payload byte
         * buffer as its value.
         *
         * The [pendingTickProtList] parameter should be a list of zone prot events that occurred during the _current world
         * cycle_. It **should not** include prots from previous cycles, such as `LocAddChange` and `ObjAdd` from previously
         * spawned locs or objs, respectively.
         *
         * Zone partial enclosed can include the following, when applicable:
         * - [net.rsprot.protocol.game.outgoing.zone.payload.LocAddChange]
         * - [net.rsprot.protocol.game.outgoing.zone.payload.LocAnim]
         * - [net.rsprot.protocol.game.outgoing.zone.payload.LocDel]
         * - [net.rsprot.protocol.game.outgoing.zone.payload.LocMerge]
         * - [net.rsprot.protocol.game.outgoing.zone.payload.MapAnim]
         * - [net.rsprot.protocol.game.outgoing.zone.payload.MapProjAnim]
         * - [net.rsprot.protocol.game.outgoing.zone.payload.ObjAdd]: *Only for "publicly-visible" objs*
         * - [net.rsprot.protocol.game.outgoing.zone.payload.ObjDel]: *Only for "publicly-visible" objs*
         * - [net.rsprot.protocol.game.outgoing.zone.payload.ObjEnabledOps]
         * - [net.rsprot.protocol.game.outgoing.zone.payload.SoundArea]
         */
        public fun <T : ZoneProt> computeZone(
            pendingTickProtList: Collection<T>,
        ): EnumMap<OldSchoolClientType, ByteBuf> {
            val clientBuffers = buildZoneProtBuffers(pendingTickProtList)
            activeCachedBuffers += clientBuffers.values
            incrementZoneComputationCount()
            return clientBuffers
        }

        /**
         * Computes the expected zone payload **only** for a single [OldSchoolClientType] and returns the corresponding
         * [ByteBuf].
         *
         * Similar to [computeZone], but for a single client rather than all [supportedClients].
         */
        public fun <T : ZoneProt> computeZoneForClient(
            client: OldSchoolClientType,
            pendingTickProtList: List<T>,
        ): ByteBuf {
            val encoder = supportedEncoders[client]

            val buffer = encoder.buildCache(byteBufAllocator, pendingTickProtList)
            activeCachedBuffers.add(buffer)
            incrementZoneComputationCount()

            return buffer
        }

        private fun <T : ZoneProt> buildZoneProtBuffers(
            protList: Collection<T>,
        ): EnumMap<OldSchoolClientType, ByteBuf> {
            val map = createClientBufferEnumMap()
            for (client in supportedClients) {
                val encoder = supportedEncoders.getOrNull(client) ?: continue
                val buffer = encoder.buildCache(byteBufAllocator, protList)
                map[client] = buffer
            }
            return map
        }

        private fun incrementZoneComputationCount() {
            currentZoneComputationCount++
            logPossibleLeak()
        }

        private fun logPossibleLeak() {
            if (currentZoneComputationCount < zoneCountBeforeLeakWarning) {
                return
            }
            logger.warn { "Update zone partial enclosed buffers have not been correctly released!" }
        }

        /**
         * Releases all prebuilt zone partial enclosed buffers that no longer have active references, indicating that all
         * Netty channels have finished writing these buffers. This method also handles buffers that could not be
         * immediately released due to their reference count ([ByteBuf.refCnt]).
         *
         * Under typical conditions, the encoder should trigger the buffer release within a single cycle. However, if a
         * buffer remains unreleased due to a session closing or other interruptions, this method ensures they are
         * handled correctly. Implementation details on this mechanism can be seen in [releaseBuffersOnThreshold].
         *
         * **Usage Note:** This function should be invoked **once** at the end of **every tick** to ensure proper buffer
         * cleanup and prevent possible memory leaks.
         */
        public fun releaseBuffers() {
            resetComputationCount()
            releaseBuffersOnThreshold()
            retainActiveBufferReferences()
            releaseRetainedBuffers()
            clearEmptyRetainedBuffers()
        }

        private fun resetComputationCount() {
            currentZoneComputationCount = 0
        }

        /**
         * Checks and forcibly releases retained buffers if the number of unreleased buffers exceeds a predefined threshold.
         *
         * - **Periodic Forcible Release**: If the total number of retained buffers that could not be released reaches
         * 100 ([bufRetentionCountBeforeRelease]), this method will begin to forcibly release these buffers during
         * each [releaseBuffers] call to prevent memory leaks.
         *
         * This mechanism is a safeguard to ensure that buffers are eventually released even in cases where they were not
         * properly released due to reference count issues. If this mechanism is triggered, it suggests a deeper
         * underlying issue.
         */
        private fun releaseBuffersOnThreshold() {
            if (retainedBufferReferences.size >= bufRetentionCountBeforeRelease) {
                val releaseTarget = retainedBufferReferences.removeFirst()
                releaseBuffers(releaseTarget, true)
            }
        }

        private fun retainActiveBufferReferences() {
            if (activeCachedBuffers.isNotEmpty()) {
                retainedBufferReferences.addLast(activeCachedBuffers)
                activeCachedBuffers = LinkedList()
            }
        }

        private fun releaseRetainedBuffers() {
            for (buffers in retainedBufferReferences) {
                releaseBuffers(buffers, false)
            }
        }

        private fun clearEmptyRetainedBuffers() {
            retainedBufferReferences.removeIf { it.isEmpty() }
        }

        internal companion object {
            private const val DEFAULT_ZONE_COUNT_BEFORE_LEAK_WARNING: Int = 25_000
            internal const val DEFAULT_BUF_RETENTION_COUNT_BEFORE_RELEASE: Int = 100

            private val supportedEncoders = createEncoderMap()

            private val logger = InlineLogger()

            private fun createClientBufferEnumMap(): EnumMap<OldSchoolClientType, ByteBuf> =
                EnumMap<OldSchoolClientType, ByteBuf>(OldSchoolClientType::class.java)

            internal fun createEncoderMap(): ClientTypeMap<UpdateZonePartialEnclosedCache> {
                val list = mutableListOf<Pair<OldSchoolClientType, UpdateZonePartialEnclosedCache>>()
                list += OldSchoolClientType.DESKTOP to DesktopUpdateZonePartialEnclosedEncoder
                return ClientTypeMap.of(OldSchoolClientType.COUNT, list)
            }

            private fun releaseBuffers(
                buffers: LinkedList<ByteBuf>,
                forceRelease: Boolean,
            ) {
                if (buffers.isEmpty()) {
                    return
                }
                val iterator = buffers.iterator()
                while (iterator.hasNext()) {
                    val next = iterator.next()
                    val refCount = next.refCnt()
                    if (forceRelease) {
                        // Don't bother removing from the list if force removing,
                        // let the garbage collector deal with it
                        if (refCount > 0) {
                            next.release(refCount)
                        }
                        continue
                    }
                    if (refCount > 1) {
                        continue
                    }
                    if (refCount == 1) {
                        next.release()
                    }
                    iterator.remove()
                }
            }
        }
    }
