package net.rsprot.protocol.metrics.channel.impl

import net.rsprot.protocol.metrics.channel.ChannelTrafficMonitor

/**
 * A wrapper for login channel traffic monitor.
 * The wrapper class allows us to write extension functions specifically targeted
 * at the login-implementation.
 * @property channelTrafficMonitor the underlying traffic monitor to which calls are delegated.
 */
public class LoginChannelTrafficMonitor(
    private val channelTrafficMonitor: ChannelTrafficMonitor,
) : ChannelTrafficMonitor by channelTrafficMonitor
