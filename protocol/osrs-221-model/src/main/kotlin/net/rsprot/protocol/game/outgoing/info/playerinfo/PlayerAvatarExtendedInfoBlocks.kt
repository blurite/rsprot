package net.rsprot.protocol.game.outgoing.info.playerinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.compression.HuffmanCodec
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
import net.rsprot.protocol.shared.platform.PlatformType

public class PlayerAvatarExtendedInfoBlocks(
    writers: List<AvatarExtendedInfoWriter>,
    allocator: ByteBufAllocator,
    huffmanCodec: HuffmanCodec,
) {
    public val appearance: Appearance =
        Appearance(
            buildPlatformEncoderArray(writers, PlayerExtendedInfoEncoders::appearance),
            allocator,
            huffmanCodec,
        )
    public val moveSpeed: MoveSpeed =
        MoveSpeed(
            buildPlatformEncoderArray(writers, PlayerExtendedInfoEncoders::moveSpeed),
            allocator,
            huffmanCodec,
        )
    public val temporaryMoveSpeed: TemporaryMoveSpeed =
        TemporaryMoveSpeed(
            buildPlatformEncoderArray(writers, PlayerExtendedInfoEncoders::temporaryMoveSpeed),
            allocator,
            huffmanCodec,
        )
    public val sequence: Sequence =
        Sequence(
            buildPlatformEncoderArray(writers, PlayerExtendedInfoEncoders::sequence),
            allocator,
            huffmanCodec,
        )
    public val facePathingEntity: FacePathingEntity =
        FacePathingEntity(
            buildPlatformEncoderArray(writers, PlayerExtendedInfoEncoders::facePathingEntity),
            allocator,
            huffmanCodec,
        )
    public val faceAngle: FaceAngle =
        FaceAngle(
            buildPlatformEncoderArray(writers, PlayerExtendedInfoEncoders::faceAngle),
            allocator,
            huffmanCodec,
        )
    public val say: Say =
        Say(
            buildPlatformEncoderArray(writers, PlayerExtendedInfoEncoders::say),
            allocator,
            huffmanCodec,
        )
    public val chat: Chat =
        Chat(
            buildPlatformEncoderArray(writers, PlayerExtendedInfoEncoders::chat),
            allocator,
            huffmanCodec,
        )
    public val exactMove: ExactMove =
        ExactMove(
            buildPlatformEncoderArray(writers, PlayerExtendedInfoEncoders::exactMove),
            allocator,
            huffmanCodec,
        )
    public val spotAnims: SpotAnimList =
        SpotAnimList(
            buildPlatformEncoderArray(writers, PlayerExtendedInfoEncoders::spotAnim),
            allocator,
            huffmanCodec,
        )
    public val hit: Hit =
        Hit(
            buildPlatformEncoderArray(writers, PlayerExtendedInfoEncoders::hit),
        )
    public val tinting: TintingList =
        TintingList(
            buildPlatformEncoderArray(writers, PlayerExtendedInfoEncoders::tinting),
        )

    private companion object {
        private inline fun <T : ExtendedInfo<T, E>, reified E : ExtendedInfoEncoder<T>> buildPlatformEncoderArray(
            allEncoders: List<AvatarExtendedInfoWriter>,
            selector: (PlayerExtendedInfoEncoders) -> E,
        ): Array<E?> {
            val array = arrayOfNulls<E>(PlatformType.COUNT)
            for (writer in allEncoders) {
                val encoder = selector(writer.encoders)
                array[writer.encoders.platformType.id] = encoder
            }
            return array
        }
    }
}
