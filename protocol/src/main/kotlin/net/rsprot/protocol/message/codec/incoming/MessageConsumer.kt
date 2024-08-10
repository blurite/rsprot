package net.rsprot.protocol.message.codec.incoming

import net.rsprot.protocol.message.IncomingGameMessage

/**
 * A functional interface that allows servers to bind to specific incoming game messages and
 * handle them with a specific receiver of type [R], typically being a Player.
 * @param R the receiver of the message on which state is modified.
 * @param T the incoming game message to be handled.
 */
public fun interface MessageConsumer<in R, in T : IncomingGameMessage> {
    /**
     * A function to consume a specific incoming game message of type [T].
     * @param receiver the receiver object on which state is modified corresponding to this packet.
     * @param message the message that provides information about what to do.
     */
    public fun consume(
        receiver: R,
        message: T,
    )
}
