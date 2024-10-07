package net.rsprot.protocol.game.outgoing.map

import io.netty.buffer.ByteBuf
import io.netty.buffer.DefaultByteBufHolder
import net.rsprot.crypto.xtea.XteaKey
import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfo
import net.rsprot.protocol.game.outgoing.map.util.XteaProvider
import net.rsprot.protocol.game.outgoing.map.util.buildXteaKeyList

/**
 * Rebuild login is sent as part of the login procedure as the very first packet,
 * as this one contains information about everyone's low resolution position, allowing
 * the player information packet to be initialized properly.
 * @property zoneX the x coordinate of the local player's current zone.
 * @property zoneZ the z coordinate of the local player's current zone.
 * @property worldArea the current world area in which the player resides.
 * @property keys the list of xtea keys needed to decrypt the map.
 * @property gpiInitBlock the initialization block of the player info protocol,
 * used to inform the client of all the low resolution coordinates of everyone in the game.
 */
public class RebuildLogin private constructor(
    private val _zoneX: UShort,
    private val _zoneZ: UShort,
    private val _worldArea: UShort,
    override val keys: List<XteaKey>,
    public val gpiInitBlock: ByteBuf,
) : DefaultByteBufHolder(gpiInitBlock),
    StaticRebuildMessage {
    public constructor(
        zoneX: Int,
        zoneZ: Int,
        worldArea: Int,
        keyProvider: XteaProvider,
        playerInfo: PlayerInfo,
    ) : this(
        zoneX.toUShort(),
        zoneZ.toUShort(),
        worldArea.toUShort(),
        buildXteaKeyList(zoneX, zoneZ, keyProvider),
        initializePlayerInfo(playerInfo),
    )

    override val zoneX: Int
        get() = _zoneX.toInt()
    override val zoneZ: Int
        get() = _zoneZ.toInt()
    override val worldArea: Int
        get() = _worldArea.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT

    override fun estimateSize(): Int =
        Short.SIZE_BYTES +
            Short.SIZE_BYTES +
            Short.SIZE_BYTES +
            Short.SIZE_BYTES +
            (keys.size * (4 * Int.SIZE_BYTES)) +
            gpiInitBlock.readableBytes()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RebuildLogin

        if (_zoneX != other._zoneX) return false
        if (_zoneZ != other._zoneZ) return false
        if (_worldArea != other._worldArea) return false
        if (keys != other.keys) return false
        if (gpiInitBlock != other.gpiInitBlock) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _zoneX.hashCode()
        result = 31 * result + _zoneZ.hashCode()
        result = 31 * result + _worldArea.hashCode()
        result = 31 * result + keys.hashCode()
        result = 31 * result + gpiInitBlock.hashCode()
        return result
    }

    override fun toString(): String =
        "RebuildLogin(" +
            "keys=$keys, " +
            "gpiInitBlock=$gpiInitBlock, " +
            "zoneX=$zoneX, " +
            "zoneZ=$zoneZ, " +
            "worldArea=$worldArea" +
            ")"

    private companion object {
        private const val REBUILD_NORMAL_MAXIMUM_SIZE: Int = 44
        private const val PLAYER_INFO_BLOCK_SIZE = ((30 + (2046 * 18)) + Byte.SIZE_BITS - 1) ushr 3

        /**
         * Initializes the player info block into a buffer provided by allocator in the playerinfo object
         * @param playerInfo the player info protocol of this player to be initialized
         * @return a buffer containing the initialization block of the player info protocol
         */
        private fun initializePlayerInfo(playerInfo: PlayerInfo): ByteBuf {
            val allocator = playerInfo.allocator
            val buffer = allocator.buffer(PLAYER_INFO_BLOCK_SIZE + REBUILD_NORMAL_MAXIMUM_SIZE)
            playerInfo.handleAbsolutePlayerPositions(buffer)
            return buffer
        }
    }
}
