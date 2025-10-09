package net.rsprot.protocol.api.js5

/**
 * A no-op implementation of the JS5 authorizer.
 * This implementation ignores all requests and accepts any requests made.
 */
public data object NoopJs5Authorizer : Js5Authorizer {
    override fun authorize(address: String) {
    }

    override fun unauthorize(address: String) {
    }

    override fun isAuthorized(
        address: String,
        archive: Int,
    ): Boolean {
        return true
    }
}
