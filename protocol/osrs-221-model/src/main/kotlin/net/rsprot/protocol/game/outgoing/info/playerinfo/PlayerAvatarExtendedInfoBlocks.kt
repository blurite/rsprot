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

/**
 * A data structure to bring all the extended info blocks together,
 * so the information can be passed onto various platform-specific encoders.
 * @param writers the list of platform-specific writers.
 * The writers must be platform-specific too, not just encoders, as
 * the order in which the extended info blocks get written must follow
 * the exact order described by the client.
 * @param allocator the buffer allocator used for pre-computed extended
 * info blocks.
 * @param huffmanCodec the Huffman codec is used to compress public chat extended info blocks.
 */
public class PlayerAvatarExtendedInfoBlocks(
    writers: List<PlayerAvatarExtendedInfoWriter>,
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
        /**
         * Builds a platform-specific array of encoders for a specific extended info block,
         * indexed by [PlatformType.id].
         * If a platform hasn't been registered, the encoder at that index will be null.
         * @param allEncoders all the platform-specific extended info writers for the given type.
         * @param selector a higher order function to retrieve a specific extended info block from
         * the full structure of all the extended info blocks.
         * @return an array of platform-specific encoders of the given extended info block,
         * indexed by [PlatformType.id].
         */
        private inline fun <T : ExtendedInfo<T, E>, reified E : ExtendedInfoEncoder<T>> buildPlatformEncoderArray(
            allEncoders: List<PlayerAvatarExtendedInfoWriter>,
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
