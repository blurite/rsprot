package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

/**
 * The tinting class is used to apply a specific tint onto the non-textured parts of
 * an avatar. As the engine does not support modifying textures this way, they remain
 * in their original form.
 */
public class Tinting {
    /**
     * The delay in client cycles (20ms/cc) until the tinting is applied.
     */
    public var start: UShort = 0u

    /**
     * The timestamp in client cycles (20ms/cc) until the tinting finishes.
     */
    public var end: UShort = 0u

    /**
     * The hue of the tint.
     */
    public var hue: UByte = 0u

    /**
     * The saturation of the tint.
     */
    public var saturation: UByte = 0u

    /**
     * The lightness of the tint.
     */
    public var lightness: UByte = 0u

    /**
     * The weight (or opacity) of the tint.
     */
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
