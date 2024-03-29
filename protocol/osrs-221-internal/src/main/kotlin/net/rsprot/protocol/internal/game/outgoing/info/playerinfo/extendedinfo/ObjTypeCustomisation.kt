package net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo

public class ObjTypeCustomisation(
    public var recolIndices: UByte,
    public var recol1: UShort,
    public var recol2: UShort,
    public var retexIndices: UByte,
    public var retex1: UShort,
    public var retex2: UShort,
) {
    public constructor() : this(
        recolIndices = 0xFFu,
        recol1 = 0u,
        recol2 = 0u,
        retexIndices = 0xFFu,
        retex1 = 0u,
        retex2 = 0u,
    )
}
