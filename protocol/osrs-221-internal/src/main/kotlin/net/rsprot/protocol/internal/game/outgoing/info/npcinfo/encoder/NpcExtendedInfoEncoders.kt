package net.rsprot.protocol.internal.game.outgoing.info.npcinfo.encoder

import net.rsprot.protocol.internal.game.outgoing.info.encoder.OnDemandExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
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
import net.rsprot.protocol.shared.platform.PlatformType

/**
 * A data class to bring all the extended info encoders for a given platform together.
 * @param platformType the platform for which these encoders are created.
 */
public data class NpcExtendedInfoEncoders(
    public val platformType: PlatformType,
    public val spotAnim: PrecomputedExtendedInfoEncoder<SpotAnimList>,
    public val say: PrecomputedExtendedInfoEncoder<Say>,
    public val visibleOps: PrecomputedExtendedInfoEncoder<VisibleOps>,
    public val exactMove: PrecomputedExtendedInfoEncoder<ExactMove>,
    public val sequence: PrecomputedExtendedInfoEncoder<Sequence>,
    public val tinting: PrecomputedExtendedInfoEncoder<NpcTinting>,
    public val headIconCustomisation: PrecomputedExtendedInfoEncoder<HeadIconCustomisation>,
    public val nameChange: PrecomputedExtendedInfoEncoder<NameChange>,
    public val headCustomisation: PrecomputedExtendedInfoEncoder<HeadCustomisation>,
    public val bodyCustomisation: PrecomputedExtendedInfoEncoder<BodyCustomisation>,
    public val transformation: PrecomputedExtendedInfoEncoder<Transformation>,
    public val combatLevelChange: PrecomputedExtendedInfoEncoder<CombatLevelChange>,
    public val hit: OnDemandExtendedInfoEncoder<Hit>,
    public val faceCoord: PrecomputedExtendedInfoEncoder<FaceCoord>,
    public val facePathingEntity: PrecomputedExtendedInfoEncoder<FacePathingEntity>,
    public val baseAnimationSet: PrecomputedExtendedInfoEncoder<BaseAnimationSet>,
)
