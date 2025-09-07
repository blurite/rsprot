package org.jire.netty.haproxy

import io.netty.channel.ChannelInboundHandler

public interface HAProxyParentHandler {
    public val childHandler: ChannelInboundHandler
}
