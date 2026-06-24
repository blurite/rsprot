package net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.BodyCustomisation
import net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.PlayerComposition
import net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.TypeCustomisation
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.Appearance

@Suppress("DuplicatedCode")
public class NpcBodyCustomisationEncoder : PrecomputedExtendedInfoEncoder<BodyCustomisation> {
    override fun precompute(
        alloc: ByteBufAllocator,
        huffmanCodecProvider: HuffmanCodecProvider,
        extendedInfo: BodyCustomisation,
    ): JagByteBuf {
        val customisation = extendedInfo.customisation
        if (customisation != null) {
            return precomputeCustomisation(alloc, customisation)
        }
        val composition = extendedInfo.composition
        if (composition != null) {
            return precomputeComposition(alloc, composition)
        }
        val buffer =
            alloc
                .buffer(1, 1)
                .toJagByteBuf()
        buffer.pFlag(FLAG_RESET)
        return buffer
    }

    private fun precomputeCustomisation(
        alloc: ByteBufAllocator,
        customisation: TypeCustomisation,
    ): JagByteBuf {
        val capacity =
            6 + (customisation.models.size * 4) +
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
        buffer.pFlag(flag)
        if (flag and FLAG_REMODEL != 0) {
            buffer.pModelCount(customisation.models.size)
            for (model in customisation.models) {
                buffer.pModel(model)
            }
        }
        if (flag and FLAG_RECOLOUR != 0) {
            buffer.pRecolourCount(customisation.recolours.size)
            for (recol in customisation.recolours) {
                buffer.pRecolour(recol)
            }
        }
        if (flag and FLAG_RETEXTURE != 0) {
            buffer.pRetextureCount(customisation.retexture.size)
            for (retex in customisation.retexture) {
                buffer.pRetexture(retex)
            }
        }
        return buffer
    }

    private fun precomputeComposition(
        alloc: ByteBufAllocator,
        composition: PlayerComposition,
    ): JagByteBuf {
        val colours = composition.colours
        val capacity = 6 + (12 * (4 + 2)) + ((colours?.size ?: 0) * 2)
        val buffer =
            alloc
                .buffer(capacity, capacity)
                .toJagByteBuf()
        var flag = FLAG_REMODEL or FLAG_PLAYER_COMPOSITION
        if (colours != null) {
            flag = flag or FLAG_RECOLOUR
        }
        buffer.pFlag(flag)

        // Equipment is always written
        pEquipment(buffer, composition)

        if (colours != null) {
            buffer.pRecolourCount(colours.size)
            for (recol in colours) {
                buffer.pRecolour(recol.toInt() and 0xFFFF)
            }
        }

        // Body type and ident kit, always written
        pIdentKits(buffer, composition)
        return buffer
    }

    private fun pEquipment(
        buffer: JagByteBuf,
        extendedInfo: PlayerComposition,
    ) {
        // Always write full 12 slots as client expects it
        buffer.pModelCount(12)
        val identKit = extendedInfo.identKit
        val objs = extendedInfo.wornObjs
        val hiddenWearposFlag = buildHiddenWearposFlag(extendedInfo.hiddenWearPos)
        for (wearpos in 0..<12) {
            if (hiddenWearposFlag and (1 shl wearpos) != 0) {
                buffer.pModel(0)
                continue
            }
            val obj = objs[wearpos].toInt() and 0xFFFF
            if (obj != 0xFFFF) {
                buffer.pModel(obj + 0x800)
                continue
            }
            val identKitSlot = Appearance.identKitSlotList[wearpos]
            if (identKitSlot == -1) {
                buffer.pModel(0)
                continue
            }
            val identKitValue = identKit[identKitSlot].toInt() and 0xFFFF
            if (identKitValue == 0xFFFF) {
                buffer.pModel(0)
            } else {
                buffer.pModel(identKitValue + 0x100)
            }
        }
    }

    private fun pIdentKits(
        buffer: JagByteBuf,
        extendedInfo: PlayerComposition,
    ) {
        buffer.pBodyType(extendedInfo.bodyType.toInt())
        // Always write full 12 slots as client expects it
        buffer.pIdentKitCount(12)
        val identKit = extendedInfo.identKit
        for (wearpos in 0..<12) {
            val identKitSlot = Appearance.identKitSlotList[wearpos]
            if (identKitSlot == -1) {
                buffer.pIdentKit(0)
                continue
            }
            val identKitValue = identKit[identKitSlot].toInt() and 0xFFFF
            if (identKitValue == 0xFFFF) {
                buffer.pIdentKit(0)
            } else {
                buffer.pIdentKit(identKitValue + 0x100)
            }
        }
    }

    private fun buildHiddenWearposFlag(hidden: ByteArray): Int {
        var hiddenWearposFlag = 0
        for (i in 0..<12) {
            val pos = hidden[i].toInt()
            val wearpos2 = pos and 0xF
            if (wearpos2 != 0xF) {
                hiddenWearposFlag = hiddenWearposFlag or (1 shl wearpos2)
            }
            val wearpos3 = pos ushr 4 and 0xF
            if (wearpos3 != 0xF) {
                hiddenWearposFlag = hiddenWearposFlag or (1 shl wearpos3)
            }
        }
        return hiddenWearposFlag
    }

    private fun JagByteBuf.pModelCount(value: Int) {
        p1Alt1(value)
    }

    private fun JagByteBuf.pRecolourCount(value: Int) {
        p1Alt2(value)
    }

    private fun JagByteBuf.pRetextureCount(value: Int) {
        p1Alt2(value)
    }

    private fun JagByteBuf.pBodyType(value: Int) {
        p1Alt1(value)
    }

    private fun JagByteBuf.pIdentKitCount(value: Int) {
        p1Alt1(value)
    }

    private fun JagByteBuf.pRecolour(value: Int) {
        p2Alt1(value)
    }

    private fun JagByteBuf.pRetexture(value: Int) {
        p2Alt3(value)
    }

    private fun JagByteBuf.pModel(value: Int) {
        p4(value)
    }

    private fun JagByteBuf.pIdentKit(value: Int) {
        p2(value)
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
