package net.rsprot.protocol.metrics.channel.snapshots

import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.metrics.channel.snapshots.util.PacketSnapshot

public class InetAddressSnapshot<CP, SP, DC>(
    public val disconnectionsByReason: Map<DC, Int>,
    public val incomingPackets: Map<CP, PacketSnapshot>,
    public val outgoingPackets: Map<SP, PacketSnapshot>,
) where CP : ClientProt, CP : Enum<CP>, SP : ServerProt, SP : Enum<SP>, DC : Enum<DC>
