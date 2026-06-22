package net.rsprot.protocol.game.outgoing.info.npcinfo

import java.lang.Exception

/**
 * An exception handler for npc avatar processing.
 * This is necessary as we might run into hiccups during computations of a specific npc,
 * in which case we need to propagate the exceptions to the server, which will ideally remove said npcs
 * from the world as a result of it.
 */
public fun interface NpcAvatarExceptionHandler {
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
