package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

public class Tinting {
    public var startTime: UShort = 0u
    public var endTime: UShort = 0u
    public var hue: UByte = 0u
    public var saturation: UByte = 0u
    public var luminance: UByte = 0u
    public var opacity: UByte = 0u

    public fun reset() {
        startTime = 0u
        endTime = 0u
        hue = 0u
        saturation = 0u
        luminance = 0u
        opacity = 0u
    }
}
