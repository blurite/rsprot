package net.rsprot.protocol.game.outgoing.interfaces

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprot.protocol.message.toIntOrMinusOne
import net.rsprot.protocol.util.CombinedId

/**
 * If open-sub messages are used to open non-root interfaces
 * on root interfaces.
 *
 * Interface types:
 * ```
 * | Id |   Name  | Is modal |
 * |:--:|:-------:|:--------:|
 * |  0 |  Modal  |    Yes   |
 * |  1 | Overlay |    No    |
 * |  3 |  Client |    Yes   |
 * ```
 *
 * Note: Client type is supported by the client, but is not actually in use by anything!
 *
 * @property destinationCombinedId the bitpacked combination of [destinationInterfaceId] and [destinationComponentId].
 * @property destinationInterfaceId the destination interface on which the sub
 * interface is being opened
 * @property destinationComponentId the component on the destination interface
 * on which the sub interface is being opened
 * @property interfaceId the sub interface id
 * @property type the type of the interface to be opened as (modal, overlay, client)
 */
@Suppress("MemberVisibilityCanBePrivate")
public class IfOpenSub private constructor(
    public val destinationCombinedId: Int,
    private val _interfaceId: UShort,
    private val _type: UByte,
) : OutgoingGameMessage {
    public constructor(
        destinationInterfaceId: Int,
        destinationComponentId: Int,
        interfaceId: Int,
        type: Int,
    ) : this(
        CombinedId(destinationInterfaceId, destinationComponentId).combinedId,
        interfaceId.toUShort(),
        type.toUByte(),
    )

    public constructor(
        destinationCombinedId: Int,
        interfaceId: Int,
        type: Int,
    ) : this(
        destinationCombinedId,
        interfaceId.toUShort(),
        type.toUByte(),
    )

    private val _destinationCombinedId: CombinedId
        get() = CombinedId(destinationCombinedId)
    public val destinationInterfaceId: Int
        get() = _destinationCombinedId.interfaceId
    public val destinationComponentId: Int
        get() = _destinationCombinedId.componentId
    public val interfaceId: Int
        get() = _interfaceId.toIntOrMinusOne()
    public val type: Int
        get() = _type.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfOpenSub

        if (destinationCombinedId != other.destinationCombinedId) return false
        if (_interfaceId != other._interfaceId) return false
        if (_type != other._type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = destinationCombinedId.hashCode()
        result = 31 * result + _interfaceId.hashCode()
        result = 31 * result + _type.hashCode()
        return result
    }

    override fun toString(): String =
        "IfOpenSub(" +
            "destinationInterfaceId=$destinationInterfaceId, " +
            "destinationComponentId=$destinationComponentId, " +
            "interfaceId=$interfaceId, " +
            "type=$type" +
            ")"
}
