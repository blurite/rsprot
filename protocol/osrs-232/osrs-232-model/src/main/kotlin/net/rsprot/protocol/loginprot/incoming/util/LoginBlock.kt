package net.rsprot.protocol.loginprot.incoming.util

import net.rsprot.protocol.loginprot.incoming.RemainingBetaArchives

@Suppress("MemberVisibilityCanBePrivate", "DuplicatedCode")
public class LoginBlock<T>(
    public val header: Header,
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
    public val deepLinks: List<Int>,
    public val hostPlatformStats: HostPlatformStats,
    private val _validationClientType: UByte,
    public val reflectionCheckerConst: Int,
    public val crc: CyclicRedundancyCheckBlock,
    public val authentication: T,
) {
    // Property delegates for backwards compatibility
    public val version: Int
        get() = header.version
    public val subVersion: Int
        get() = header.subVersion
    public val serverVersion: Int
        get() = header.serverVersion
    public val clientType: LoginClientType
        get() = header.clientType
    public val platformType: LoginPlatformType
        get() = header.platformType
    public val hasExternalAuthenticator: Boolean
        get() = header.hasExternalAuthenticator

    public val width: Int
        get() = _width.toInt()
    public val height: Int
        get() = _height.toInt()
    public val validationClientType: LoginClientType
        get() = LoginClientType[_validationClientType.toInt()]

    public fun mergeBetaCrcs(remainingBetaArchives: RemainingBetaArchives) {
        for (i in remainingBetaArchiveIndices) {
            this.crc.set(i, remainingBetaArchives.crc[i])
        }
    }

    public class Header(
        public val version: Int,
        public val subVersion: Int,
        public val serverVersion: Int,
        private val _clientType: UByte,
        private val _platformType: UByte,
        public val hasExternalAuthenticator: Boolean,
    ) {
        public val clientType: LoginClientType
            get() = LoginClientType[_clientType.toInt()]
        public val platformType: LoginPlatformType
            get() = LoginPlatformType[_platformType.toInt()]

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Header

            if (version != other.version) return false
            if (subVersion != other.subVersion) return false
            if (serverVersion != other.serverVersion) return false
            if (hasExternalAuthenticator != other.hasExternalAuthenticator) return false
            if (_clientType != other._clientType) return false
            if (_platformType != other._platformType) return false

            return true
        }

        override fun hashCode(): Int {
            var result = version
            result = 31 * result + subVersion
            result = 31 * result + serverVersion
            result = 31 * result + hasExternalAuthenticator.hashCode()
            result = 31 * result + _clientType.hashCode()
            result = 31 * result + _platformType.hashCode()
            return result
        }

        override fun toString(): String {
            return "Header(" +
                "version=$version, " +
                "subVersion=$subVersion, " +
                "serverVersion=$serverVersion, " +
                "hasExternalAuthenticator=$hasExternalAuthenticator, " +
                "clientType=$clientType, " +
                "platformType=$platformType" +
                ")"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LoginBlock<*>

        if (header != other.header) return false
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
        if (deepLinks != other.deepLinks) return false
        if (hostPlatformStats != other.hostPlatformStats) return false
        if (_validationClientType != other._validationClientType) return false
        if (reflectionCheckerConst != other.reflectionCheckerConst) return false
        if (crc != other.crc) return false
        if (authentication != other.authentication) return false

        return true
    }

    override fun hashCode(): Int {
        var result = header.hashCode()
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
        result = 31 * result + deepLinks.hashCode()
        result = 31 * result + hostPlatformStats.hashCode()
        result = 31 * result + _validationClientType.hashCode()
        result = 31 * result + reflectionCheckerConst
        result = 31 * result + crc.hashCode()
        result = 31 * result + (authentication?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "LoginBlock(" +
            "header=$header, " +
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
            "deepLinks=$deepLinks, " +
            "width=$width, " +
            "height=$height, " +
            "validationClientType=$validationClientType, " +
            "reflectionCheckerConst=$reflectionCheckerConst, " +
            "authentication=$authentication" +
            ")"
    }

    private companion object {
        private val remainingBetaArchiveIndices =
            listOf(
                0,
                1,
                2,
                3,
                5,
                7,
                9,
                11,
                12,
                16,
                17,
                18,
                19,
                20,
            )
    }
}
