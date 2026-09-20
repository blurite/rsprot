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
        buffer.p3Alt3(message.targetIndex)
        buffer.p2(message.targetHeight)
        buffer.p2Alt3(message.endTime)
        buffer.p2Alt3(message.slot)
        buffer.p2(message.sourceOffsetZ)
        buffer.p3Alt1(message.sourceIndex)
        buffer.p2Alt2(message.curveScriptH)
        buffer.p2Alt1(message.id)
        buffer.p2Alt3(message.sourceOffsetX)
        buffer.p2(message.sourceHeight)
        buffer.p1Alt1(message.coordInZonePacked)
        buffer.p4Alt1(message.targetCoord.packed)
        buffer.p2(message.startTime)
        buffer.p2Alt2(message.targetOffsetZ)
        buffer.p2(message.targetOffsetX)
        buffer.p2Alt1(message.curveScriptT)
        buffer.p2Alt3(message.curveScriptA)
    }
}
