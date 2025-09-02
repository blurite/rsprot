package net.rsprot.protocol.api.js5

import java.net.InetAddress

/**
 * A no-op implementation of the JS5 authorizer.
 * This implementation ignores all requests and accepts any requests made.
 */
public data object NoopJs5Authorizer : Js5Authorizer {
    override fun authorize(address: InetAddress) {
    }

    override fun unauthorize(address: InetAddress) {
    }

    override fun isAuthorized(
        address: InetAddress,
        archive: Int,
    ): Boolean {
        return true
    }
}
