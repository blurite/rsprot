package net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.SpotAnimList
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util.SpotAnim

public class PlayerSpotAnimEncoder : PrecomputedExtendedInfoEncoder<net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.SpotAnimList> {
    override fun precompute(
	    alloc: ByteBufAllocator,
	    huffmanCodecProvider: HuffmanCodecProvider,
	    extendedInfo: net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.SpotAnimList,
    ): JagByteBuf {
        val changelist = extendedInfo.changelist
        val count = changelist.cardinality()
        val capacity = 1 + count * 7
        val buffer =
            alloc
                .buffer(capacity, capacity)
                .toJagByteBuf()
        buffer.p1Alt1(count)
        val spotanims = extendedInfo.spotanims
        var slot = changelist.nextSetBit(0)
        while (slot != -1) {
            val spotanim = SpotAnim(spotanims[slot])
            buffer.p1(slot)
            buffer.p2Alt3(spotanim.id)
            buffer.p4(spotanim.delay or (spotanim.height shl 16))
            slot = changelist.nextSetBit(slot + 1)
        }
        return buffer
    }
}
