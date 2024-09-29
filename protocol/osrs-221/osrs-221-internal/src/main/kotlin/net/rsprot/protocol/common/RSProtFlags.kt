package net.rsprot.protocol.common

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
        getBoolean(
            "development",
            true,
        )

    /**
     * Whether to check that obj ids in inventory packets are all positive.
     */
    @JvmStatic
    public val inventoryObjCheck: Boolean =
        getBoolean(
            "inventoryObjCheck",
            development,
        )

    /**
     * Whether to validate extended info block inputs.
     */
    @JvmStatic
    public val extendedInfoInputVerification: Boolean =
        getBoolean(
            "extendedInfoInputVerification",
            development,
        )

    @JvmStatic
    public val clientscriptVerification: Boolean =
        getBoolean(
            "clientscriptVerification",
            development,
        )

    @JvmStatic
    public val networkLogging: Boolean =
        getBoolean(
            "networkLogging",
            false,
        )

    @JvmStatic
    public val js5Logging: Boolean =
        getBoolean(
            "js5Logging",
            false,
        )

    @JvmStatic
    public val byteBufRecyclerCycleThreshold: Int =
        getInt(
            "recyclerCycleThreshold",
            50,
        )

    @JvmStatic
    public val npcPlayerAvatarTracking: Boolean =
        getBoolean(
            "npcPlayerAvatarTracking",
            true,
        )

    init {
        log("development", development)
        log("inventoryObjCheck", inventoryObjCheck)
        log("extendedInfoInputVerification", extendedInfoInputVerification)
        log("clientscriptVerification", clientscriptVerification)
        log("networkLogging", networkLogging)
        log("js5Logging", js5Logging)
        log("npcPlayerAvatarTracking", npcPlayerAvatarTracking)
    }

    private fun getBoolean(
        propertyName: String,
        defaultValue: Boolean,
    ): Boolean =
        SystemPropertyUtil.getBoolean(
            PREFIX + propertyName,
            defaultValue,
        )

    @Suppress("SameParameterValue")
    private fun getInt(
        propertyName: String,
        defaultValue: Int,
    ): Int =
        SystemPropertyUtil
            .get(
                PREFIX + propertyName,
                defaultValue.toString(),
            )?.toIntOrNull() ?: defaultValue

    private fun log(
        name: String,
        value: Boolean,
    ) {
        logger.debug {
            "-D${PREFIX}$name: $value"
        }
    }
}
