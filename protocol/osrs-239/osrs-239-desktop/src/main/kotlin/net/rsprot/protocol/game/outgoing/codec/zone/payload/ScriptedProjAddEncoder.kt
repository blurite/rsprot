package net.rsprot.protocol.game.outgoing.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.zone.payload.ScriptedProjAdd
import net.rsprot.protocol.internal.game.outgoing.codec.zone.payload.ZoneProtEncoder

public class ScriptedProjAddEncoder : ZoneProtEncoder<ScriptedProjAdd> {
    override val prot: ServerProt = GameServerProt.SCRIPTEDPROJ_ADD

    override fun encode(
        buffer: JagByteBuf,
        message: ScriptedProjAdd,
    ) {
        buffer.p2Alt1(message.sourceOffsetX)
        buffer.p2(message.id)
        buffer.p2(message.targetOffsetZ)
        buffer.p4Alt3(message.targetCoord.packed)
        buffer.p2Alt2(message.startTime)
        buffer.p2(message.sourceHeight)
        buffer.p2Alt1(message.curveScriptA)
        buffer.p3Alt3(message.targetIndex)
        buffer.p2(message.slot)
        buffer.p2Alt1(message.endTime)
        buffer.p1(message.coordInZonePacked)
        buffer.p2(message.targetOffsetX)
        buffer.p2Alt1(message.curveScriptH)
        buffer.p2Alt3(message.sourceOffsetZ)
        buffer.p2Alt1(message.targetHeight)
        buffer.p3Alt2(message.sourceIndex)
        buffer.p2Alt1(message.curveScriptT)
    }
}
