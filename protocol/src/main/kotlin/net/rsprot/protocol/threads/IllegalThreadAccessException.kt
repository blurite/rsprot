package net.rsprot.protocol.threads

/**
 * An illegal thread access exception is thrown when the server assigns a dedicated
 * communication thread through which the server is allowed to communicate with
 * RSProt's thread-unsafe functions, ensuring that any illegal accesses cannot
 * take place while sensitive operations are happening.
 * @param message the message to display alongside the exception.
 */
public class IllegalThreadAccessException(
    message: String,
) : RuntimeException(message)
