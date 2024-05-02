package net.rsprot.protocol.api

import io.netty.buffer.ByteBufAllocator
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.api.suppliers.NpcInfoSupplier
import net.rsprot.protocol.api.suppliers.PlayerInfoSupplier
import net.rsprot.protocol.common.client.ClientTypeMap
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.common.game.outgoing.info.npcinfo.encoder.NpcResolutionChangeEncoder
import net.rsprot.protocol.game.outgoing.codec.npcinfo.DesktopLowResolutionChangeEncoder
import net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo.writer.NpcAvatarExtendedInfoDesktopWriter
import net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo.writer.PlayerAvatarExtendedInfoDesktopWriter
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcAvatarExtendedInfoWriter
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcAvatarFactory
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcInfoProtocol
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerAvatarExtendedInfoWriter
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerAvatarFactory
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfoProtocol

@Suppress("OPT_IN_USAGE")
public class EntityInfoProtocols
    private constructor(
        public val playerAvatarFactory: PlayerAvatarFactory,
        public val playerInfoProtocol: PlayerInfoProtocol,
        public val npcAvatarFactory: NpcAvatarFactory,
        public val npcInfoProtocol: NpcInfoProtocol,
    ) {
        internal companion object {
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
                    buildPlayerInfoProtocol(allocator, playerInfoSupplier, playerAvatarFactory)
                val npcAvatarFactory =
                    buildNpcAvatarFactory(allocator, npcInfoSupplier, npcWriters, huffmanCodecProvider)
                val npcInfoProtocol =
                    buildNpcInfoProtocol(
                        allocator,
                        npcInfoSupplier,
                        npcResolutionChangeEncoders,
                        npcAvatarFactory,
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
            ) = NpcInfoProtocol(
                allocator,
                npcInfoSupplier.npcIndexSupplier,
                ClientTypeMap.of(
                    npcResolutionChangeEncoders,
                    OldSchoolClientType.COUNT,
                ) {
                    it.clientType
                },
                npcAvatarFactory,
                npcInfoSupplier.npcAvatarExceptionHandler,
                npcInfoSupplier.npcInfoProtocolWorker,
            )

            private fun buildNpcAvatarFactory(
                allocator: ByteBufAllocator,
                npcInfoSupplier: NpcInfoSupplier,
                npcWriters: MutableList<NpcAvatarExtendedInfoWriter>,
                huffmanCodecProvider: HuffmanCodecProvider,
            ): NpcAvatarFactory {
                return NpcAvatarFactory(
                    allocator,
                    npcInfoSupplier.npcExtendedInfoFilter,
                    npcWriters,
                    huffmanCodecProvider,
                )
            }

            private fun buildPlayerInfoProtocol(
                allocator: ByteBufAllocator,
                playerInfoSupplier: PlayerInfoSupplier,
                playerAvatarFactory: PlayerAvatarFactory,
            ): PlayerInfoProtocol {
                return PlayerInfoProtocol(
                    allocator,
                    playerInfoSupplier.playerInfoProtocolWorker,
                    playerAvatarFactory,
                )
            }

            private fun buildPlayerAvatarFactory(
                allocator: ByteBufAllocator,
                playerInfoSupplier: PlayerInfoSupplier,
                playerWriters: MutableList<PlayerAvatarExtendedInfoWriter>,
                huffmanCodecProvider: HuffmanCodecProvider,
            ): PlayerAvatarFactory {
                return PlayerAvatarFactory(
                    allocator,
                    playerInfoSupplier.playerExtendedInfoFilter,
                    playerWriters,
                    huffmanCodecProvider,
                )
            }
        }
    }
