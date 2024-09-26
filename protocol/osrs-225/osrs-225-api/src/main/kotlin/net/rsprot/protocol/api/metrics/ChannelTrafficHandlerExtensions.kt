package net.rsprot.protocol.api.metrics

import net.rsprot.protocol.api.game.GameDisconnectionReason
import net.rsprot.protocol.api.js5.Js5DisconnectionReason
import net.rsprot.protocol.api.login.LoginDisconnectionReason
import net.rsprot.protocol.metrics.channel.impl.GameChannelTrafficMonitor
import net.rsprot.protocol.metrics.channel.impl.Js5ChannelTrafficMonitor
import net.rsprot.protocol.metrics.channel.impl.LoginChannelTrafficMonitor
import java.net.InetAddress

internal fun LoginChannelTrafficMonitor.addDisconnectionReason(
    inetAddress: InetAddress,
    reason: LoginDisconnectionReason,
) {
    addDisconnectionReason(
        inetAddress,
        reason.ordinal,
    )
}

internal fun Js5ChannelTrafficMonitor.addDisconnectionReason(
    inetAddress: InetAddress,
    reason: Js5DisconnectionReason,
) {
    addDisconnectionReason(
        inetAddress,
        reason.ordinal,
    )
}

internal fun GameChannelTrafficMonitor.addDisconnectionReason(
    inetAddress: InetAddress,
    reason: GameDisconnectionReason,
) {
    addDisconnectionReason(
        inetAddress,
        reason.ordinal,
    )
}
