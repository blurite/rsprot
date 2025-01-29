package net.rsprot.protocol.game.outgoing.info.npcinfo

import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.game.outgoing.info.AvatarExtendedInfoWriter
import net.rsprot.protocol.internal.client.ClientTypeMap
import net.rsprot.protocol.internal.game.outgoing.info.ExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.ExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.npcinfo.encoder.NpcExtendedInfoEncoders
import net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.BaseAnimationSet
import net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.BodyCustomisation
import net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.CombatLevelChange
import net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.FaceCoord
import net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.HeadCustomisation
import net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.HeadIconCustomisation
import net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.NameChange
import net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.NpcTinting
import net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.Transformation
import net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.VisibleOps
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.ExactMove
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.FacePathingEntity
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.Hit
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.Say
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.Sequence
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.SpotAnimList

private typealias NEnc = NpcExtendedInfoEncoders
private typealias HeadIcon = HeadIconCustomisation
private typealias NpcExtendedInfoWriters =
    List<AvatarExtendedInfoWriter<NpcExtendedInfoEncoders, NpcAvatarExtendedInfoBlocks>>

/**
 * A data structure to bring all the extended info blocks together,
 * so the information can be passed onto various client-specific encoders.
 * @param writers the list of client-specific writers.
 * The writers must be client-specific too, not just encoders, as
 * the order in which the extended info blocks get written must follow
 * the exact order described by the client.
 */
public class NpcAvatarExtendedInfoBlocks(
    writers: NpcExtendedInfoWriters,
) {
    public val spotAnims: SpotAnimList = SpotAnimList(encoders(writers, NEnc::spotAnim))
    public val say: Say = Say(encoders(writers, NEnc::say))
    public val visibleOps: VisibleOps = VisibleOps(encoders(writers, NEnc::visibleOps))
    public val exactMove: ExactMove = ExactMove(encoders(writers, NEnc::exactMove))
    public val sequence: Sequence = Sequence(encoders(writers, NEnc::sequence))
    public val tinting: NpcTinting = NpcTinting(encoders(writers, NEnc::tinting))
    public val headIconCustomisation: HeadIcon = HeadIcon(encoders(writers, NEnc::headIconCustomisation))
    public val nameChange: NameChange =
        NameChange(
            encoders(
                writers,
                NEnc::nameChange,
            ),
        )
    public val headCustomisation: HeadCustomisation =
        HeadCustomisation(
            encoders(
                writers,
                NEnc::headCustomisation,
            ),
        )
    public val bodyCustomisation: BodyCustomisation = BodyCustomisation(encoders(writers, NEnc::bodyCustomisation))
    public val transformation: Transformation =
        Transformation(
            encoders(
                writers,
                NEnc::transformation,
            ),
        )
    public val combatLevelChange: CombatLevelChange = CombatLevelChange(encoders(writers, NEnc::combatLevelChange))
    public val hit: Hit = Hit(encoders(writers, NEnc::hit))
    public val faceCoord: FaceCoord = FaceCoord(encoders(writers, NEnc::faceCoord))
    public val facePathingEntity: FacePathingEntity = FacePathingEntity(encoders(writers, NEnc::facePathingEntity))
    public val baseAnimationSet: BaseAnimationSet = BaseAnimationSet(encoders(writers, NEnc::baseAnimationSet))

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
            allEncoders: NpcExtendedInfoWriters,
            selector: (NpcExtendedInfoEncoders) -> E,
        ): ClientTypeMap<E> =
            ClientTypeMap.ofType(allEncoders, OldSchoolClientType.COUNT) {
                it.encoders.oldSchoolClientType to selector(it.encoders)
            }
    }
}
