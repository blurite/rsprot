package net.rsprot.protocol.game.outgoing.interfaces

import net.rsprot.protocol.message.OutgoingMessage
import net.rsprot.protocol.util.CombinedId

/**
 * Interface events are sent to set/unlock various options on a component,
 * such as button clicks and dragging.
 * @property interfaceId the interface id on which to set the events
 * @property componentId the component on that interface to set the events on
 * @property start the start subcomponent id
 * @property end the end subcomponent id (inclusive)
 * @property events the bitpacked events
 */
@Suppress("MemberVisibilityCanBePrivate", "DuplicatedCode")
public class IfSetEvents private constructor(
    public val combinedId: CombinedId,
    private val _start: UShort,
    private val _end: UShort,
    public val events: Int,
) : OutgoingMessage {
    public constructor(
        interfaceId: Int,
        componentId: Int,
        start: Int,
        end: Int,
        events: Int,
    ) : this(
        CombinedId(interfaceId, componentId),
        start.toUShort(),
        end.toUShort(),
        events,
    )

    public val interfaceId: Int
        get() = combinedId.interfaceId
    public val componentId: Int
        get() = combinedId.componentId
    public val start: Int
        get() = _start.toInt()
    public val end: Int
        get() = _end.toInt()
}
