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
        buffer.p2Alt1(message.targetOffsetZ)
        buffer.p2Alt2(message.targetHeight)
        buffer.p4Alt2(message.targetCoord.packed)
        buffer.p2(message.targetOffsetX)
        buffer.p3Alt3(message.targetIndex)
        buffer.p2Alt1(message.freezeDuration)
        buffer.p2(message.slot)
        buffer.p1Alt1(if (message.deleteOnFreezeEnd) 1 else 0)
    }
}
