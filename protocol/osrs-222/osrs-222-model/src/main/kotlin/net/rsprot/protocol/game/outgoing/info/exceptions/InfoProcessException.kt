package net.rsprot.protocol.game.outgoing.info.exceptions

/**
 * An exception that is sent whenever an info packet gets built by the server.
 * This exception wraps around another exception that was generated during the processing of
 * the respective info packet, allowing servers to properly observe and handle the exception.
 * @param message the message associated with the exception
 * @param throwable the throwable that was thrown during info processing
 */
public class InfoProcessException(
    message: String,
    throwable: Throwable,
) : RuntimeException(message, throwable)
