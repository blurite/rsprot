package net.rsprot.protocol.game.outgoing.info.playerinfo

import net.rsprot.protocol.internal.game.outgoing.info.ExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.ExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.encoder.PlayerExtendedInfoEncoders
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
import net.rsprot.protocol.internal.platform.PlatformMap
import net.rsprot.protocol.shared.platform.PlatformType

private typealias Encoders = PlayerExtendedInfoEncoders
private typealias TempMoveSpeed = TemporaryMoveSpeed

/**
 * A data structure to bring all the extended info blocks together,
 * so the information can be passed onto various platform-specific encoders.
 * @param writers the list of platform-specific writers.
 * The writers must be platform-specific too, not just encoders, as
 * the order in which the extended info blocks get written must follow
 * the exact order described by the client.
 */
public class PlayerAvatarExtendedInfoBlocks(
    writers: List<PlayerAvatarExtendedInfoWriter>,
) {
    public val appearance: Appearance = Appearance(encoders(writers, Encoders::appearance))
    public val moveSpeed: MoveSpeed = MoveSpeed(encoders(writers, Encoders::moveSpeed))
    public val temporaryMoveSpeed: TempMoveSpeed = TempMoveSpeed(encoders(writers, Encoders::temporaryMoveSpeed))
    public val sequence: Sequence = Sequence(encoders(writers, Encoders::sequence))
    public val facePathingEntity: FacePathingEntity = FacePathingEntity(encoders(writers, Encoders::facePathingEntity))
    public val faceAngle: FaceAngle = FaceAngle(encoders(writers, Encoders::faceAngle))
    public val say: Say = Say(encoders(writers, Encoders::say))
    public val chat: Chat = Chat(encoders(writers, Encoders::chat))
    public val exactMove: ExactMove = ExactMove(encoders(writers, Encoders::exactMove))
    public val spotAnims: SpotAnimList = SpotAnimList(encoders(writers, Encoders::spotAnim))
    public val hit: Hit = Hit(encoders(writers, Encoders::hit))
    public val tinting: TintingList = TintingList(encoders(writers, Encoders::tinting))

    private companion object {
        /**
         * Builds a platform-specific map of encoders for a specific extended info block,
         * keyed by [PlatformType.id].
         * If a platform hasn't been registered, the encoder at that index will be null.
         * @param allEncoders all the platform-specific extended info writers for the given type.
         * @param selector a higher order function to retrieve a specific extended info block from
         * the full structure of all the extended info blocks.
         * @return a map of platform-specific encoders of the given extended info block,
         * keyed by [PlatformType.id].
         */
        private inline fun <T : ExtendedInfo<T, E>, reified E : ExtendedInfoEncoder<T>> encoders(
            allEncoders: List<PlayerAvatarExtendedInfoWriter>,
            selector: (PlayerExtendedInfoEncoders) -> E,
        ): PlatformMap<E> {
            return PlatformMap.ofType(allEncoders, PlatformType.COUNT) {
                it.encoders.platformType to selector(it.encoders)
            }
        }
    }
}
