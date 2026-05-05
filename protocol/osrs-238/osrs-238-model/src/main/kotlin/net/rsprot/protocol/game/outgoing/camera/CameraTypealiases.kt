@file:Suppress("ktlint:standard:filename")

package net.rsprot.protocol.game.outgoing.camera

@Deprecated(
    message = "Deprecated. Use CamTargetV3.",
    replaceWith = ReplaceWith("CamTargetV3"),
)
public typealias CamTarget = CamTargetV3

@Deprecated(
    message = "Deprecated. Use CamTargetV3.",
    replaceWith = ReplaceWith("CamTargetV3"),
)
public typealias CamTargetV2 = CamTargetV3

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
