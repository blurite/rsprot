package net.rsprot.protocol.game.outgoing.interfaces

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprot.protocol.util.CombinedId

/**
 * If set-player-model basecolour packet is used to set the ident kit colour
 * of a customized player model on an interface. This allows one to build
 * a completely unique player model up without using anyone as reference.
 * The colouring logic is identical to that found within Appearance for players.
 * @property combinedId the bitpacked combination of [interfaceId] and [componentId].
 * @property interfaceId the id of the interface on which the model resides
 * @property componentId the id of the component on which the model resides
 * @property index the index of the colour, ranging from 0 to 4 (inclusive)
 * @property colour the value of the colour, ranging from 0 to 255 (inclusive)
 */
public class IfSetPlayerModelBaseColour private constructor(
    public val combinedId: Int,
    private val _index: UByte,
    private val _colour: UByte,
) : OutgoingGameMessage {
    public constructor(
        interfaceId: Int,
        componentId: Int,
        index: Int,
        colour: Int,
    ) : this(
        CombinedId(interfaceId, componentId).combinedId,
        index.toUByte(),
        colour.toUByte(),
    )

    public constructor(
        combinedId: Int,
        index: Int,
        colour: Int,
    ) : this(
        combinedId,
        index.toUByte(),
        colour.toUByte(),
    )

    private val _combinedId: CombinedId
        get() = CombinedId(combinedId)
    public val interfaceId: Int
        get() = _combinedId.interfaceId
    public val componentId: Int
        get() = _combinedId.componentId
    public val index: Int
        get() = _index.toInt()
    public val colour: Int
        get() = _colour.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfSetPlayerModelBaseColour

        if (combinedId != other.combinedId) return false
        if (_index != other._index) return false
        if (_colour != other._colour) return false

        return true
    }

    override fun hashCode(): Int {
        var result = combinedId.hashCode()
        result = 31 * result + _index.hashCode()
        result = 31 * result + _colour.hashCode()
        return result
    }

    override fun toString(): String =
        "IfSetPlayerModelBaseColour(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId, " +
            "index=$index, " +
            "colour=$colour" +
            ")"
}
