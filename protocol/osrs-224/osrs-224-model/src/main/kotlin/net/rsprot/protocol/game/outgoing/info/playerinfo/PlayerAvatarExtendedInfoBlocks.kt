package net.rsprot.protocol.game.outgoing.info.playerinfo

import net.rsprot.protocol.internal.client.ClientTypeMap
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.internal.game.outgoing.info.ExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.ExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.encoder.PlayerExtendedInfoEncoders
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
import net.rsprot.protocol.game.outgoing.info.AvatarExtendedInfoWriter

private typealias PEnc = PlayerExtendedInfoEncoders
private typealias TempMoveSpeed = net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.TemporaryMoveSpeed

/**
 * A data structure to bring all the extended info blocks together,
 * so the information can be passed onto various client-specific encoders.
 * @param writers the list of client-specific writers.
 * The writers must be client-specific too, not just encoders, as
 * the order in which the extended info blocks get written must follow
 * the exact order described by the client.
 */
public class PlayerAvatarExtendedInfoBlocks(
    writers: List<AvatarExtendedInfoWriter<PlayerExtendedInfoEncoders, PlayerAvatarExtendedInfoBlocks>>,
) {
    public val appearance: Appearance = Appearance(encoders(writers, PEnc::appearance))
    public val moveSpeed: MoveSpeed = MoveSpeed(encoders(writers, PEnc::moveSpeed))
    public val temporaryMoveSpeed: TempMoveSpeed = TempMoveSpeed(encoders(writers, PEnc::temporaryMoveSpeed))
    public val sequence: Sequence = Sequence(encoders(writers, PEnc::sequence))
    public val facePathingEntity: FacePathingEntity = FacePathingEntity(encoders(writers, PEnc::facePathingEntity))
    public val faceAngle: net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.FaceAngle =
	    net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.FaceAngle(
		    encoders(
			    writers,
			    PEnc::faceAngle
		    )
	    )
    public val say: Say = Say(encoders(writers, PEnc::say))
    public val chat: Chat = Chat(encoders(writers, PEnc::chat))
    public val exactMove: ExactMove = ExactMove(encoders(writers, PEnc::exactMove))
    public val spotAnims: net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.SpotAnimList =
	    net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.SpotAnimList(
		    encoders(
			    writers,
			    PEnc::spotAnim
		    )
	    )
    public val hit: Hit = Hit(encoders(writers, PEnc::hit))
    public val tinting: PlayerTintingList = PlayerTintingList(encoders(writers, PEnc::tinting))

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
            allEncoders: List<AvatarExtendedInfoWriter<PlayerExtendedInfoEncoders, PlayerAvatarExtendedInfoBlocks>>,
            selector: (PlayerExtendedInfoEncoders) -> E,
        ): ClientTypeMap<E> =
            ClientTypeMap.ofType(allEncoders, OldSchoolClientType.COUNT) {
                it.encoders.oldSchoolClientType to selector(it.encoders)
            }
    }
}
