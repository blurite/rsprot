package net.rsprot.protocol.common.loginprot.incoming.codec.shared.exceptions

/**
 * A singleton exception for whenever login decoding fails due to an unsupported client connecting to it.
 * It is not ideal to be using exceptions for flow control, but it is by far the easiest option
 * here. From a performance standpoint, only building stack traces is slow, which we aren't
 * going to be doing for this type of exception.
 */
public data object UnsupportedClientException : RuntimeException() {
    @Suppress("unused")
    private fun readResolve(): Any = UnsupportedClientException
}
