package net.rsprot.protocol.internal.game.outgoing.info

import net.rsprot.protocol.internal.game.outgoing.info.encoder.ExtendedInfoEncoder

public abstract class CachedExtendedInfo<in T : ExtendedInfo<T, E>, E : ExtendedInfoEncoder<T>>(
    encoders: Array<E?>,
) : ExtendedInfo<T, E>(encoders)
