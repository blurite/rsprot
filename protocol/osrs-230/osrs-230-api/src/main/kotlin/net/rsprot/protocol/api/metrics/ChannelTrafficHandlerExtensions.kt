package net.rsprot.protocol.api.metrics

import net.rsprot.protocol.api.game.GameDisconnectionReason
import net.rsprot.protocol.api.js5.Js5DisconnectionReason
import net.rsprot.protocol.api.login.LoginDisconnectionReason
import net.rsprot.protocol.metrics.channel.impl.GameChannelTrafficMonitor
import net.rsprot.protocol.metrics.channel.impl.Js5ChannelTrafficMonitor
import net.rsprot.protocol.metrics.channel.impl.LoginChannelTrafficMonitor
import java.net.SocketAddress

internal fun LoginChannelTrafficMonitor.addDisconnectionReason(
    socketAddress: SocketAddress,
    reason: LoginDisconnectionReason,
) {
    addDisconnectionReason(
        socketAddress,
        reason.ordinal,
    )
}

internal fun Js5ChannelTrafficMonitor.addDisconnectionReason(
    socketAddress: SocketAddress,
    reason: Js5DisconnectionReason,
) {
    addDisconnectionReason(
        socketAddress,
        reason.ordinal,
    )
}

internal fun GameChannelTrafficMonitor.addDisconnectionReason(
    socketAddress: SocketAddress,
    reason: GameDisconnectionReason,
) {
    addDisconnectionReason(
        socketAddress,
        reason.ordinal,
    )
}
