package net.rsprot.protocol.game.outgoing.codec.zone.header

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.common.game.outgoing.codec.zone.payload.OldSchoolZoneProt
import net.rsprot.protocol.common.game.outgoing.codec.zone.payload.ZoneProtEncoder
import net.rsprot.protocol.game.outgoing.codec.zone.payload.LocAddChangeEncoder
import net.rsprot.protocol.game.outgoing.codec.zone.payload.LocAnimEncoder
import net.rsprot.protocol.game.outgoing.codec.zone.payload.LocDelEncoder
import net.rsprot.protocol.game.outgoing.codec.zone.payload.LocMergeEncoder
import net.rsprot.protocol.game.outgoing.codec.zone.payload.MapAnimEncoder
import net.rsprot.protocol.game.outgoing.codec.zone.payload.MapProjAnimEncoder
import net.rsprot.protocol.game.outgoing.codec.zone.payload.ObjAddEncoder
import net.rsprot.protocol.game.outgoing.codec.zone.payload.ObjCountEncoder
import net.rsprot.protocol.game.outgoing.codec.zone.payload.ObjDelEncoder
import net.rsprot.protocol.game.outgoing.codec.zone.payload.ObjOpFilterEncoder
import net.rsprot.protocol.game.outgoing.codec.zone.payload.SoundAreaEncoder
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.zone.header.UpdateZonePartialEnclosed
import net.rsprot.protocol.message.ZoneProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.message.codec.UpdateZonePartialEnclosedCache
import kotlin.math.min

public class DesktopUpdateZonePartialEnclosedEncoder : MessageEncoder<UpdateZonePartialEnclosed> {
    override val prot: ServerProt = GameServerProt.UPDATE_ZONE_PARTIAL_ENCLOSED

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: UpdateZonePartialEnclosed,
    ) {
        buffer.p1(message.zoneX)
        buffer.p1Alt2(message.zoneZ)
        buffer.p1Alt3(message.level)
        buffer.buffer.writeBytes(
            message.payload,
            message.payload.readerIndex(),
            message.payload.readableBytes(),
        )
    }

    public companion object : UpdateZonePartialEnclosedCache {
        private const val MAX_PARTIAL_ENCLOSED_SIZE = 40_000 - 3

        /**
         * Builds a cache of a given zone's list of zone prots.
         * This is intended so the server only requests one cache per zone per game cycle,
         * rather than re-building the same buffer N times, where N is the number of players
         * observing the zone. With this in mind however, zone prots which are player-specific,
         * such as OBJ_ADD cannot be grouped together and must be sent separately, as they also
         * are in OldSchool RuneScape.
         * @param allocator the byte buffer allocator used for the cached buffer.
         * Note that it is the server's responsibility to release the buffer once the cycle has ended.
         * The individual writes of [UpdateZonePartialEnclosed] do not modify the reference count
         * in any way.
         * @param messages the list of zone prot messages to be encoded.
         */
        override fun <T : ZoneProt> buildCache(
            allocator: ByteBufAllocator,
            messages: List<T>,
        ): ByteBuf {
            val buffer =
                allocator
                    .buffer(
                        min(IndexedZoneProtEncoder.maxZoneProtSize * messages.size, MAX_PARTIAL_ENCLOSED_SIZE),
                        MAX_PARTIAL_ENCLOSED_SIZE,
                    ).toJagByteBuf()
            for (message in messages) {
                val indexedEncoder = IndexedZoneProtEncoder.indexedEncoders[message.protId]
                buffer.p1(indexedEncoder.ordinal)
                encodeMessage(
                    buffer,
                    message,
                    indexedEncoder.encoder,
                )
            }
            return buffer.buffer
        }

        /**
         * Encodes the [message] into the [buffer] using the [encoder] as the encoder for it.
         * @param buffer the buffer to encode into
         * @param message the message to be encoded
         * @param encoder the encoder to use for encoding the message.
         * Note that the type of the encoder is not compile-time known as we acquire it dynamically
         * based on the message itself.
         */
        private fun <T : ZoneProt> encodeMessage(
            buffer: JagByteBuf,
            message: T,
            encoder: ZoneProtEncoder<*>,
        ) {
            @Suppress("UNCHECKED_CAST")
            encoder as ZoneProtEncoder<T>
            encoder.encode(buffer, message)
        }

        /**
         * Zone prot encoders here are used specifically by the [UpdateZonePartialEnclosed]
         * packet, as this packet has its own sub-system of the zone prots, with the ability
         * to send a batch of zone packets in one go with its own internal indexing.
         *
         * WARNING: This enum's order MUST match the order in the client, as the
         * [IndexedZoneProtEncoder.ordinal] function is used for indexing!
         *
         * @property protId the respective [ZoneProt.protId] of each message, used for
         * quick indexing of respective messages.
         * @property encoder the zone prot encoder responsible for encoding the respective message
         * into a byte buffer.
         */
        private enum class IndexedZoneProtEncoder(
            private val protId: Int,
            val encoder: ZoneProtEncoder<*>,
        ) {
            LOC_DEL(OldSchoolZoneProt.LOC_DEL, LocDelEncoder()),
            OBJ_DEL(OldSchoolZoneProt.OBJ_DEL, ObjDelEncoder()),
            LOC_ANIM(OldSchoolZoneProt.LOC_ANIM, LocAnimEncoder()),
            LOC_MERGE(OldSchoolZoneProt.LOC_MERGE, LocMergeEncoder()),
            OBJ_ADD(OldSchoolZoneProt.OBJ_ADD, ObjAddEncoder()),
            MAP_ANIM(OldSchoolZoneProt.MAP_ANIM, MapAnimEncoder()),
            OBJ_COUNT(OldSchoolZoneProt.OBJ_COUNT, ObjCountEncoder()),
            OBJ_OPFILTER(OldSchoolZoneProt.OBJ_OPFILTER, ObjOpFilterEncoder()),
            MAP_PROJANIM(OldSchoolZoneProt.MAP_PROJANIM, MapProjAnimEncoder()),
            SOUND_AREA(OldSchoolZoneProt.SOUND_AREA, SoundAreaEncoder()),
            LOC_ADD_CHANGE(OldSchoolZoneProt.LOC_ADD_CHANGE, LocAddChangeEncoder()),
            ;

            companion object {
                /**
                 * The maximum possible size of a single zone prot.
                 * This constant is used to determine the maximum initial possible buffer capacity.
                 */
                val maxZoneProtSize =
                    entries.maxOf {
                        it.encoder.prot.size
                    }

                /**
                 * The zone prot encoders indexed by their prot ids, allowing for fast access based
                 * on the respective [ZoneProt.protId] through the array.
                 */
                val indexedEncoders =
                    Array(entries.size) { index ->
                        entries.first { prot ->
                            index == prot.protId
                        }
                    }
            }
        }
    }
}
