package net.rsprot.protocol.api.repositories

import io.netty.channel.ChannelHandlerContext
import net.rsprot.protocol.message.codec.incoming.GameMessageConsumerRepository

public class MessageConsumerRepositories<R>(
    public val loginMessageConsumerRepository: GameMessageConsumerRepository<ChannelHandlerContext>,
    public val js5MessageConsumerRepository: GameMessageConsumerRepository<ChannelHandlerContext>,
    public val gameMessageConsumerRepository: GameMessageConsumerRepository<R>,
)
