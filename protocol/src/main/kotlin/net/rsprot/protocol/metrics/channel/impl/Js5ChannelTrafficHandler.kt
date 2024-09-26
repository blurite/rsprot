package net.rsprot.protocol.metrics.channel.impl

import net.rsprot.protocol.metrics.channel.ChannelTrafficHandler

public class Js5ChannelTrafficHandler(
    private val channelTrafficHandler: ChannelTrafficHandler,
) : ChannelTrafficHandler by channelTrafficHandler
