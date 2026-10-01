package net.rsprot.protocol.common.loginprot.incoming.codec.shared.exceptions

/**
 * A singleton exception for whenever login decoding fails due to the revision being out of date.
 * It is not ideal to be using exceptions for flow control, but it is by far the easiest option
 * here. From a performance standpoint, only building stack traces is slow, which we aren't
 * going to be doing for this type of exception.
 */
public data object InvalidVersionException : RuntimeException() {
    @Suppress("unused")
    private fun readResolve(): Any = InvalidVersionException
}
