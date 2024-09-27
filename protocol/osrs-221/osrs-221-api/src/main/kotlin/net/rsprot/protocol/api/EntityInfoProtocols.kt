package net.rsprot.protocol.api

import io.netty.buffer.ByteBufAllocator
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.api.suppliers.NpcInfoSupplier
import net.rsprot.protocol.api.suppliers.PlayerInfoSupplier
import net.rsprot.protocol.common.client.ClientTypeMap
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.common.game.outgoing.info.npcinfo.encoder.NpcResolutionChangeEncoder
import net.rsprot.protocol.common.game.outgoing.info.util.ZoneIndexStorage
import net.rsprot.protocol.game.outgoing.codec.npcinfo.DesktopLowResolutionChangeEncoder
import net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo.writer.NpcAvatarExtendedInfoDesktopWriter
import net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo.writer.PlayerAvatarExtendedInfoDesktopWriter
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcAvatarExtendedInfoWriter
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcAvatarFactory
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcInfoProtocol
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerAvatarExtendedInfoWriter
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerAvatarFactory
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfoProtocol

/**
 * The entity info protocols class brings together the relatively complex player and NPC info
 * protocols. This is responsible for registering all the client types that are used by the user.
 * @property playerAvatarFactory the avatar factory for players. Since players have a 1:1 player info
 * to avatar ratio, the avatar is automatically included in the player info object that is requested.
 * This is additionally strategically placed to improve cache locality and improve the performance
 * of the player info protocol.
 * @property playerInfoProtocol the main player info protocol responsible for computing the player
 * info packet for all the players in the game.
 * @property npcAvatarFactory the avatar factory for NPCs. Each NPC must allocate one avatar
 * as they spawn, and that avatar must be deallocated when the NPC is fully removed from the game.
 * @property npcInfoProtocol the main NPC info protocol responsible for computing the npc info packet
 * for all the players in the game.
 */
public class EntityInfoProtocols
    private constructor(
        public val playerAvatarFactory: PlayerAvatarFactory,
        public val playerInfoProtocol: PlayerInfoProtocol,
        public val npcAvatarFactory: NpcAvatarFactory,
        public val npcInfoProtocol: NpcInfoProtocol,
    ) {
        internal companion object {
            /**
             * Initializes the player and NPC info avatar factories and protocols.
             * @param allocator the byte buffer allocator used for player and NPC info main buffers,
             * as well as any pre-computed extended info blocks.
             * @param clientTypes the list of client types to register
             * @param huffmanCodecProvider the Huffman codec provider that will be used to compute
             * the chat extended info block, any others in the future that may require it.
             * @param playerInfoSupplier the class wrapping the worker used to perform computations,
             * as well as the filter for extended info blocks that ensures that the packet does not
             * under any circumstances exceed the maximum packet limitations.
             * @param npcInfoSupplier the class wrapping the worker used to perform computations,
             * as well as the filter for extended info blocks that ensures that the packet does not
             * under any circumstances exceed the maximum packet limitations. Furthermore, unlike
             * player info, this will also provide an implementation for exceptions caught during
             * pre-computations of NPC avatars. It is up to the server to decide how to handle
             * any exceptions which are caught when computing information for avatars. The least
             * destructive way is to remove the underlying NPC from the world when that happens,
             * and log the exception in the process. This will still cause any observers to disconnect,
             * however, but it ensures that anyone else that comes around the same area will not
             * experience the same fate. There is also an implementation that is used to supply
             * indices of nearby NPCs for the NPC info packet from the server's perspective,
             * given a number of arguments necessary to determine it. The server is expected
             * to return an iterator of all the indices of the NPCs that match the predicate,
             * even if a NPC is already tracked by a given player. The protocol is responsible
             * for ensuring no duplications will occur.
             * @return a class wrapping all the protocols into one object.
             */
            fun initialize(
                allocator: ByteBufAllocator,
                clientTypes: List<OldSchoolClientType>,
                huffmanCodecProvider: HuffmanCodecProvider,
                playerInfoSupplier: PlayerInfoSupplier,
                npcInfoSupplier: NpcInfoSupplier,
            ): EntityInfoProtocols {
                val playerWriters = mutableListOf<PlayerAvatarExtendedInfoWriter>()
                val npcWriters = mutableListOf<NpcAvatarExtendedInfoWriter>()
                val npcResolutionChangeEncoders = mutableListOf<NpcResolutionChangeEncoder>()
                if (OldSchoolClientType.DESKTOP in clientTypes) {
                    playerWriters += PlayerAvatarExtendedInfoDesktopWriter()
                    npcWriters += NpcAvatarExtendedInfoDesktopWriter()
                    npcResolutionChangeEncoders += DesktopLowResolutionChangeEncoder()
                }
                val playerAvatarFactory =
                    buildPlayerAvatarFactory(allocator, playerInfoSupplier, playerWriters, huffmanCodecProvider)
                val playerInfoProtocol =
                    buildPlayerInfoProtocol(
                        allocator,
                        playerInfoSupplier,
                        playerAvatarFactory,
                    )
                val storage = ZoneIndexStorage(ZoneIndexStorage.NPC_CAPACITY)
                val npcAvatarFactory =
                    buildNpcAvatarFactory(
                        allocator,
                        npcInfoSupplier,
                        npcWriters,
                        huffmanCodecProvider,
                        storage,
                    )
                val npcInfoProtocol =
                    buildNpcInfoProtocol(
                        allocator,
                        npcInfoSupplier,
                        npcResolutionChangeEncoders,
                        npcAvatarFactory,
                        storage,
                    )

                return EntityInfoProtocols(
                    playerAvatarFactory,
                    playerInfoProtocol,
                    npcAvatarFactory,
                    npcInfoProtocol,
                )
            }

            private fun buildNpcInfoProtocol(
                allocator: ByteBufAllocator,
                npcInfoSupplier: NpcInfoSupplier,
                npcResolutionChangeEncoders: MutableList<NpcResolutionChangeEncoder>,
                npcAvatarFactory: NpcAvatarFactory,
                zoneIndexStorage: ZoneIndexStorage,
            ) = NpcInfoProtocol(
                allocator,
                ClientTypeMap.of(
                    npcResolutionChangeEncoders,
                    OldSchoolClientType.COUNT,
                ) {
                    it.clientType
                },
                npcAvatarFactory,
                npcInfoSupplier.npcAvatarExceptionHandler,
                npcInfoSupplier.npcInfoProtocolWorker,
                zoneIndexStorage,
            )

            private fun buildNpcAvatarFactory(
                allocator: ByteBufAllocator,
                npcInfoSupplier: NpcInfoSupplier,
                npcWriters: MutableList<NpcAvatarExtendedInfoWriter>,
                huffmanCodecProvider: HuffmanCodecProvider,
                zoneIndexStorage: ZoneIndexStorage,
            ): NpcAvatarFactory =
                NpcAvatarFactory(
                    allocator,
                    npcInfoSupplier.npcExtendedInfoFilter,
                    npcWriters,
                    huffmanCodecProvider,
                    zoneIndexStorage,
                )

            private fun buildPlayerInfoProtocol(
                allocator: ByteBufAllocator,
                playerInfoSupplier: PlayerInfoSupplier,
                playerAvatarFactory: PlayerAvatarFactory,
            ): PlayerInfoProtocol =
                PlayerInfoProtocol(
                    allocator,
                    playerInfoSupplier.playerInfoProtocolWorker,
                    playerAvatarFactory,
                )

            private fun buildPlayerAvatarFactory(
                allocator: ByteBufAllocator,
                playerInfoSupplier: PlayerInfoSupplier,
                playerWriters: MutableList<PlayerAvatarExtendedInfoWriter>,
                huffmanCodecProvider: HuffmanCodecProvider,
            ): PlayerAvatarFactory =
                PlayerAvatarFactory(
                    allocator,
                    playerInfoSupplier.playerExtendedInfoFilter,
                    playerWriters,
                    huffmanCodecProvider,
                )
        }
    }
