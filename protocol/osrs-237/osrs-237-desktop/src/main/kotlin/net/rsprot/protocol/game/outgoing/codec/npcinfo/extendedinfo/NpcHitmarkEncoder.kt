package net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.internal.game.outgoing.info.encoder.OnDemandExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.HitmarkList

@Suppress("DuplicatedCode")
public class NpcHitmarkEncoder : OnDemandExtendedInfoEncoder<HitmarkList> {
    override fun encode(
        buffer: JagByteBuf,
        localPlayerIndex: Int,
        updatedAvatarIndex: Int,
        extendedInfo: HitmarkList,
    ) {
        val countMarker = buffer.writerIndex()
        buffer.skipWrite(1)
        var count = 0
        for (hit in extendedInfo.elements) {
            // If we were the source of the hit in the first place
            val tinted = localPlayerIndex == (hit.sourceIndex - 0x10_000)
            val type = if (tinted) hit.selfType else hit.otherType
            // Skip the hitsplat if it isn't meant to render to us
            // Should be noted that we only check this on the main types, and not soak ones
            if (type == UShort.MAX_VALUE) continue
            buffer.pSmart1or2(type.toInt())
            buffer.pSmart1or2(hit.value.toInt())
            buffer.pSmart1or2(hit.delay.toInt())
            buffer.pSmart1or2(hit.limit.toInt())
            // Exit out of the loop if there are more than 255 hits,
            // as that's the highest count we can write
            if (++count >= 0xFF) {
                break
            }
        }
        val writerIndex = buffer.writerIndex()
        buffer.writerIndex(countMarker)
        buffer.p1Alt2(count)
        buffer.writerIndex(writerIndex)
    }
}
