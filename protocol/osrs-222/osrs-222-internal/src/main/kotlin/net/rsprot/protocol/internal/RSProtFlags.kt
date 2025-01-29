package net.rsprot.protocol.internal

import com.github.michaelbull.logging.InlineLogger
import io.netty.util.internal.SystemPropertyUtil

/**
 * An internal object that provides easy access to various error-checking flags.
 * The purpose of this object is to avoid scattering these checks throughout
 * the codebase, making it difficult for users to find any.
 * Additionally, requires duplication of code to re-create all this.
 */
public object RSProtFlags {
    private val logger: InlineLogger = InlineLogger()
    private const val PREFIX = "net.rsprot.protocol.internal."

    /**
     * Whether the server is in 'development' mode.
     * Development mode is effectively a mode where all
     * checks are performed to ensure all inputs are validated.
     * Users are expected to turn development mode off when
     * putting the server into production, as these checks
     * end up taking a considerable amount of time.
     */
    @JvmStatic
    private val development: Boolean =
	    net.rsprot.protocol.internal.RSProtFlags.getBoolean(
		    "development",
		    true,
	    )

    /**
     * Whether to check that obj ids in inventory packets are all positive.
     */
    @JvmStatic
    public val inventoryObjCheck: Boolean =
	    net.rsprot.protocol.internal.RSProtFlags.getBoolean(
		    "inventoryObjCheck",
		    net.rsprot.protocol.internal.RSProtFlags.development,
	    )

    /**
     * Whether to validate extended info block inputs.
     */
    @JvmStatic
    public val extendedInfoInputVerification: Boolean =
	    net.rsprot.protocol.internal.RSProtFlags.getBoolean(
		    "extendedInfoInputVerification",
		    net.rsprot.protocol.internal.RSProtFlags.development,
	    )

    @JvmStatic
    public val clientscriptVerification: Boolean =
	    net.rsprot.protocol.internal.RSProtFlags.getBoolean(
		    "clientscriptVerification",
		    net.rsprot.protocol.internal.RSProtFlags.development,
	    )

    @JvmStatic
    public val networkLogging: Boolean =
	    net.rsprot.protocol.internal.RSProtFlags.getBoolean(
		    "networkLogging",
		    false,
	    )

    @JvmStatic
    public val js5Logging: Boolean =
	    net.rsprot.protocol.internal.RSProtFlags.getBoolean(
		    "js5Logging",
		    false,
	    )

    @JvmStatic
    public val byteBufRecyclerCycleThreshold: Int =
	    net.rsprot.protocol.internal.RSProtFlags.getInt(
		    "recyclerCycleThreshold",
		    50,
	    )

    @JvmStatic
    public val npcPlayerAvatarTracking: Boolean =
	    net.rsprot.protocol.internal.RSProtFlags.getBoolean(
		    "npcPlayerAvatarTracking",
		    true,
	    )

    @JvmStatic
    public val filterMissingPacketsInClient: Boolean =
	    net.rsprot.protocol.internal.RSProtFlags.getBoolean(
		    "filterMissingPacketsInClient",
		    true,
	    )

    @JvmStatic
    public val spotanimListCapacity: Int =
	    net.rsprot.protocol.internal.RSProtFlags.getInt(
		    "spotanimListCapacity",
		    256,
	    )

    @JvmStatic
    public val captureChat: Boolean =
	    net.rsprot.protocol.internal.RSProtFlags.getBoolean(
		    "captureChat",
		    false,
	    )

    @JvmStatic
    public val captureSay: Boolean =
	    net.rsprot.protocol.internal.RSProtFlags.getBoolean(
		    "captureSay",
		    false,
	    )

    init {
	    net.rsprot.protocol.internal.RSProtFlags.log(
		    "development",
		    net.rsprot.protocol.internal.RSProtFlags.development
	    )
	    net.rsprot.protocol.internal.RSProtFlags.log(
		    "inventoryObjCheck",
		    net.rsprot.protocol.internal.RSProtFlags.inventoryObjCheck
	    )
	    net.rsprot.protocol.internal.RSProtFlags.log(
		    "extendedInfoInputVerification",
		    net.rsprot.protocol.internal.RSProtFlags.extendedInfoInputVerification
	    )
	    net.rsprot.protocol.internal.RSProtFlags.log(
		    "clientscriptVerification",
		    net.rsprot.protocol.internal.RSProtFlags.clientscriptVerification
	    )
	    net.rsprot.protocol.internal.RSProtFlags.log(
		    "networkLogging",
		    net.rsprot.protocol.internal.RSProtFlags.networkLogging
	    )
	    net.rsprot.protocol.internal.RSProtFlags.log("js5Logging", net.rsprot.protocol.internal.RSProtFlags.js5Logging)
	    net.rsprot.protocol.internal.RSProtFlags.log(
		    "npcPlayerAvatarTracking",
		    net.rsprot.protocol.internal.RSProtFlags.npcPlayerAvatarTracking
	    )
	    net.rsprot.protocol.internal.RSProtFlags.log(
		    "filterMissingPacketsInClient",
		    net.rsprot.protocol.internal.RSProtFlags.filterMissingPacketsInClient
	    )
	    net.rsprot.protocol.internal.RSProtFlags.log(
		    "spotanimListCapacity",
		    net.rsprot.protocol.internal.RSProtFlags.spotanimListCapacity
	    )
	    net.rsprot.protocol.internal.RSProtFlags.log(
		    "captureChat",
		    net.rsprot.protocol.internal.RSProtFlags.captureChat
	    )
	    net.rsprot.protocol.internal.RSProtFlags.log("captureSay", net.rsprot.protocol.internal.RSProtFlags.captureSay)
        require(net.rsprot.protocol.internal.RSProtFlags.spotanimListCapacity in 0..256)
    }

    private fun getBoolean(
        propertyName: String,
        defaultValue: Boolean,
    ): Boolean =
        SystemPropertyUtil.getBoolean(
            net.rsprot.protocol.internal.RSProtFlags.PREFIX + propertyName,
            defaultValue,
        )

    @Suppress("SameParameterValue")
    private fun getInt(
        propertyName: String,
        defaultValue: Int,
    ): Int =
        SystemPropertyUtil
            .get(
                net.rsprot.protocol.internal.RSProtFlags.PREFIX + propertyName,
                defaultValue.toString(),
            )?.toIntOrNull() ?: defaultValue

    private fun log(
        name: String,
        value: Any,
    ) {
        net.rsprot.protocol.internal.RSProtFlags.logger.debug {
            "-D${net.rsprot.protocol.internal.RSProtFlags.PREFIX}$name: $value"
        }
    }
}
