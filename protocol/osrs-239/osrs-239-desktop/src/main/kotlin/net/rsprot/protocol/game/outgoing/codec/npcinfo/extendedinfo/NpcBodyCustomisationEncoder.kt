package net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.BodyCustomisation

@Suppress("DuplicatedCode")
public class NpcBodyCustomisationEncoder : PrecomputedExtendedInfoEncoder<BodyCustomisation> {
    override fun precompute(
        alloc: ByteBufAllocator,
        huffmanCodecProvider: HuffmanCodecProvider,
        extendedInfo: BodyCustomisation,
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
            3 + (customisation.models.size * 4) +
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
        if (customisation.mirror == true) {
            flag = flag or FLAG_MIRROR_LOCAL_PLAYER
        }
        val playerComposition = customisation.playerComposition
        if (playerComposition != null) {
            flag = flag or FLAG_PLAYER_COMPOSITION
        }
        buffer.pFlag(flag)
        if (flag and FLAG_REMODEL != 0) {
            buffer.p1Alt1(customisation.models.size)
            for (model in customisation.models) {
                buffer.p4(model)
            }
        }
        if (flag and FLAG_RECOLOUR != 0) {
            buffer.p1Alt2(customisation.recolours.size)
            for (recol in customisation.recolours) {
                buffer.p2Alt1(recol)
            }
        }
        if (flag and FLAG_RETEXTURE != 0) {
            buffer.p1Alt2(customisation.retexture.size)
            for (retex in customisation.retexture) {
                buffer.p2Alt3(retex)
            }
        }
        if (playerComposition != null) {
            buffer.p1Alt1(playerComposition.bodyType)
            buffer.p1Alt1(playerComposition.identKit.size)
            for (worn in playerComposition.identKit) {
                buffer.p2(worn)
            }
        }
        return buffer
    }

    private fun JagByteBuf.pFlag(value: Int) {
        p1Alt2(value)
    }

    private companion object {
        private const val FLAG_RESET: Int = 0x1
        private const val FLAG_REMODEL: Int = 0x2
        private const val FLAG_RECOLOUR: Int = 0x4
        private const val FLAG_RETEXTURE: Int = 0x8
        private const val FLAG_MIRROR_LOCAL_PLAYER: Int = 0x10
        private const val FLAG_PLAYER_COMPOSITION = 0x20
    }
}
