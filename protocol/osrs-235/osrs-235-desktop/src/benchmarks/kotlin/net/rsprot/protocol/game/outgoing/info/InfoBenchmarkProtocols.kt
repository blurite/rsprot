@file:Suppress("ktlint:standard:filename")

package net.rsprot.protocol.game.outgoing.info

import io.netty.buffer.Unpooled
import io.netty.buffer.UnpooledByteBufAllocator
import net.rsprot.compression.HuffmanCodec
import net.rsprot.compression.provider.DefaultHuffmanCodecProvider
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.game.outgoing.codec.npcinfo.DesktopLowResolutionChangeEncoder
import net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo.writer.NpcAvatarExtendedInfoDesktopWriter
import net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo.writer.PlayerAvatarExtendedInfoDesktopWriter
import net.rsprot.protocol.game.outgoing.codec.worldentity.extendedinfo.WorldEntityAvatarExtendedInfoDesktopWriter
import net.rsprot.protocol.game.outgoing.info.filter.DefaultExtendedInfoFilter
import net.rsprot.protocol.game.outgoing.info.npcinfo.DeferredNpcInfoProtocolSupplier
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcAvatarFactory
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcInfoProtocol
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerAvatarFactory
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfoProtocol
import net.rsprot.protocol.game.outgoing.info.worker.DefaultProtocolWorker
import net.rsprot.protocol.game.outgoing.info.worker.ProtocolWorker
import net.rsprot.protocol.game.outgoing.info.worldentityinfo.WorldEntityAvatarFactory
import net.rsprot.protocol.game.outgoing.info.worldentityinfo.WorldEntityProtocol
import net.rsprot.protocol.internal.client.ClientTypeMap
import net.rsprot.protocol.internal.game.outgoing.info.util.ZoneIndexStorage

internal data class BenchmarkInfoProtocolContext(
    val protocols: InfoProtocols,
    val npcAvatarFactory: NpcAvatarFactory,
)

internal fun generateBenchmarkInfoProtocols(
    playerProtocolWorker: ProtocolWorker = DefaultProtocolWorker(),
    npcProtocolWorker: ProtocolWorker = DefaultProtocolWorker(),
): BenchmarkInfoProtocolContext {
    val allocator = UnpooledByteBufAllocator.DEFAULT
    val npcStorage = ZoneIndexStorage(ZoneIndexStorage.NPC_CAPACITY)
    val npcProtocolSupplier = DeferredNpcInfoProtocolSupplier()
    val npcAvatarFactory =
        NpcAvatarFactory(
            allocator,
            DefaultExtendedInfoFilter(),
            listOf(NpcAvatarExtendedInfoDesktopWriter()),
            DefaultHuffmanCodecProvider(createHuffmanCodec()),
            npcStorage,
            npcProtocolSupplier,
        )
    val npcEncoders =
        ClientTypeMap.of(
            listOf(DesktopLowResolutionChangeEncoder()),
            OldSchoolClientType.COUNT,
        ) {
            it.clientType
        }
    val npcInfoProtocol =
        NpcInfoProtocol(
            allocator,
            npcEncoders,
            npcAvatarFactory,
            { _, exception -> exception.printStackTrace() },
            npcProtocolWorker,
            npcStorage,
        )
    npcProtocolSupplier.supply(npcInfoProtocol)

    val worldEntityStorage = ZoneIndexStorage(ZoneIndexStorage.WORLDENTITY_CAPACITY)
    val worldEntityInfoProtocol =
        WorldEntityProtocol(
            allocator,
            exceptionHandler = { _, _ -> },
            factory =
                WorldEntityAvatarFactory(
                    allocator,
                    worldEntityStorage,
                    listOf(WorldEntityAvatarExtendedInfoDesktopWriter()),
                    DefaultHuffmanCodecProvider(createHuffmanCodec()),
                ),
            zoneIndexStorage = worldEntityStorage,
        )
    val playerInfoProtocol =
        PlayerInfoProtocol(
            allocator,
            playerProtocolWorker,
            PlayerAvatarFactory(
                allocator,
                DefaultExtendedInfoFilter(),
                listOf(PlayerAvatarExtendedInfoDesktopWriter()),
                DefaultHuffmanCodecProvider(createHuffmanCodec()),
            ),
        )
    return BenchmarkInfoProtocolContext(
        InfoProtocols(
            playerInfoProtocol,
            npcInfoProtocol,
            worldEntityInfoProtocol,
        ),
        npcAvatarFactory,
    )
}

private fun createHuffmanCodec(): HuffmanCodec {
    val resource = PlayerInfoBenchmark::class.java.getResourceAsStream("huffman.dat")
    checkNotNull(resource) {
        "huffman.dat could not be found"
    }
    return HuffmanCodec.create(Unpooled.wrappedBuffer(resource.readBytes()))
}
