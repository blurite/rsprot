package org.jire.netty.haproxy

import io.netty.handler.codec.haproxy.HAProxyMessageDecoder

/**
 * Constants for handler names used in the HAProxy pipeline.
 */
public object HAProxyHandlerNames {
    public val HAPROXY_CHANNEL_INITIALIZER_NAME: String =
        HAProxyChannelInitializer::class.qualifiedName!!

    public val HAPROXY_IDLE_STATE_HANDLER_NAME: String =
        HAProxyIdleStateHandler::class.qualifiedName!!

    public val HAPROXY_DETECTION_HANDLER_NAME: String =
        HAProxyDetectionHandler::class.qualifiedName!!

    public val HAPROXY_MESSAGE_DECODER_HANDLER_NAME: String =
        HAProxyMessageDecoder::class.qualifiedName!!
    public val HAPROXY_MESSAGE_HANDLER_NAME: String =
        HAProxyMessageHandler::class.qualifiedName!!
}
