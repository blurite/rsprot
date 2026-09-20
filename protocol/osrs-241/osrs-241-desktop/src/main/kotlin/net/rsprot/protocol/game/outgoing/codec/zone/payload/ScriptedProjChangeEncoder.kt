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
        buffer.p2Alt3(message.targetHeight)
        buffer.p3Alt3(message.targetIndex)
        buffer.p2(message.targetOffsetX)
        buffer.p1Alt2(if (message.deleteOnFreezeEnd) 1 else 0)
        buffer.p2Alt1(message.slot)
        buffer.p2Alt3(message.freezeDuration)
        buffer.p2Alt3(message.targetOffsetZ)
        buffer.p4Alt2(message.targetCoord.packed)
    }
}
