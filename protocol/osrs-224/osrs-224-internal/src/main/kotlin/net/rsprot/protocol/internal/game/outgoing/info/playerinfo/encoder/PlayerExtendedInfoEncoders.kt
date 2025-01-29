package net.rsprot.protocol.internal.game.outgoing.info.playerinfo.encoder

import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.internal.game.outgoing.info.encoder.OnDemandExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.Appearance
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.Chat
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.FaceAngle
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.MoveSpeed
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.PlayerTintingList
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.TemporaryMoveSpeed
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.ExactMove
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.FacePathingEntity
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.Hit
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
	public val faceAngle: PrecomputedExtendedInfoEncoder<net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.FaceAngle>,
	public val facePathingEntity: PrecomputedExtendedInfoEncoder<FacePathingEntity>,
	public val hit: OnDemandExtendedInfoEncoder<Hit>,
	public val moveSpeed: PrecomputedExtendedInfoEncoder<MoveSpeed>,
	public val say: PrecomputedExtendedInfoEncoder<Say>,
	public val sequence: PrecomputedExtendedInfoEncoder<Sequence>,
	public val spotAnim: PrecomputedExtendedInfoEncoder<net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.SpotAnimList>,
	public val temporaryMoveSpeed: PrecomputedExtendedInfoEncoder<net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.TemporaryMoveSpeed>,
	public val tinting: OnDemandExtendedInfoEncoder<PlayerTintingList>,
)
