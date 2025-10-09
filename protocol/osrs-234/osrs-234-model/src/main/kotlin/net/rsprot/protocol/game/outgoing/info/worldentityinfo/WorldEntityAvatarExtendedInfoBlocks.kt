package net.rsprot.protocol.game.outgoing.info.worldentityinfo

import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.game.outgoing.info.AvatarExtendedInfoWriter
import net.rsprot.protocol.internal.client.ClientTypeMap
import net.rsprot.protocol.internal.game.outgoing.info.ExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.ExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.VisibleOps
import net.rsprot.protocol.internal.game.outgoing.info.worldentityinfo.encoder.WorldEntityExtendedInfoEncoders

private typealias WEEnc = WorldEntityExtendedInfoEncoders
private typealias WorldEntityExtendedInfoWriters =
    List<AvatarExtendedInfoWriter<WorldEntityExtendedInfoEncoders, WorldEntityAvatarExtendedInfoBlocks>>

/**
 * A data structure to bring all the extended info blocks together,
 * so the information can be passed onto various client-specific encoders.
 * @param writers the list of client-specific writers.
 * The writers must be client-specific too, not just encoders, as
 * the order in which the extended info blocks get written must follow
 * the exact order described by the client.
 */
public class WorldEntityAvatarExtendedInfoBlocks(
    writers: WorldEntityExtendedInfoWriters,
) {
    public val visibleOps: VisibleOps = VisibleOps(encoders(writers, WEEnc::visibleOps))

    private companion object {
        /**
         * Builds a client-specific map of encoders for a specific extended info block,
         * keyed by [OldSchoolClientType.id].
         * If a client hasn't been registered, the encoder at that index will be null.
         * @param allEncoders all the client-specific extended info writers for the given type.
         * @param selector a higher order function to retrieve a specific extended info block from
         * the full structure of all the extended info blocks.
         * @return a map of client-specific encoders of the given extended info block,
         * keyed by [OldSchoolClientType.id].
         */
        private inline fun <T : ExtendedInfo<T, E>, reified E : ExtendedInfoEncoder<T>> encoders(
            allEncoders: WorldEntityExtendedInfoWriters,
            selector: (WorldEntityExtendedInfoEncoders) -> E,
        ): ClientTypeMap<E> =
            ClientTypeMap.ofType(allEncoders, OldSchoolClientType.COUNT) {
                it.encoders.oldSchoolClientType to selector(it.encoders)
            }
    }
}
