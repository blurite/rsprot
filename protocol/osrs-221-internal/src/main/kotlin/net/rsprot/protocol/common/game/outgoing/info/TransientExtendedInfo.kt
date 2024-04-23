package net.rsprot.protocol.common.game.outgoing.info

import net.rsprot.protocol.common.game.outgoing.info.encoder.ExtendedInfoEncoder

/**
 * Transient extended info is for extended info blocks which do not use any
 * caching mechanisms client-sided. This does not, however, mean that no caching occurs
 * at all. Certain extended info blocks are intended to last over several game cycles,
 * such as [net.rsprot.protocol.common.game.outgoing.info.shared.extendedinfo.FacePathingEntity],
 * In those cases, this structure is responsible for holding the state necessary to synchronize
 * the avatars later on in the future. If the respective extended info block is then reset to
 * the default value, new avatars should not receive this any more, as the client will also
 * use the default value.
 * @param T the extended info block
 * @param E the encoder for the given extended info block
 */
public abstract class TransientExtendedInfo<in T : ExtendedInfo<T, E>, E : ExtendedInfoEncoder<T>> :
    ExtendedInfo<T, E>()
