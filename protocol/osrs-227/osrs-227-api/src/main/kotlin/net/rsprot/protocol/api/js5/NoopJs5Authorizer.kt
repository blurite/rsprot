package net.rsprot.protocol.api.js5

import java.net.SocketAddress

/**
 * A no-op implementation of the JS5 authorizer.
 * This implementation ignores all requests and accepts any requests made.
 */
public data object NoopJs5Authorizer : Js5Authorizer {
    override fun authorize(address: SocketAddress) {
    }

    override fun unauthorize(address: SocketAddress) {
    }

    override fun isAuthorized(
        address: SocketAddress,
        archive: Int,
    ): Boolean {
        return true
    }
}
