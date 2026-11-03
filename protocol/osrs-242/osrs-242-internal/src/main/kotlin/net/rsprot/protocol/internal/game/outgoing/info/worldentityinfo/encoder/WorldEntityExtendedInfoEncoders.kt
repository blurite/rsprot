package net.rsprot.protocol.internal.game.outgoing.info.worldentityinfo.encoder

import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.Sequence
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.VisibleOps

/**
 * A data class to bring all the extended info encoders for a given client together.
 * @param oldSchoolClientType the client for which these encoders are created.
 * @param visibleOps the enabled/visible ops when right-clicking a world entity
 */
public data class WorldEntityExtendedInfoEncoders(
    public val oldSchoolClientType: OldSchoolClientType,
    public val sequence: PrecomputedExtendedInfoEncoder<Sequence>,
    public val visibleOps: PrecomputedExtendedInfoEncoder<VisibleOps>,
)
