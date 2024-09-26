package net.rsprot.protocol.metrics.channel.impl

import net.rsprot.protocol.metrics.channel.ChannelTrafficHandler

public class GameChannelTrafficHandler(
    private val channelTrafficHandler: ChannelTrafficHandler,
) : ChannelTrafficHandler by channelTrafficHandler
