package net.rsprot.protocol.game.outgoing.map.util

import net.rsprot.crypto.util.XteaKey

public fun interface XteaProvider {
    public fun provide(mapsquareId: Int): XteaKey
}
