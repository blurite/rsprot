package net.rsprot.protocol.internal.game.outgoing.info.playerinfo.encoder

import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.internal.game.outgoing.info.encoder.OnDemandExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.Appearance
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.Chat
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.MoveSpeed
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.PlayerReset
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.PlayerTintingList
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.TemporaryMoveSpeed
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.Contrast
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.ExactMove
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.Face
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.Freeze
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.HeadbarList
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.HitmarkList
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.Say
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.Sequence
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.SpotAnimList

/**
 * A data class to bring all the extended info encoders for a given client together.
 * @param oldSchoolClientType the client for which these encoders are created.
 */
public data class PlayerExtendedInfoEncoders(
    public val oldSchoolClientType: OldSchoolClientType,
    public val appearance: PrecomputedExtendedInfoEncoder<Appearance>,
    public val chat: PrecomputedExtendedInfoEncoder<Chat>,
    public val exactMove: PrecomputedExtendedInfoEncoder<ExactMove>,
    public val face: PrecomputedExtendedInfoEncoder<Face>,
    public val hitmarkList: OnDemandExtendedInfoEncoder<HitmarkList>,
    public val headbarList: OnDemandExtendedInfoEncoder<HeadbarList>,
    public val moveSpeed: PrecomputedExtendedInfoEncoder<MoveSpeed>,
    public val say: PrecomputedExtendedInfoEncoder<Say>,
    public val sequence: PrecomputedExtendedInfoEncoder<Sequence>,
    public val spotAnim: PrecomputedExtendedInfoEncoder<SpotAnimList>,
    public val temporaryMoveSpeed: PrecomputedExtendedInfoEncoder<TemporaryMoveSpeed>,
    public val tinting: OnDemandExtendedInfoEncoder<PlayerTintingList>,
    public val contrast: PrecomputedExtendedInfoEncoder<Contrast>,
    public val freeze: PrecomputedExtendedInfoEncoder<Freeze>,
    public val playerReset: PrecomputedExtendedInfoEncoder<PlayerReset>,
)
