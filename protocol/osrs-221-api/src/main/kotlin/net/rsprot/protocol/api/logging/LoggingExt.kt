package net.rsprot.protocol.api.logging

import com.github.michaelbull.logging.InlineLogger
import net.rsprot.protocol.common.RSProtFlags

internal inline fun networkLog(
    logger: InlineLogger,
    block: () -> Any?,
) {
    if (RSProtFlags.networkLogging) {
        logger.debug(block)
    }
}

internal inline fun js5Log(
    logger: InlineLogger,
    block: () -> Any?,
) {
    if (RSProtFlags.js5Logging) {
        logger.debug(block)
    }
}
