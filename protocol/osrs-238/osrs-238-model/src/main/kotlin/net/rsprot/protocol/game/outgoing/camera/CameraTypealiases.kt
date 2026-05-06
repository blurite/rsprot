@file:Suppress("ktlint:standard:filename")

package net.rsprot.protocol.game.outgoing.camera

@Deprecated(
    message = "Deprecated. Use CamTargetV4.",
    replaceWith = ReplaceWith("CamTargetV4"),
)
public typealias CamTarget = CamTargetV4

@Deprecated(
    message = "Deprecated. Use CamTargetV4.",
    replaceWith = ReplaceWith("CamTargetV4"),
)
public typealias CamTargetV2 = CamTargetV4

@Deprecated(
    message = "Deprecated. Use CamMoveToV2.",
    replaceWith = ReplaceWith("CamMoveToV2"),
)
public typealias CamMoveTo = CamMoveToV1

@Deprecated(
    message = "Deprecated. Use CamLookAtV2.",
    replaceWith = ReplaceWith("CamLookAtV2"),
)
public typealias CamLookAt = CamLookAtV1

@Deprecated(
    message = "Deprecated. Use CamMoveToCyclesV2.",
    replaceWith = ReplaceWith("CamMoveToCyclesV2"),
)
public typealias CamMoveToCycles = CamMoveToCyclesV1

@Deprecated(
    message = "Deprecated. Use CamLookAtEasedCoordV2.",
    replaceWith = ReplaceWith("CamLookAtEasedCoordV2"),
)
public typealias CamLookAtEasedCoord = CamLookAtEasedCoordV1

@Deprecated(
    message = "Deprecated. Use CamMoveToArcV2.",
    replaceWith = ReplaceWith("CamMoveToArcV2"),
)
public typealias CamMoveToArc = CamMoveToArcV1
