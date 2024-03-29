package net.rsprot.protocol.internal.game.outgoing.info.encoder

import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.Appearance
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.Chat
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.FaceAngle
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.MoveSpeed
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.TemporaryMoveSpeed
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.ExactMove
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.FacePathingEntity
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.Hit
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.Say
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.Sequence
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.SpotAnimList
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.TintingList

public data class ExtendedInfoEncoders(
    public val appearance: PrecomputedExtendedInfoEncoder<Appearance>,
    public val chat: PrecomputedExtendedInfoEncoder<Chat>,
    public val exactMove: PrecomputedExtendedInfoEncoder<ExactMove>,
    public val faceAngle: PrecomputedExtendedInfoEncoder<FaceAngle>,
    public val facePathingEntity: PrecomputedExtendedInfoEncoder<FacePathingEntity>,
    public val hit: OnDemandExtendedInfoEncoder<Hit>,
    public val moveSpeed: PrecomputedExtendedInfoEncoder<MoveSpeed>,
    public val say: PrecomputedExtendedInfoEncoder<Say>,
    public val sequence: PrecomputedExtendedInfoEncoder<Sequence>,
    public val spotAnim: PrecomputedExtendedInfoEncoder<SpotAnimList>,
    public val temporaryMoveSpeed: PrecomputedExtendedInfoEncoder<TemporaryMoveSpeed>,
    public val tinting: OnDemandExtendedInfoEncoder<TintingList>,
)
