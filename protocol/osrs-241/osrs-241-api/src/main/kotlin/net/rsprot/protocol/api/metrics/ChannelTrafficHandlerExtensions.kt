package net.rsprot.protocol.api.metrics

import net.rsprot.protocol.api.game.GameDisconnectionReason
import net.rsprot.protocol.api.js5.Js5DisconnectionReason
import net.rsprot.protocol.api.login.LoginDisconnectionReason
import net.rsprot.protocol.metrics.channel.impl.GameChannelTrafficMonitor
import net.rsprot.protocol.metrics.channel.impl.Js5ChannelTrafficMonitor
import net.rsprot.protocol.metrics.channel.impl.LoginChannelTrafficMonitor

internal fun LoginChannelTrafficMonitor.addDisconnectionReason(
    hostAddress: String,
    reason: LoginDisconnectionReason,
) {
    addDisconnectionReason(
        hostAddress,
        reason.ordinal,
    )
}

internal fun Js5ChannelTrafficMonitor.addDisconnectionReason(
    hostAddress: String,
    reason: Js5DisconnectionReason,
) {
    addDisconnectionReason(
        hostAddress,
        reason.ordinal,
    )
}

internal fun GameChannelTrafficMonitor.addDisconnectionReason(
    hostAddress: String,
    reason: GameDisconnectionReason,
) {
    addDisconnectionReason(
        hostAddress,
        reason.ordinal,
    )
}
