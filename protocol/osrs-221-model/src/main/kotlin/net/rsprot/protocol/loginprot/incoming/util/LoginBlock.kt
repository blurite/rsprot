package net.rsprot.protocol.loginprot.incoming.util

import net.rsprot.protocol.message.IncomingMessage

@Suppress("MemberVisibilityCanBePrivate", "DuplicatedCode")
public class LoginBlock<T>(
    public val version: Int,
    public val subVersion: Int,
    private val _firstClientType: UByte,
    private val _platformType: UByte,
    private val _constZero1: UByte,
    public val seed: IntArray,
    public val sessionId: Long,
    public val username: String,
    public val lowDetail: Boolean,
    public val resizable: Boolean,
    private val _width: UShort,
    private val _height: UShort,
    public val uuid: ByteArray,
    public val siteSettings: String,
    public val affiliate: Int,
    private val _constZero2: UByte,
    public val hostPlatformStats: HostPlatformStats,
    private val _secondClientType: UByte,
    private val _crcBlockHeader: UByte,
    public val crc: CyclicRedundancyCheckBlock,
    public val authentication: T,
) : IncomingMessage {
    public val firstClientType: Int
        get() = _firstClientType.toInt()
    public val platformType: Int
        get() = _platformType.toInt()
    public val constZero1: Int
        get() = _constZero1.toInt()
    public val width: Int
        get() = _width.toInt()
    public val height: Int
        get() = _height.toInt()
    public val constZero2: Int
        get() = _constZero2.toInt()
    public val secondClientType: Int
        get() = _secondClientType.toInt()
    public val crcBlockHeader: Int
        get() = _crcBlockHeader.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LoginBlock<*>

        if (version != other.version) return false
        if (subVersion != other.subVersion) return false
        if (_firstClientType != other._firstClientType) return false
        if (_platformType != other._platformType) return false
        if (_constZero1 != other._constZero1) return false
        if (!seed.contentEquals(other.seed)) return false
        if (sessionId != other.sessionId) return false
        if (username != other.username) return false
        if (lowDetail != other.lowDetail) return false
        if (resizable != other.resizable) return false
        if (_width != other._width) return false
        if (_height != other._height) return false
        if (!uuid.contentEquals(other.uuid)) return false
        if (siteSettings != other.siteSettings) return false
        if (affiliate != other.affiliate) return false
        if (_constZero2 != other._constZero2) return false
        if (hostPlatformStats != other.hostPlatformStats) return false
        if (_secondClientType != other._secondClientType) return false
        if (_crcBlockHeader != other._crcBlockHeader) return false
        if (crc != other.crc) return false
        if (authentication != other.authentication) return false

        return true
    }

    override fun hashCode(): Int {
        var result = version
        result = 31 * result + subVersion
        result = 31 * result + _firstClientType.hashCode()
        result = 31 * result + _platformType.hashCode()
        result = 31 * result + _constZero1.hashCode()
        result = 31 * result + seed.contentHashCode()
        result = 31 * result + sessionId.hashCode()
        result = 31 * result + username.hashCode()
        result = 31 * result + lowDetail.hashCode()
        result = 31 * result + resizable.hashCode()
        result = 31 * result + _width.hashCode()
        result = 31 * result + _height.hashCode()
        result = 31 * result + uuid.contentHashCode()
        result = 31 * result + siteSettings.hashCode()
        result = 31 * result + affiliate
        result = 31 * result + _constZero2.hashCode()
        result = 31 * result + hostPlatformStats.hashCode()
        result = 31 * result + _secondClientType.hashCode()
        result = 31 * result + _crcBlockHeader.hashCode()
        result = 31 * result + crc.hashCode()
        result = 31 * result + authentication.hashCode()
        return result
    }

    override fun toString(): String {
        return "LoginBlock(" +
            "version=$version, " +
            "subVersion=$subVersion, " +
            "seed=${seed.contentToString()}, " +
            "sessionId=$sessionId, " +
            "username='$username', " +
            "lowDetail=$lowDetail, " +
            "resizable=$resizable, " +
            "uuid=${uuid.contentToString()}, " +
            "siteSettings='$siteSettings', " +
            "affiliate=$affiliate, " +
            "hostPlatformStats=$hostPlatformStats, " +
            "crc=$crc, " +
            "firstClientType=$firstClientType, " +
            "platformType=$platformType, " +
            "constZero1=$constZero1, " +
            "constZero2=$constZero2, " +
            "width=$width, " +
            "height=$height, " +
            "secondClientType=$secondClientType, " +
            "crcBlockHeader=$crcBlockHeader, " +
            "authentication=$authentication" +
            ")"
    }
}
