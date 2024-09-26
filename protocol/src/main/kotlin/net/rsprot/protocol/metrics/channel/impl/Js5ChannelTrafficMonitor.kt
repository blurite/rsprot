package net.rsprot.protocol.metrics.channel.impl

import net.rsprot.protocol.metrics.channel.ChannelTrafficMonitor

/**
 * A wrapper for JS5 channel traffic monitor.
 * The wrapper class allows us to write extension functions specifically targeted
 * at the JS5-implementation.
 * @property channelTrafficMonitor the underlying traffic monitor to which calls are delegated.
 */
public class Js5ChannelTrafficMonitor(
    private val channelTrafficMonitor: ChannelTrafficMonitor,
) : ChannelTrafficMonitor by channelTrafficMonitor
