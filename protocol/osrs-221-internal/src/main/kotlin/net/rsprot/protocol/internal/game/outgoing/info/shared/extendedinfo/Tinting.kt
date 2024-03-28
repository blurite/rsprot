package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

public class Tinting {
    public var start: UShort = 0u
    public var end: UShort = 0u
    public var hue: UByte = 0u
    public var saturation: UByte = 0u
    public var lightness: UByte = 0u
    public var weight: UByte = 0u

    public fun reset() {
        start = 0u
        end = 0u
        hue = 0u
        saturation = 0u
        lightness = 0u
        weight = 0u
    }
}
