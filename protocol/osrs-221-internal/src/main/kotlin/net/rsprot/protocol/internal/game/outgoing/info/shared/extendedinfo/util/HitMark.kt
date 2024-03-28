package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util

@Suppress("MemberVisibilityCanBePrivate")
public class HitMark(
    public var sourceIndex: Int,
    public var selfType: UShort,
    public var otherType: UShort,
    public var value: UShort,
    public var selfSoakType: UShort,
    public var otherSoakType: UShort,
    public var soakValue: UShort,
    public var delay: UShort,
) {
    public constructor(
        sourceIndex: Int,
        selfType: UShort,
        otherType: UShort,
        value: UShort,
        delay: UShort,
    ) : this(
        sourceIndex = sourceIndex,
        selfType = selfType,
        otherType = otherType,
        value = value,
        selfSoakType = UShort.MAX_VALUE,
        otherSoakType = UShort.MAX_VALUE,
        soakValue = UShort.MAX_VALUE,
        delay = delay,
    )

    public constructor(
        selfType: UShort,
        delay: UShort,
    ) : this(
        sourceIndex = 0,
        selfType = selfType,
        otherType = selfType,
        value = UShort.MAX_VALUE,
        selfSoakType = UShort.MAX_VALUE,
        otherSoakType = UShort.MAX_VALUE,
        soakValue = UShort.MAX_VALUE,
        delay = delay,
    )
}
