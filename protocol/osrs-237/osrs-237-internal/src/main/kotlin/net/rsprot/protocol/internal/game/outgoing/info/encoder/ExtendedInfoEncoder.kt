package net.rsprot.protocol.internal.game.outgoing.info.encoder

import net.rsprot.protocol.internal.game.outgoing.info.ExtendedInfo

/**
 * Extended info encoders are responsible for turning [T] into a byte buffer.
 */
public interface ExtendedInfoEncoder<in T : ExtendedInfo<T, *>>
