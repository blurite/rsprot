package net.rsprot.protocol.game.outgoing.info.worldentityinfo

import java.lang.Exception

public fun interface WorldEntityAvatarExceptionHandler {
    /**
     * This function is triggered whenever there's an exception caught during world entity
     * avatar processing.
     * @param index the index of the world entity that had an exception during its processing.
     * @param exception the exception that was caught during a world entity's avatar processing
     */
    public fun exceptionCaught(
        index: Int,
        exception: Exception,
    )
}
