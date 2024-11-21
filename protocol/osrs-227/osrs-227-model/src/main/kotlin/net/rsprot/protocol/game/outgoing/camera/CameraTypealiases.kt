@file:Suppress("DEPRECATION")

package net.rsprot.protocol.game.outgoing.camera

@Deprecated(
    message = "Deprecated. Use CamTargetV2.",
    replaceWith = ReplaceWith("CamTargetV2"),
)
public typealias CamTarget = CamTargetV2

@Deprecated(
    message = "Deprecated. Use CamTargetV1.",
    replaceWith = ReplaceWith("CamTargetV1"),
)
public typealias CamTargetOld = CamTargetV1
