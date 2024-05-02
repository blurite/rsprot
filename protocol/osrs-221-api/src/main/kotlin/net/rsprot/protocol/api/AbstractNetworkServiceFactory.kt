package net.rsprot.protocol.api

import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.PooledByteBufAllocator
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.crypto.rsa.RsaKeyPair
import net.rsprot.protocol.api.bootstrap.BootstrapFactory
import net.rsprot.protocol.api.handlers.ExceptionHandlers
import net.rsprot.protocol.api.handlers.GameMessageHandlers
import net.rsprot.protocol.api.handlers.INetAddressHandlers
import net.rsprot.protocol.api.handlers.LoginHandlers
import net.rsprot.protocol.api.js5.Js5GroupProvider
import net.rsprot.protocol.api.suppliers.NpcInfoSupplier
import net.rsprot.protocol.api.suppliers.PlayerInfoSupplier
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.message.codec.incoming.provider.GameMessageConsumerRepositoryProvider

public abstract class AbstractNetworkServiceFactory<R, T : Js5GroupProvider.Js5GroupType> {
    public open val allocator: ByteBufAllocator get() = PooledByteBufAllocator.DEFAULT
    public abstract val ports: List<Int>
    public abstract val supportedClientTypes: List<OldSchoolClientType>

    public abstract fun getBootstrapFactory(): BootstrapFactory

    public abstract fun getRsaKeyPair(): RsaKeyPair

    public abstract fun getHuffmanCodecProvider(): HuffmanCodecProvider

    public abstract fun getJs5GroupProvider(): Js5GroupProvider<T>

    public abstract fun getGameMessageConsumerRepositoryProvider(): GameMessageConsumerRepositoryProvider<R>

    public abstract fun getGameConnectionHandler(): GameConnectionHandler<R>

    public abstract fun getNpcInfoSupplier(): NpcInfoSupplier

    public open fun getPlayerInfoSupplier(): PlayerInfoSupplier {
        return PlayerInfoSupplier()
    }

    public abstract fun getExceptionHandlers(): ExceptionHandlers<R>

    public open fun getINetAddressHandlers(): INetAddressHandlers {
        return INetAddressHandlers()
    }

    public open fun getGameMessageHandlers(): GameMessageHandlers {
        return GameMessageHandlers()
    }

    public open fun getLoginHandlers(): LoginHandlers {
        return LoginHandlers()
    }

    public fun build(): NetworkService<R, T> {
        val allocator = this.allocator
        val ports = this.ports
        val supportedClientTypes = this.supportedClientTypes
        val huffman = getHuffmanCodecProvider()
        val entityInfoProtocols =
            EntityInfoProtocols.initialize(
                allocator,
                supportedClientTypes,
                huffman,
                getPlayerInfoSupplier(),
                getNpcInfoSupplier(),
            )
        return NetworkService(
            allocator,
            ports,
            getBootstrapFactory(),
            entityInfoProtocols,
            supportedClientTypes,
            getGameConnectionHandler(),
            getExceptionHandlers(),
            getINetAddressHandlers(),
            getGameMessageHandlers(),
            getLoginHandlers(),
            huffman,
            getGameMessageConsumerRepositoryProvider(),
            getRsaKeyPair(),
            getJs5GroupProvider(),
        )
    }
}
