package net.rsprot.protocol.game.outgoing.map.util

import net.rsprot.crypto.util.XteaKey

public fun interface XteaProvider {
    public fun provide(mapsquareId: Int): XteaKey

    public companion object {
        @JvmStatic
        public val ZERO_XTEA_KEY_PROVIDER: XteaProvider = XteaProvider { XteaKey.ZERO }
    }
}
