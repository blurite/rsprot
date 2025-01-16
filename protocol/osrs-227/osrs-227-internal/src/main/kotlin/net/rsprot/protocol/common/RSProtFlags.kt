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

    private val networkLoggingString: String =
        getString(
            "networkLogging",
            "off",
        )

    private val js5LoggingString: String =
        getString(
            "js5Logging",
            "off",
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

    @JvmStatic
    public val filterMissingPacketsInClient: Boolean =
        getBoolean(
            "filterMissingPacketsInClient",
            true,
        )

    @JvmStatic
    public val npcAvatarMaxId: Int =
        getInt(
            "npcAvatarMaxId",
            16383,
        )

    @JvmStatic
    public val spotanimListCapacity: Int =
        getInt(
            "spotanimListCapacity",
            256,
        )

    @JvmStatic
    public val captureChat: Boolean =
        getBoolean(
            "captureChat",
            false,
        )

    @JvmStatic
    public val captureSay: Boolean =
        getBoolean(
            "captureSay",
            false,
        )

    @JvmStatic
    public val networkLogging: LogLevel =
        when (networkLoggingString) {
            "off" -> LogLevel.OFF
            "trace" -> LogLevel.TRACE
            "debug" -> LogLevel.DEBUG
            "info" -> LogLevel.INFO
            "warn" -> LogLevel.WARN
            "error" -> LogLevel.ERROR
            else -> {
                logger.warn {
                    "Unknown network logging option: $networkLoggingString, " +
                        "expected values: [off, trace, debug, info, warn, error]"
                }
                LogLevel.OFF
            }
        }

    @JvmStatic
    public val js5Logging: LogLevel =
        when (js5LoggingString) {
            "off" -> LogLevel.OFF
            "trace" -> LogLevel.TRACE
            "debug" -> LogLevel.DEBUG
            "info" -> LogLevel.INFO
            "warn" -> LogLevel.WARN
            "error" -> LogLevel.ERROR
            else -> {
                logger.warn {
                    "Unknown js5 logging option: $networkLoggingString, " +
                        "expected values: [off, trace, debug, info, warn, error]"
                }
                LogLevel.OFF
            }
        }

    init {
        log("development", development)
        log("inventoryObjCheck", inventoryObjCheck)
        log("extendedInfoInputVerification", extendedInfoInputVerification)
        log("clientscriptVerification", clientscriptVerification)
        log("networkLogging", networkLoggingString)
        log("js5Logging", js5LoggingString)
        log("npcPlayerAvatarTracking", npcPlayerAvatarTracking)
        log("filterMissingPacketsInClient", filterMissingPacketsInClient)
        log("npcAvatarMaxId", npcAvatarMaxId)
        log("spotanimListCapacity", spotanimListCapacity)
        log("captureChat", captureChat)
        log("captureSay", captureSay)
        require(npcAvatarMaxId == -1 || npcAvatarMaxId <= 65534)
        require(spotanimListCapacity in 0..256)
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
    private fun getString(
        propertyName: String,
        defaultValue: String,
    ): String =
        SystemPropertyUtil.get(
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
        value: Any,
    ) {
        logger.debug {
            "-D${PREFIX}$name: $value"
        }
    }
}
