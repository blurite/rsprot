package net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.common.game.outgoing.info.encoder.OnDemandExtendedInfoEncoder
import net.rsprot.protocol.common.game.outgoing.info.shared.extendedinfo.Hit

public class PlayerHitEncoder : OnDemandExtendedInfoEncoder<Hit> {
    override fun encode(
        buffer: JagByteBuf,
        localPlayerIndex: Int,
        updatedAvatarIndex: Int,
        extendedInfo: Hit,
    ) {
        pHits(buffer, localPlayerIndex, updatedAvatarIndex, extendedInfo)
        pHeadBars(buffer, extendedInfo)
    }

    private fun pHits(
        buffer: JagByteBuf,
        localPlayerIndex: Int,
        updatedPlayerIndex: Int,
        info: Hit,
    ) {
        val countMarker = buffer.writerIndex()
        buffer.skipWrite(1)
        var count = 0
        for (hit in info.hitMarkList) {
            // If the hit appears on us, or we were the source of the hit in the first place
            val tinted =
                localPlayerIndex == updatedPlayerIndex ||
                    localPlayerIndex == (hit.sourceIndex - 0x10_000)
            // Skip the hitsplat if it isn't meant to render to us
            // Should be noted that we only check this on the main types, and not soak ones
            if (hit.otherType == UShort.MAX_VALUE && !tinted) {
                continue
            }
            val mainType = if (tinted) hit.selfType else hit.otherType
            val soakType = if (tinted) hit.selfSoakType else hit.otherSoakType
            if (mainType.toInt() == 0x7FFE) {
                buffer.pSmart1or2(0x7FFE)
            } else if (soakType != UShort.MAX_VALUE) {
                buffer.pSmart1or2(0x7FFF)
                buffer.pSmart1or2(mainType.toInt())
                buffer.pSmart1or2(hit.value.toInt())
                buffer.pSmart1or2(soakType.toInt())
                buffer.pSmart1or2(hit.soakValue.toInt())
            } else {
                buffer.pSmart1or2(mainType.toInt())
                buffer.pSmart1or2(hit.value.toInt())
            }
            buffer.pSmart1or2(hit.delay.toInt())
            // Exit out of the loop if there are more than 255 hits,
            // as that's the highest count we can write
            if (++count >= 0xFF) {
                break
            }
        }
        val writerIndex = buffer.writerIndex()
        buffer.writerIndex(countMarker)
        buffer.p1Alt3(count)
        buffer.writerIndex(writerIndex)
    }

    private fun pHeadBars(
        buffer: JagByteBuf,
        info: Hit,
    ) {
        val countMarker = buffer.writerIndex()
        buffer.skipWrite(1)
        var count = 0
        for (headBar in info.headBarList) {
            buffer.pSmart1or2(headBar.id.toInt())
            val endTime = headBar.endTime.toInt()
            buffer.pSmart1or2(endTime)
            if (endTime == 0x7FFF) {
                continue
            }
            buffer.pSmart1or2(headBar.startTime.toInt())
            buffer.p1Alt3(headBar.startFill.toInt())
            if (endTime > 0) {
                buffer.p1Alt1(headBar.endFill.toInt())
            }
            // Exit out of the loop if there are more than 255 head bars,
            // as that's the highest count we can write
            if (++count >= 0xFF) {
                break
            }
        }
        val writerIndex = buffer.writerIndex()
        buffer.writerIndex(countMarker)
        buffer.p1Alt1(count)
        buffer.writerIndex(writerIndex)
    }
}
