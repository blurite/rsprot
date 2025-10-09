package net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.HeadCustomisation

@Suppress("DuplicatedCode")
public class NpcHeadCustomisationEncoder : PrecomputedExtendedInfoEncoder<HeadCustomisation> {
    override fun precompute(
        alloc: ByteBufAllocator,
        huffmanCodecProvider: HuffmanCodecProvider,
        extendedInfo: HeadCustomisation,
    ): JagByteBuf {
        val customisation = extendedInfo.customisation
        if (customisation == null) {
            val buffer =
                alloc
                    .buffer(1, 1)
                    .toJagByteBuf()
            buffer.pFlag(FLAG_RESET)
            return buffer
        }
        val capacity =
            3 + (customisation.models.size * 2) +
                (customisation.recolours.size * 2) +
                (customisation.retexture.size * 2)
        val buffer =
            alloc
                .buffer(capacity, capacity)
                .toJagByteBuf()
        var flag = 0
        if (customisation.models.isNotEmpty()) {
            flag = flag or FLAG_REMODEL
        }
        if (customisation.recolours.isNotEmpty()) {
            flag = flag or FLAG_RECOLOUR
        }
        if (customisation.retexture.isNotEmpty()) {
            flag = flag or FLAG_RETEXTURE
        }
        if (customisation.mirror != null) {
            flag = flag or FLAG_MIRROR_LOCAL_PLAYER
        }
        buffer.pFlag(flag)
        if (flag and FLAG_REMODEL != 0) {
            buffer.p1Alt2(customisation.models.size)
            for (model in customisation.models) {
                buffer.p2Alt3(model)
            }
        }
        if (flag and FLAG_RECOLOUR != 0) {
            for (recol in customisation.recolours) {
                buffer.p2Alt3(recol)
            }
        }
        if (flag and FLAG_RETEXTURE != 0) {
            for (retex in customisation.retexture) {
                buffer.p2Alt3(retex)
            }
        }
        if (flag and FLAG_MIRROR_LOCAL_PLAYER != 0) {
            buffer.p1Alt3(if (customisation.mirror == true) 1 else 0)
        }
        return buffer
    }

    private fun JagByteBuf.pFlag(value: Int) {
        p1Alt1(value)
    }

    private companion object {
        private const val FLAG_RESET: Int = 0x1
        private const val FLAG_REMODEL: Int = 0x2
        private const val FLAG_RECOLOUR: Int = 0x4
        private const val FLAG_RETEXTURE: Int = 0x8
        private const val FLAG_MIRROR_LOCAL_PLAYER: Int = 0x10
    }
}
