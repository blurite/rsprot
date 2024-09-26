package net.rsprot.protocol.metrics.channel.impl

import net.rsprot.protocol.metrics.channel.ChannelTrafficHandler

/**
 * A wrapper for game channel traffic handler.
 * The wrapper class allows us to write extension functions specifically targeted
 * at the game-implementation.
 * @property channelTrafficHandler the underlying traffic handler to which calls are delegated.
 */
public class GameChannelTrafficHandler(
    private val channelTrafficHandler: ChannelTrafficHandler,
) : ChannelTrafficHandler by channelTrafficHandler
