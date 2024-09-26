package net.rsprot.protocol.metrics.channel.snapshots

import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.metrics.channel.snapshots.util.PacketSnapshot

/**
 * The [java.net.InetAddress] snapshot is responsible for tracking any disconnections and
 * packets flowing both directions based on a specific [java.net.InetAddress].
 * @property disconnectionsByReason a map of disconnection reasons to the number of times that
 * specific disconnection reason was witnessed for this [java.net.InetAddress].
 * @property incomingPackets a map of client prots to packet snapshots.
 * @property outgoingPackets a map of server prots to packet snapshots.
 * @param CP the type for client prots
 * @param SP the type for server prots
 * @param DC the type for disconnection reasons.
 */
public class InetAddressSnapshot<CP, SP, DC>(
    public val disconnectionsByReason: Map<DC, Int>,
    public val incomingPackets: Map<CP, PacketSnapshot>,
    public val outgoingPackets: Map<SP, PacketSnapshot>,
) where CP : ClientProt, CP : Enum<CP>, SP : ServerProt, SP : Enum<SP>, DC : Enum<DC>
