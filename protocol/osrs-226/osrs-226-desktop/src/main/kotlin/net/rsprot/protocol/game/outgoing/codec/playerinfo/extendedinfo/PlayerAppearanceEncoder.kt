package net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.common.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.common.game.outgoing.info.playerinfo.extendedinfo.Appearance
import net.rsprot.protocol.common.game.outgoing.info.playerinfo.extendedinfo.ObjTypeCustomisation

@Suppress("DuplicatedCode")
public class PlayerAppearanceEncoder : PrecomputedExtendedInfoEncoder<Appearance> {
    override fun precompute(
        alloc: ByteBufAllocator,
        huffmanCodecProvider: HuffmanCodecProvider,
        extendedInfo: Appearance,
    ): JagByteBuf {
        val intermediate = alloc.buffer(100).toJagByteBuf()
        intermediate.p1(extendedInfo.bodyType.toInt())
        intermediate.p1(extendedInfo.skullIcon.toInt())
        intermediate.p1(extendedInfo.overheadIcon.toInt())
        if (extendedInfo.transformedNpcId != UShort.MAX_VALUE) {
            pTransmog(intermediate, extendedInfo)
        } else {
            pEquipment(intermediate, extendedInfo)
        }
        pIdentKits(intermediate, extendedInfo)
        pColours(intermediate, extendedInfo)
        pBaseAnimationSet(intermediate, extendedInfo)
        intermediate.pjstr(extendedInfo.name)
        intermediate.p1(extendedInfo.combatLevel.toInt())
        intermediate.p2(extendedInfo.skillLevel.toInt())
        intermediate.p1(if (extendedInfo.hidden) 1 else 0)
        pObjTypeCustomisations(intermediate, extendedInfo)
        intermediate.pjstr(extendedInfo.beforeName)
        intermediate.pjstr(extendedInfo.afterName)
        intermediate.pjstr(extendedInfo.afterCombatLevel)
        intermediate.p1(extendedInfo.pronoun.toInt())
        val capacity = intermediate.readableBytes() + 1
        val buffer = alloc.buffer(capacity, capacity).toJagByteBuf()
        buffer.p1(capacity - 1)
        try {
            buffer.pdataAlt1(intermediate.buffer)
        } finally {
            intermediate.buffer.release()
        }
        return buffer
    }

    private fun pTransmog(
        intermediate: JagByteBuf,
        extendedInfo: Appearance,
    ) {
        intermediate.p2(-1)
        intermediate.p2(extendedInfo.transformedNpcId.toInt())
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

    private fun pEquipment(
        intermediate: JagByteBuf,
        extendedInfo: Appearance,
    ) {
        val identKit = extendedInfo.identKit
        val objs = extendedInfo.wornObjs
        val hiddenWearposFlag = buildHiddenWearposFlag(extendedInfo.hiddenWearPos)
        for (wearpos in 0..<12) {
            if (hiddenWearposFlag and (1 shl wearpos) != 0) {
                intermediate.p1(0)
                continue
            }
            val obj = objs[wearpos].toInt() and 0xFFFF
            if (obj != 0xFFFF) {
                intermediate.p2(obj + 0x800)
                continue
            }
            val identKitSlot = Appearance.identKitSlotList[wearpos]
            if (identKitSlot == -1) {
                intermediate.p1(0)
                continue
            }
            val identKitValue = identKit[identKitSlot].toInt() and 0xFFFF
            if (identKitValue == 0xFFFF) {
                intermediate.p1(0)
            } else {
                intermediate.p2(identKitValue + 0x100)
            }
        }
    }

    private fun pIdentKits(
        intermediate: JagByteBuf,
        extendedInfo: Appearance,
    ) {
        val identKit = extendedInfo.identKit
        for (wearpos in 0..<12) {
            val identKitSlot = Appearance.identKitSlotList[wearpos]
            if (identKitSlot == -1) {
                intermediate.p1(0)
                continue
            }
            val identKitValue = identKit[identKitSlot].toInt() and 0xFFFF
            if (identKitValue == 0xFFFF) {
                intermediate.p1(0)
            } else {
                intermediate.p2(identKitValue + 0x100)
            }
        }
    }

    private fun pColours(
        intermediate: JagByteBuf,
        extendedInfo: Appearance,
    ) {
        val colours = extendedInfo.colours
        for (i in colours.indices) {
            intermediate.p1(colours[i].toInt())
        }
    }

    private fun pBaseAnimationSet(
        intermediate: JagByteBuf,
        extendedInfo: Appearance,
    ) {
        intermediate.p2(extendedInfo.readyAnim.toInt())
        intermediate.p2(extendedInfo.turnAnim.toInt())
        intermediate.p2(extendedInfo.walkAnim.toInt())
        intermediate.p2(extendedInfo.walkAnimBack.toInt())
        intermediate.p2(extendedInfo.walkAnimLeft.toInt())
        intermediate.p2(extendedInfo.walkAnimRight.toInt())
        intermediate.p2(extendedInfo.runAnim.toInt())
    }

    private fun pObjTypeCustomisations(
        intermediate: JagByteBuf,
        extendedInfo: Appearance,
    ) {
        val marker = intermediate.writerIndex()
        intermediate.skipWrite(2)
        val objTypeCustomisations = extendedInfo.objTypeCustomisation
        var flag = 0
        for (wearpos in objTypeCustomisations.indices) {
            val objTypeCustomisation = objTypeCustomisations[wearpos] ?: continue
            pObjTypeCustomisation(intermediate, objTypeCustomisation)
            flag = flag or (1 shl (12 - wearpos))
        }
        if (extendedInfo.forceModelRefresh) flag = flag or 0x8000
        val pos = intermediate.writerIndex()
        intermediate.writerIndex(marker)
        intermediate.p2(flag)
        intermediate.writerIndex(pos)
    }

    private fun pObjTypeCustomisation(
        intermediate: JagByteBuf,
        customisation: ObjTypeCustomisation,
    ) {
        val recolIndices = customisation.recolIndices.toInt()
        val retexIndices = customisation.retexIndices.toInt()
        var flag = 0
        if (recolIndices != 0xFF) {
            flag = flag or 0x1
        }
        if (retexIndices != 0xFF) {
            flag = flag or 0x2
        }
        intermediate.p1(flag)
        pObjTypeCustomisation(
            intermediate,
            recolIndices,
            customisation.recol1.toInt(),
            customisation.recol2.toInt(),
        )
        pObjTypeCustomisation(
            intermediate,
            retexIndices,
            customisation.retex1.toInt(),
            customisation.retex2.toInt(),
        )
    }

    private fun pObjTypeCustomisation(
        intermediate: JagByteBuf,
        flag: Int,
        value1: Int,
        value2: Int,
    ) {
        intermediate.p1(flag)
        if (flag and 0xF != 0xF) {
            intermediate.p2(value1)
        }
        if (flag and 0xF0 != 0xF0) {
            intermediate.p2(value2)
        }
    }
}
