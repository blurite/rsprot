package net.rsprot.protocol.api.js5

/**
 * An interface to authorize certain [String]es, and check if they are authorized
 * to download certain parts of the cache. This feature is only enabled if the world
 * has the beta flag enabled.
 */
public interface Js5Authorizer {
    /**
     * Authorizes an INetAddress to download the cache in its entirety on beta worlds.
     *
     * @param address the [String] to authorize.
     */
    public fun authorize(address: String)

    /**
     * Authorizes an INetAddress, so it can no longer download certain archives of the cache.
     * Note that it is possible for multiple connections to be open by the user.
     * In such cases, the authorization remains active until the last connection dies.
     *
     * @param address the [String] to authorize.
     */
    public fun unauthorize(address: String)

    /**
     * Checks if a cache archive is authorized for the specified [String].
     *
     * @param address the [String] to check for authorization.
     * @param archive the cache archive to check. Only certain cache archives
     * are protected.
     */
    public fun isAuthorized(
        address: String,
        archive: Int,
    ): Boolean
}
