# netty-haproxy-support

_Support for the [HAProxy](https://en.wikipedia.org/wiki/HAProxy) protocol,
to resolve "real" IP addresses behind a proxy_

### Installing to your Netty `ServerBootstrap`

Simply call `HAProxy.childHandlerProxied` instead of `childHandler` on your
`ServerBootstrap`, e.g.:

```kotlin
serverBootstrap.childHandlerProxied(yourChannelInitializer)
```

Or like this in Java:

```java
HAProxy.childHandlerProxied(serverBootstrap, yourChannelInitializer);
```

### Getting the real address of a proxied channel

When you want to get the real address of a channel, use
`HAProxyAttributes.sourceAddress` instead of
`channel.remoteAddress()`, e.g.:

```kotlin
val realAddress: SocketAddress = channel.sourceAddress
```

Or like this in Java:

```java
SocketAddress realAddress = HAProxyAttributes.getSourceAddress(channel);
```

There are also other variations, like `channel.sourceHost` which is a `String`
of the IP address rather than a
`SocketAddress`:

```kotlin
val realHost: String = channel.sourceHost
```

You can also access all available attributes via `channel.haproxyAttribute`.

Also, all of the `HAProxyAttributes` are available as extension properties on
`ChannelHandlerContext` as well, e.g.
`ctx.sourceAddress` or `ctx.sourceHost`.
