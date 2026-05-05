package net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.internal.game.outgoing.info.encoder.OnDemandExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.HeadbarList
import net.rsprot.protocol.message.toIntOrMinusOne

@Suppress("DuplicatedCode")
public class NpcHeadbarEncoder : OnDemandExtendedInfoEncoder<HeadbarList> {
    override fun encode(
        buffer: JagByteBuf,
        localPlayerIndex: Int,
        updatedAvatarIndex: Int,
        extendedInfo: HeadbarList,
    ) {
        val countMarker = buffer.writerIndex()
        buffer.skipWrite(1)
        var count = 0
        for (headBar in extendedInfo.elements) {
            val selfType = headBar.selfType.toIntOrMinusOne()
            val isSelf = localPlayerIndex == (headBar.sourceIndex - 0x10_000)
            if (isSelf && selfType == -1) {
                continue
            }
            val otherType = headBar.otherType.toIntOrMinusOne()
            if (!isSelf && otherType == -1) {
                continue
            }
            val type = if (isSelf) selfType else otherType
            buffer.pSmart1or2(type)
            val endTime = headBar.endTime.toInt()
            buffer.pSmart1or2(endTime)
            if (endTime != 0x7FFF) {
                buffer.pSmart1or2(headBar.startTime.toInt())
                buffer.p1Alt1(headBar.startFill.toInt())
                if (endTime > 0) {
                    buffer.p1Alt2(headBar.endFill.toInt())
                }
            }
            // Exit out of the loop if there are more than 255 head bars,
            // as that's the highest count we can write
            if (++count >= 0xFF) {
                break
            }
        }
        val writerIndex = buffer.writerIndex()
        buffer.writerIndex(countMarker)
        buffer.p1(count)
        buffer.writerIndex(writerIndex)
    }
}
