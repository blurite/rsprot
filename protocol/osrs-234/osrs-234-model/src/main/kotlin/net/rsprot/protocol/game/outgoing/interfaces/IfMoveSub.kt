package net.rsprot.protocol.game.outgoing.interfaces

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprot.protocol.util.CombinedId

/**
 * If move-sub messages are used to move a sub-level interface from
 * one position to another, typically when changing top-level interfaces.
 * @property sourceCombinedId the bitpacked combination of [sourceInterfaceId] and [sourceComponentId].
 * @property sourceInterfaceId the current interface on which the interface that's
 * being moved is opened on
 * @property sourceComponentId the current component of the [sourceInterfaceId] on which
 * the interface that's being moved is opened on
 * @property destinationCombinedId the bitpacked combination of [destinationInterfaceId] and [destinationComponentId].
 * @property destinationInterfaceId the destination interface on which the sub-interface
 * should be opened
 * @property destinationComponentId the component id on the [destinationInterfaceId] on
 * which the sub-interface should be opened
 */
@Suppress("MemberVisibilityCanBePrivate")
public class IfMoveSub(
    public val sourceCombinedId: Int,
    public val destinationCombinedId: Int,
) : OutgoingGameMessage {
    public constructor(
        sourceInterfaceId: Int,
        sourceComponentId: Int,
        destinationInterfaceId: Int,
        destinationComponentId: Int,
    ) : this(
        CombinedId(sourceInterfaceId, sourceComponentId).combinedId,
        CombinedId(destinationInterfaceId, destinationComponentId).combinedId,
    )

    private val _sourceCombinedId: CombinedId
        get() = CombinedId(sourceCombinedId)
    public val sourceInterfaceId: Int
        get() = _sourceCombinedId.interfaceId
    public val sourceComponentId: Int
        get() = _sourceCombinedId.componentId

    private val _destinationCombinedId: CombinedId
        get() = CombinedId(destinationCombinedId)
    public val destinationInterfaceId: Int
        get() = _destinationCombinedId.interfaceId
    public val destinationComponentId: Int
        get() = _destinationCombinedId.componentId
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfMoveSub

        if (sourceCombinedId != other.sourceCombinedId) return false
        if (destinationCombinedId != other.destinationCombinedId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sourceCombinedId.hashCode()
        result = 31 * result + destinationCombinedId.hashCode()
        return result
    }

    override fun toString(): String =
        "IfMoveSub(" +
            "sourceInterfaceId=$sourceInterfaceId, " +
            "sourceComponentId=$sourceComponentId, " +
            "destinationInterfaceId=$destinationInterfaceId, " +
            "destinationComponentId=$destinationComponentId" +
            ")"
}
