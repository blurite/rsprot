package net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.common.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.common.game.outgoing.info.npcinfo.extendedinfo.BaseAnimationSet

public class NpcBaseAnimationSetEncoder : PrecomputedExtendedInfoEncoder<BaseAnimationSet> {
    override fun precompute(
        alloc: ByteBufAllocator,
        huffmanCodec: HuffmanCodec,
        extendedInfo: BaseAnimationSet,
    ): JagByteBuf {
        val flag = extendedInfo.overrides
        val bitCount = flag.countOneBits()
        val capacity = 4 + (bitCount * 2)
        val buffer =
            alloc
                .buffer(capacity, capacity)
                .toJagByteBuf()
        buffer.p4Alt3(flag)
        if (flag and 0x1 != 0) {
            buffer.p2(extendedInfo.turnLeftAnim.toInt())
        }
        if (flag and 0x2 != 0) {
            buffer.p2Alt1(extendedInfo.turnRightAnim.toInt())
        }
        if (flag and 0x4 != 0) {
            buffer.p2(extendedInfo.walkAnim.toInt())
        }
        if (flag and 0x8 != 0) {
            buffer.p2(extendedInfo.walkAnimBack.toInt())
        }
        if (flag and 0x10 != 0) {
            buffer.p2Alt1(extendedInfo.walkAnimLeft.toInt())
        }
        if (flag and 0x20 != 0) {
            buffer.p2Alt3(extendedInfo.walkAnimRight.toInt())
        }
        if (flag and 0x40 != 0) {
            buffer.p2(extendedInfo.runAnim.toInt())
        }
        if (flag and 0x80 != 0) {
            buffer.p2Alt2(extendedInfo.runAnimBack.toInt())
        }
        if (flag and 0x100 != 0) {
            buffer.p2Alt2(extendedInfo.runAnimLeft.toInt())
        }
        if (flag and 0x200 != 0) {
            buffer.p2Alt3(extendedInfo.runAnimRight.toInt())
        }
        if (flag and 0x400 != 0) {
            buffer.p2Alt2(extendedInfo.crawlAnim.toInt())
        }
        if (flag and 0x800 != 0) {
            buffer.p2Alt3(extendedInfo.crawlAnimBack.toInt())
        }
        if (flag and 0x1000 != 0) {
            buffer.p2Alt1(extendedInfo.crawlAnimLeft.toInt())
        }
        if (flag and 0x2000 != 0) {
            buffer.p2Alt3(extendedInfo.crawlAnimRight.toInt())
        }
        if (flag and 0x4000 != 0) {
            buffer.p2(extendedInfo.readyAnim.toInt())
        }
        return buffer
    }
}
