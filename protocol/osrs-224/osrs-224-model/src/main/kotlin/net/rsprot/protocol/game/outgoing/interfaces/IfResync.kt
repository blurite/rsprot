package net.rsprot.protocol.game.outgoing.interfaces

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprot.protocol.message.toIntOrMinusOne
import net.rsprot.protocol.util.CombinedId

/**
 * If resync is used to synchronize opened interfaces with the client.
 * @property topLevelInterface the top-level interface being opened
 * @property subInterfaces the sub interfaces being opened in this batch
 * @property events the interface events being set in this batch
 */
@Suppress("MemberVisibilityCanBePrivate")
public class IfResync private constructor(
    private val _topLevelInterface: UShort,
    public val subInterfaces: List<SubInterfaceMessage>,
    public val events: List<InterfaceEventsMessage>,
) : OutgoingGameMessage {
    public constructor(
        topLevelInterface: Int,
        subInterfaces: List<SubInterfaceMessage>,
        events: List<InterfaceEventsMessage>,
    ) : this(
        topLevelInterface.toUShort(),
        subInterfaces,
        events,
    )

    public val topLevelInterface: Int
        get() = _topLevelInterface.toIntOrMinusOne()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfResync

        if (_topLevelInterface != other._topLevelInterface) return false
        if (subInterfaces != other.subInterfaces) return false
        if (events != other.events) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _topLevelInterface.hashCode()
        result = 31 * result + subInterfaces.hashCode()
        result = 31 * result + events.hashCode()
        return result
    }

    override fun toString(): String =
        "IfResync(" +
            "topLevelInterface=$topLevelInterface, " +
            "subInterfaces=$subInterfaces, " +
            "events=$events" +
            ")"

    /**
     * Sub interface holds state about a sub interface to be opened.
     * @property destinationCombinedId the bitpacked combination of [destinationInterfaceId] and [destinationComponentId].
     * @property destinationInterfaceId the destination interface on which the sub
     * interface is being opened
     * @property destinationComponentId the component on the destination interface
     * on which the sub interface is being opened
     * @property interfaceId the sub interface id
     * @property type the type of the interface to be opened as (modal, overlay, client)
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public class SubInterfaceMessage private constructor(
        public val destinationCombinedId: Int,
        private val _interfaceId: UShort,
        private val _type: UByte,
    ) {
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

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as SubInterfaceMessage

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
            "SubInterfaceMessage(" +
                "destinationInterfaceId=$destinationInterfaceId, " +
                "destinationComponentId=$destinationComponentId, " +
                "interfaceId=$interfaceId, " +
                "type=$type" +
                ")"
    }

    /**
     * Interface events compress the IF_SETEVENTS packet's payload
     * into a helper class.
     * @property interfaceId the interface id on which to set the events
     * @property componentId the component on that interface to set the events on
     * @property start the start subcomponent id
     * @property end the end subcomponent id (inclusive)
     * @property events the bitpacked events
     */
    public class InterfaceEventsMessage private constructor(
        public val combinedId: Int,
        private val _start: UShort,
        private val _end: UShort,
        public val events: Int,
    ) {
        public constructor(
            interfaceId: Int,
            componentId: Int,
            start: Int,
            end: Int,
            events: Int,
        ) : this(
            CombinedId(interfaceId, componentId).combinedId,
            start.toUShort(),
            end.toUShort(),
            events,
        )

        public constructor(
            combinedId: Int,
            start: Int,
            end: Int,
            events: Int,
        ) : this(
            combinedId,
            start.toUShort(),
            end.toUShort(),
            events,
        )

        private val _combinedId: CombinedId
            get() = CombinedId(combinedId)
        public val interfaceId: Int
            get() = _combinedId.interfaceId
        public val componentId: Int
            get() = _combinedId.componentId
        public val start: Int
            get() = _start.toInt()
        public val end: Int
            get() = _end.toInt()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as InterfaceEventsMessage

            if (combinedId != other.combinedId) return false
            if (_start != other._start) return false
            if (_end != other._end) return false
            if (events != other.events) return false

            return true
        }

        override fun hashCode(): Int {
            var result = combinedId.hashCode()
            result = 31 * result + _start.hashCode()
            result = 31 * result + _end.hashCode()
            result = 31 * result + events
            return result
        }

        override fun toString(): String =
            "InterfaceEvents(" +
                "interfaceId=$interfaceId, " +
                "componentId=$componentId, " +
                "start=$start, " +
                "end=$end, " +
                "events=$events" +
                ")"
    }
}
