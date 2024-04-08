package net.rsprot.protocol.game.outgoing.map

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import net.rsprot.crypto.util.XteaKey
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfo
import net.rsprot.protocol.game.outgoing.map.util.XteaProvider
import net.rsprot.protocol.game.outgoing.map.util.buildXteaKeyList

/**
 * Rebuild login is sent as part of the login procedure as the very first packet,
 * as this one contains information about everyone's low resolution position, allowing
 * the player information packet to be initialized properly.
 * @property zoneX the x coordinate of the local player's current zone.
 * @property zoneZ the z coordinate of the local player's current zone.
 * @property keys the list of xtea keys needed to decrypt the map.
 * @property gpiInitBlock the initialization block of the player info protocol,
 * used to inform the client of all the low resolution coordinates of everyone in the game.
 */
@Suppress("MemberVisibilityCanBePrivate")
public class RebuildLogin private constructor(
    private val _zoneX: UShort,
    private val _zoneZ: UShort,
    override val keys: List<XteaKey>,
    public val gpiInitBlock: ByteBuf,
) : StaticRebuildMessage {
    public constructor(
        zoneX: Int,
        zoneZ: Int,
        keyProvider: XteaProvider,
        allocator: ByteBufAllocator,
        playerInfo: PlayerInfo,
    ) : this(
        zoneX.toUShort(),
        zoneZ.toUShort(),
        buildXteaKeyList(zoneX, zoneZ, keyProvider),
        initializePlayerInfo(allocator, playerInfo),
    )

    override val zoneX: Int
        get() = _zoneX.toInt()
    override val zoneZ: Int
        get() = _zoneZ.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RebuildLogin

        if (_zoneX != other._zoneX) return false
        if (_zoneZ != other._zoneZ) return false
        if (keys != other.keys) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _zoneX.hashCode()
        result = 31 * result + _zoneZ.hashCode()
        result = 31 * result + keys.hashCode()
        return result
    }

    override fun toString(): String {
        return "RebuildNormal(" +
            "zoneX=$zoneX, " +
            "zoneZ=$zoneZ, " +
            "keys=$keys, " +
            "gpiInitBlock=$gpiInitBlock" +
            ")"
    }

    private companion object {
        private const val PLAYER_INFO_BLOCK_SIZE = ((30 + (2046 * 18)) + Int.SIZE_BITS - 1) ushr 5

        /**
         * Initializes the player info block into a buffer provided by the [allocator].
         * @param allocator the allocator to obtain the buffer from
         * @param playerInfo the player info protocol of this player to be initialized
         * @return a buffer containing the initialization block of the player info protocol
         */
        private fun initializePlayerInfo(
            allocator: ByteBufAllocator,
            playerInfo: PlayerInfo,
        ): ByteBuf {
            val buffer = allocator.buffer(PLAYER_INFO_BLOCK_SIZE)
            playerInfo.handleAbsolutePlayerPositions(buffer)
            return buffer
        }
    }
}
