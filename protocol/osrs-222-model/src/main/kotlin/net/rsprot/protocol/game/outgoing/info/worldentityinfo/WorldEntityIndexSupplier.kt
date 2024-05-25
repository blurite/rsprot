package net.rsprot.protocol.game.outgoing.info.worldentityinfo

public fun interface WorldEntityIndexSupplier {
    public fun supply(
        localPlayerIndex: Int,
        level: Int,
        x: Int,
        z: Int,
        viewDistance: Int,
    ): Iterator<Int>
}
