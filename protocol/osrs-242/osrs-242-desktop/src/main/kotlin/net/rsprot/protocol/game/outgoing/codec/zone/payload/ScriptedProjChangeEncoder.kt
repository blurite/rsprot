package net.rsprot.protocol.game.outgoing.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.zone.payload.ScriptedProjChange
import net.rsprot.protocol.internal.game.outgoing.codec.zone.payload.ZoneProtEncoder

public class ScriptedProjChangeEncoder : ZoneProtEncoder<ScriptedProjChange> {
    override val prot: ServerProt = GameServerProt.SCRIPTEDPROJ_CHANGE

    override fun encode(
        buffer: JagByteBuf,
        message: ScriptedProjChange,
    ) {
        buffer.p3Alt3(message.targetIndex)
        buffer.p1(if (message.deleteOnFreezeEnd) 1 else 0)
        buffer.p4Alt3(message.targetCoord.packed)
        buffer.p2Alt1(message.targetOffsetX)
        buffer.p2Alt2(message.targetOffsetZ)
        buffer.p2Alt1(message.freezeDuration)
        buffer.p2(message.targetHeight)
        buffer.p2Alt3(message.slot)
    }
}
