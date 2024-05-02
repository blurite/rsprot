package net.rsprot.protocol.api

import java.net.InetAddress

public interface InetAddressTracker {
    public fun register(address: InetAddress)

    public fun deregister(address: InetAddress)

    public fun getCount(address: InetAddress): Int
}
