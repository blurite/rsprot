package net.rsprot.protocol.metrics.channel.impl

import net.rsprot.protocol.metrics.channel.ChannelTrafficHandler

public class LoginChannelTrafficHandler(
    private val channelTrafficHandler: ChannelTrafficHandler,
) : ChannelTrafficHandler by channelTrafficHandler
