package net.rsprot.protocol.api

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.handler.timeout.IdleStateHandler
import net.rsprot.protocol.api.login.LoginChannelHandler
import net.rsprot.protocol.api.login.LoginMessageDecoder
import net.rsprot.protocol.api.login.LoginMessageEncoder
import java.util.concurrent.TimeUnit

public class LoginChannelInitializer<R>(
    private val networkService: NetworkService<R, *>,
) : ChannelInitializer<Channel>() {
    override fun initChannel(ch: Channel) {
        logger.debug {
            "Channel initialized: $ch"
        }
        ch.pipeline().addLast(
            IdleStateHandler(
                true,
                NetworkService.LOGIN_TIMEOUT_SECONDS,
                NetworkService.LOGIN_TIMEOUT_SECONDS,
                NetworkService.LOGIN_TIMEOUT_SECONDS,
                TimeUnit.SECONDS,
            ),
            LoginMessageDecoder(networkService),
            LoginMessageEncoder(networkService),
            LoginChannelHandler(networkService),
        )
    }

    private companion object {
        private val logger: InlineLogger = InlineLogger()
    }
}
