package net.rsprot.protocol.api.logging

import com.github.michaelbull.logging.InlineLogger
import net.rsprot.protocol.common.RSProtFlags

/**
 * Performs a debug log if network logging flag is enabled.
 * This effectively allows one to apply a second filter as network stuff
 * is fairly high pressure. Furthermore, since the logger level checks
 * actually consist of quite a lot of function calls and checks,
 * they're not as cheap as one might expect, so running a preliminary
 * boolean check on it beforehand avoids doing any of that work, meaning
 * this should have virtually no effect in production if disabled.
 */
internal inline fun networkLog(
    logger: InlineLogger,
    block: () -> Any?,
) {
    if (RSProtFlags.networkLogging) {
        logger.debug(block)
    }
}

/**
 * Performs a debug log if JS5 logging flag is enabled.
 * This effectively allows one to apply a second filter as JS5 logging
 * is extremely high pressure. Furthermore, since the logger level checks
 * actually consist of quite a lot of function calls and checks,
 * they're not as cheap as one might expect, so running a preliminary
 * boolean check on it beforehand avoids doing any of that work, meaning
 * this should have virtually no effect in production if disabled.
 */
internal inline fun js5Log(
    logger: InlineLogger,
    block: () -> Any?,
) {
    if (RSProtFlags.js5Logging) {
        logger.debug(block)
    }
}
