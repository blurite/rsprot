package net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.util

public class PlayerBodyType(public val values: ShortArray = ShortArray(12)) : BodyType {
    internal companion object {
        val DEFAULT = PlayerBodyType()
    }
}
