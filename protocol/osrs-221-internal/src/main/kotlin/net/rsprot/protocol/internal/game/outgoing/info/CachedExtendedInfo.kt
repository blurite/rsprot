package net.rsprot.protocol.internal.game.outgoing.info

import net.rsprot.protocol.internal.game.outgoing.info.encoder.ExtendedInfoEncoder

/**
 * Extended info blocks which get cached by the client, meaning if
 * an avatar goes from low resolution to high resolution, and the client has a
 * cached buffer of them, unless the server writes a new variant (in the case of a
 * de-synchronization), the client will use the old buffer to restore that block).
 * @param T the extended info block
 * @param E the encoder for that extended info block
 * @param encoders the array of platform-specific encoders of type [E].
 */
public abstract class CachedExtendedInfo<in T : ExtendedInfo<T, E>, E : ExtendedInfoEncoder<T>>(
    encoders: Array<E?>,
) : ExtendedInfo<T, E>(encoders)
