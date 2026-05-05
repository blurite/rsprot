package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util

/**
 * A class for holding all the state of a given hitmark.
 * @param sourceIndex the index of the character that dealt the hit.
 * If the target avatar is a player, add 0x10000 to the real index value (0-2048).
 * If the target avatar is a NPC, set the index as it is.
 * If there is no source, set the index to -1.
 * The index will be used for tinting purposes, as both the player who dealt
 * the hit, and the recipient will see a tinted variant.
 * Everyone else, however, will see a regular darkened hit mark.
 * @param sourceType the multi hitmark id that supports tinted and darkened variants, shown to the player
 * with the index of [sourceIndex].
 * @param selfType the multi hitmark id that supports tinted and darkened variants, shown to the player
 * that receives the hit.
 * @param otherType the hitmark id to render to anyone that isn't the recipient,
 * or the one who dealt the hit. This will generally be a darkened variant.
 * If the hitmark should only render to the local player, set the [otherType]
 * value to -1, forcing it to only render to the recipient (and in the case of
 * a [sourceIndex] being defined, the one who dealt the hit)
 * @param value the value to show over the hitmark.
 * @param delay the delay in client cycles (20ms/cc) until the hitmark renders.
 * @param limit the maximum number of hitmarks that may render at the time of posting this hitmark.
 */
public class HitMark(
    public var sourceIndex: Int,
    public var sourceType: UShort,
    public var selfType: UShort,
    public var otherType: UShort,
    public var value: UShort,
    public var delay: UShort,
    public var limit: UByte,
) {
    public constructor(
        selfType: UShort,
        delay: UShort,
    ) : this(
        sourceIndex = -1,
        sourceType = selfType,
        selfType = selfType,
        otherType = selfType,
        value = UShort.MAX_VALUE,
        delay = delay,
        limit = 4.toUByte(),
    )
}
