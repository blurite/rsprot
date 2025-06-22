@file:Suppress("ktlint:standard:filename")

package net.rsprot.protocol.game.outgoing.interfaces

@Deprecated(
    message = "Deprecated. Use IfResyncV1.",
    replaceWith = ReplaceWith("IfResyncV1"),
)
public typealias IfResync = IfResyncV1

@Deprecated(
    message = "Deprecated. Use IfSetEventsV1.",
    replaceWith = ReplaceWith("IfSetEventsV1"),
)
public typealias IfSetEvents = IfSetEventsV1
