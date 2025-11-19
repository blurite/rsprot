package net.rsprot.protocol.binary

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.extensions.toJagByteBuf
import java.nio.file.Path

public data class BinaryHeader(
    public val path: Path,
    public val headerVersion: Int,
    public val revision: Int,
    public val subRevision: Int,
    public val clientType: Int,
    public val platformType: Int,
    public val timestamp: Long,
    public val worldId: Int,
    public val worldFlags: Int,
    public val worldLocation: Int,
    public val worldHost: String,
    public val worldActivity: String,
    public val localPlayerIndex: Int,
    public val accountHash: ByteArray,
    public val clientName: String,
    public val js5MasterIndex: ByteArray,
) {
    public fun encode(allocator: ByteBufAllocator): ByteBuf {
        val buffer = allocator.buffer(10_000).toJagByteBuf()
        buffer.p4(headerVersion)
        buffer.p4(revision)
        buffer.p4(subRevision)
        buffer.p1(clientType)
        buffer.p1(platformType)
        buffer.p8(timestamp)
        buffer.p2(worldId)
        buffer.p4(worldFlags)
        buffer.p1(worldLocation)
        buffer.pjstr(worldHost)
        buffer.pjstr(worldActivity)
        buffer.p2(localPlayerIndex)
        buffer.p2(accountHash.size)
        buffer.pdata(accountHash)
        buffer.pjstr(clientName)
        buffer.p4(js5MasterIndex.size)
        buffer.pdata(js5MasterIndex)
        return buffer.buffer
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BinaryHeader

        if (headerVersion != other.headerVersion) return false
        if (revision != other.revision) return false
        if (subRevision != other.subRevision) return false
        if (clientType != other.clientType) return false
        if (platformType != other.platformType) return false
        if (timestamp != other.timestamp) return false
        if (worldId != other.worldId) return false
        if (worldFlags != other.worldFlags) return false
        if (worldLocation != other.worldLocation) return false
        if (worldHost != other.worldHost) return false
        if (worldActivity != other.worldActivity) return false
        if (localPlayerIndex != other.localPlayerIndex) return false
        if (!accountHash.contentEquals(other.accountHash)) return false
        if (clientName != other.clientName) return false
        if (!js5MasterIndex.contentEquals(other.js5MasterIndex)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = headerVersion
        result = 31 * result + revision
        result = 31 * result + subRevision
        result = 31 * result + clientType
        result = 31 * result + platformType
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + worldId
        result = 31 * result + worldFlags
        result = 31 * result + worldLocation
        result = 31 * result + worldHost.hashCode()
        result = 31 * result + worldActivity.hashCode()
        result = 31 * result + localPlayerIndex
        result = 31 * result + accountHash.contentHashCode()
        result = 31 * result + clientName.hashCode()
        result = 31 * result + js5MasterIndex.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "BinaryHeader(" +
            "headerVersion=$headerVersion, " +
            "revision=$revision, " +
            "subRevision=$subRevision, " +
            "clientType=$clientType, " +
            "platformType=$platformType, " +
            "timestamp=$timestamp, " +
            "worldId=$worldId, " +
            "worldFlags=$worldFlags, " +
            "worldLocation=$worldLocation, " +
            "worldHost='$worldHost', " +
            "worldActivity='$worldActivity', " +
            "localPlayerIndex=$localPlayerIndex, " +
            "accountHash=${accountHash.contentToString()}, " +
            "clientName='$clientName', " +
            "js5MasterIndex=${js5MasterIndex.contentToString()}" +
            ")"
    }

    public class Builder {
        private var path: Path? = null
        private var headerVersion: Int = HEADER_VERSION
        private var revision: Int = -1
        private var subRevision: Int = -1
        private var clientType: Int = -1
        private var platformType: Int = -1
        private var timestamp: Long = -1
        private var worldId: Int = -1
        private var worldProperties: Int = -1
        private var worldLocation: Int = -1
        private var worldHost: String? = null
        private var worldActivity: String? = null
        private var localPlayerIndex: Int = -1
        private var accountHash: ByteArray? = null
        private var clientName: String? = null
        private var js5MasterIndex: ByteArray? = null

        public fun path(path: Path): Builder {
            this.path = path
            return this
        }

        public fun headerVersion(value: Int): Builder {
            this.headerVersion = value
            return this
        }

        public fun revision(value: Int): Builder {
            this.revision = value
            return this
        }

        public fun subRevision(value: Int): Builder {
            this.subRevision = value
            return this
        }

        public fun clientType(value: Int): Builder {
            this.clientType = value
            return this
        }

        public fun platformType(value: Int): Builder {
            this.platformType = value
            return this
        }

        public fun timestamp(value: Long): Builder {
            this.timestamp = value
            return this
        }

        public fun worldId(worldId: Int): Builder {
            this.worldId = worldId
            return this
        }

        public fun worldProperties(worldProperties: Int): Builder {
            this.worldProperties = worldProperties
            return this
        }

        public fun worldLocation(worldLocation: Int): Builder {
            this.worldLocation = worldLocation
            return this
        }

        public fun worldHost(worldHost: String): Builder {
            this.worldHost = worldHost
            return this
        }

        public fun worldActivity(worldActivity: String): Builder {
            this.worldActivity = worldActivity
            return this
        }

        public fun localPlayerIndex(value: Int): Builder {
            this.localPlayerIndex = value
            return this
        }

        public fun accountHash(value: ByteArray): Builder {
            this.accountHash = value
            return this
        }

        public fun clientName(value: String): Builder {
            this.clientName = value
            return this
        }

        public fun js5MasterIndex(value: ByteArray): Builder {
            this.js5MasterIndex = value
            return this
        }

        public fun build(): BinaryHeader {
            val path = checkNotNull(path) { "Path uninitialized" }
            check(headerVersion != -1) { "Header version uninitialized" }
            check(revision != -1) { "Revision uninitialized" }
            check(subRevision != -1) { "Sub revision uninitialized" }
            check(clientType != -1) { "Client type uninitialized" }
            check(platformType != -1) { "Platform type uninitialized" }
            check(timestamp != -1L) { "Login timestamp uninitialized" }
            check(localPlayerIndex != -1) { "Local player index uninitialized" }
            val accountHash = this.accountHash
            check(accountHash != null) { "Account hash uninitialized" }
            check(worldId != -1) { "World id uninitialized" }
            check(worldProperties != -1) { "World properties uninitialized" }
            check(worldLocation != -1) { "World location uninitialized" }
            val worldHost = checkNotNull(worldHost) { "World host uninitialized" }
            val worldActivity = checkNotNull(worldActivity) { "World activity uninitialized" }
            val clientName = this.clientName
            checkNotNull(clientName) { "Client name uninitialized" }
            val js5MasterIndex = this.js5MasterIndex
            checkNotNull(js5MasterIndex) { "JS5 Master Index uninitialized" }
            return BinaryHeader(
                path,
                headerVersion,
                revision,
                subRevision,
                clientType,
                platformType,
                timestamp,
                worldId,
                worldProperties,
                worldLocation,
                worldHost,
                worldActivity,
                localPlayerIndex,
                accountHash,
                clientName,
                js5MasterIndex,
            )
        }
    }

    private companion object {
        private const val HEADER_VERSION: Int = 1
    }
}
