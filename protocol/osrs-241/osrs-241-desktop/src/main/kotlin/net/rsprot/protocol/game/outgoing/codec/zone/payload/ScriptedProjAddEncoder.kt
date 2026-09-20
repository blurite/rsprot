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
        buffer.p2(message.curveScriptT)
        buffer.p3Alt1(message.sourceIndex)
        buffer.p2Alt2(message.sourceHeight)
        buffer.p2Alt3(message.targetHeight)
        buffer.p2(message.sourceOffsetX)
        buffer.p2Alt2(message.targetOffsetX)
        buffer.p2Alt1(message.targetOffsetZ)
        buffer.p2Alt2(message.id)
        buffer.p3Alt2(message.targetIndex)
        buffer.p2(message.endTime)
        buffer.p2Alt3(message.slot)
        buffer.p2(message.startTime)
        buffer.p2Alt2(message.sourceOffsetZ)
        buffer.p2Alt3(message.curveScriptA)
        buffer.p4Alt1(message.targetCoord.packed)
        buffer.p2Alt3(message.curveScriptH)
        buffer.p1Alt2(message.coordInZonePacked)
    }
}
