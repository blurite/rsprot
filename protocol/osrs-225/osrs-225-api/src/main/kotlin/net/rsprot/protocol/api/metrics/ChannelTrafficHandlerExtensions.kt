package net.rsprot.protocol.api.metrics

import net.rsprot.protocol.api.game.GameDisconnectionReason
import net.rsprot.protocol.api.js5.Js5DisconnectionReason
import net.rsprot.protocol.api.login.LoginDisconnectionReason
import net.rsprot.protocol.metrics.channel.impl.GameChannelTrafficHandler
import net.rsprot.protocol.metrics.channel.impl.Js5ChannelTrafficHandler
import net.rsprot.protocol.metrics.channel.impl.LoginChannelTrafficHandler
import java.net.InetAddress

internal fun LoginChannelTrafficHandler.addDisconnectionReason(
    inetAddress: InetAddress,
    reason: LoginDisconnectionReason,
) {
    addDisconnectionReason(
        inetAddress,
        reason.ordinal,
    )
}

internal fun Js5ChannelTrafficHandler.addDisconnectionReason(
    inetAddress: InetAddress,
    reason: Js5DisconnectionReason,
) {
    addDisconnectionReason(
        inetAddress,
        reason.ordinal,
    )
}

internal fun GameChannelTrafficHandler.addDisconnectionReason(
    inetAddress: InetAddress,
    reason: GameDisconnectionReason,
) {
    addDisconnectionReason(
        inetAddress,
        reason.ordinal,
    )
}
