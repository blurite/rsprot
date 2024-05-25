package net.rsprot.protocol.game.outgoing.info.worldentityinfo

import java.lang.Exception

public fun interface WorldEntityExceptionHandler {
    /**
     * This function is triggered whenever there's an exception caught during npc
     * avatar processing.
     * @param index the index of the npc that had an exception during its processing.
     * @param exception the exception that was caught during a npc's avatar processing
     */
    public fun exceptionCaught(
        index: Int,
        exception: Exception,
    )
}
