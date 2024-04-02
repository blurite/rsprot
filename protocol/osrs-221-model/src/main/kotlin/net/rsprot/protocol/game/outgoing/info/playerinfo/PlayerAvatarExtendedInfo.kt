@file:Suppress("DuplicatedCode")

package net.rsprot.protocol.game.outgoing.info.playerinfo

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBufAllocator
import io.netty.util.internal.SystemPropertyUtil
import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.game.outgoing.info.playerinfo.filter.ExtendedInfoFilter
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.MoveSpeed
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.ObjTypeCustomisation
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.FacePathingEntity
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.Tinting
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util.HeadBar
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util.HitMark
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util.SpotAnim
import net.rsprot.protocol.shared.platform.PlatformType

@Suppress("MemberVisibilityCanBePrivate")
public class PlayerAvatarExtendedInfo(
    private val protocol: PlayerInfoProtocol,
    private val localIndex: Int,
    private val filter: ExtendedInfoFilter,
    extendedInfoWriters: List<AvatarExtendedInfoWriter>,
    allocator: ByteBufAllocator,
    huffmanCodec: HuffmanCodec,
) {
    internal var flags: Int = 0
    private val blocks: PlayerAvatarExtendedInfoBlocks =
        PlayerAvatarExtendedInfoBlocks(
            extendedInfoWriters,
            allocator,
            huffmanCodec,
        )
    private val writers: Array<AvatarExtendedInfoWriter?> = buildPlatformWriterArray(extendedInfoWriters)

    // Appearance has built-in engine support for caching, so we use a changes counter to track this
    private val otherAppearanceChangesCounter: IntArray = IntArray(PlayerInfoProtocol.PROTOCOL_CAPACITY)
    private var appearanceChangesCounter: Int = 0

    public fun setMoveSpeed(value: Int) {
        verify {
            require(value in -1..2) {
                "Unexpected move speed: $value, expected values: -1, 0, 1, 2"
            }
        }
        blocks.moveSpeed.value = value
        flags = flags or MOVE_SPEED
    }

    public fun setTempMoveSpeed(value: Int) {
        verify {
            require(value in -1..2 || value == 127) {
                "Unexpected temporary move speed: $value, expected values: -1, 0, 1, 2, 127"
            }
        }
        blocks.temporaryMoveSpeed.value = value
        flags = flags or TEMP_MOVE_SPEED
    }

    public fun setSequence(
        id: Int,
        delay: Int,
    ) {
        verify {
            require(id == -1 || id in UNSIGNED_SHORT_RANGE) {
                "Unexpected sequence id: $id, expected value -1 or in range $UNSIGNED_SHORT_RANGE"
            }
            require(delay in UNSIGNED_SHORT_RANGE) {
                "Unexpected sequence delay: $delay, expected range: $UNSIGNED_SHORT_RANGE"
            }
        }
        blocks.sequence.id = id.toUShort()
        blocks.sequence.delay = delay.toUShort()
        flags = flags or SEQUENCE
    }

    public fun setFacePathingEntity(index: Int) {
        verify {
            require(index == -1 || index in 0..0x107FF) {
                "Unexpected pathing entity index: $index, expected values: -1 to reset, " +
                    "0-65535 for NPCs, 65536-67583 for players"
            }
        }
        blocks.facePathingEntity.index = index
        flags = flags or FACE_PATHINGENTITY
    }

    public fun setFaceAngle(angle: Int) {
        verify {
            require(angle in 0..2047) {
                "Unexpected angle: $angle, expected range: 0-2047"
            }
        }
        blocks.faceAngle.angle = angle
        flags = flags or FACE_ANGLE
    }

    public fun setSay(text: String) {
        verify {
            require(text.length <= 80) {
                "Unexpected say input; expected value 80 characters or less, " +
                    "input len: ${text.length}, input: $text"
            }
        }
        blocks.say.text = text
        flags = flags or SAY
    }

    public fun setChat(
        colour: Int,
        effects: Int,
        modicon: Int,
        autotyper: Boolean,
        text: String,
        pattern: ByteArray?,
    ) {
        verify {
            require(text.length <= 80) {
                "Unexpected chat input; expected value 80 characters or less, " +
                    "input len: ${text.length}, input: $text"
            }
            require(colour in 0..20) {
                "Unexpected colour value: $colour, expected range: 0-20"
            }
            // No verification for mod icons, as servers often create custom ranks
        }
        val patternLength = if (colour in 13..20) colour - 12 else 0
        // Unlike most inputs, these are necessary to avoid crashes, so these can't be turned off.
        if (patternLength in 1..8) {
            requireNotNull(pattern) {
                "Pattern cannot be null if pattern length is defined."
            }
            require(pattern.size == patternLength) {
                "Pattern length does not match the size configured in the colour property."
            }
        }
        blocks.chat.colour = colour.toUByte()
        blocks.chat.effects = effects.toUByte()
        blocks.chat.modicon = modicon.toUByte()
        blocks.chat.autotyper = autotyper
        blocks.chat.text = text
        blocks.chat.pattern = pattern
        flags = flags or CHAT
    }

    public fun setExactMove(
        deltaX1: Int,
        deltaZ1: Int,
        delay1: Int,
        deltaX2: Int,
        deltaZ2: Int,
        delay2: Int,
        direction: Int,
    ) {
        verify {
            require(delay1 >= 0) {
                "First delay cannot be negative: $delay1"
            }
            require(delay2 >= 0) {
                "Second delay cannot be negative: $delay2"
            }
            require(delay2 > delay1) {
                "Second delay must be greater than the first: $delay1 > $delay2"
            }
            require(direction in 0..2047) {
                "Unexpected direction value: $direction, expected range: 0..2047"
            }
            require(deltaX1 in SIGNED_BYTE_RANGE) {
                "Unexpected deltaX1: $deltaX1, expected range: $SIGNED_BYTE_RANGE"
            }
            require(deltaZ1 in SIGNED_BYTE_RANGE) {
                "Unexpected deltaZ1: $deltaZ1, expected range: $SIGNED_BYTE_RANGE"
            }
            require(deltaX2 in SIGNED_BYTE_RANGE) {
                "Unexpected deltaX1: $deltaX2, expected range: $SIGNED_BYTE_RANGE"
            }
            require(deltaZ2 in SIGNED_BYTE_RANGE) {
                "Unexpected deltaZ1: $deltaZ2, expected range: $SIGNED_BYTE_RANGE"
            }
        }
        blocks.exactMove.deltaX1 = deltaX1.toUByte()
        blocks.exactMove.deltaZ1 = deltaZ1.toUByte()
        blocks.exactMove.delay1 = delay1.toUShort()
        blocks.exactMove.deltaX2 = deltaX2.toUByte()
        blocks.exactMove.deltaZ2 = deltaZ2.toUByte()
        blocks.exactMove.delay2 = delay2.toUShort()
        blocks.exactMove.direction = direction.toUShort()
        flags = flags or EXACT_MOVE
    }

    public fun setSpotAnim(
        slot: Int,
        id: Int,
        delay: Int,
        height: Int,
    ) {
        verify {
            require(slot in UNSIGNED_BYTE_RANGE) {
                "Unexpected slot: $slot, expected range: $UNSIGNED_BYTE_RANGE"
            }
            require(id in UNSIGNED_SHORT_RANGE) {
                "Unexpected id: $id, expected range: $UNSIGNED_SHORT_RANGE"
            }
            require(delay in UNSIGNED_SHORT_RANGE) {
                "Unexpected delay: $delay, expected range: $UNSIGNED_SHORT_RANGE"
            }
            require(height in UNSIGNED_SHORT_RANGE) {
                "Unexpected delay: $height, expected range: $UNSIGNED_SHORT_RANGE"
            }
        }
        blocks.spotAnims.set(slot, SpotAnim(id, delay, height))
        flags = flags or SPOTANIM
    }

    public fun addHitMark(
        sourceIndex: Int,
        selfType: Int,
        otherType: Int = selfType,
        value: Int,
        delay: Int = 0,
    ) {
        verify {
            require(sourceIndex == -1 || sourceIndex in 0..0x107FF) {
                "Unexpected source index: $sourceIndex, expected values: -1 to reset, " +
                    "0-65535 for NPCs, 65536-67583 for players"
            }
            require(selfType in UNSIGNED_SMART_1_OR_2_RANGE) {
                "Unexpected selfType: $selfType, expected range $UNSIGNED_SMART_1_OR_2_RANGE"
            }
            require(otherType in UNSIGNED_SMART_1_OR_2_RANGE) {
                "Unexpected otherType: $otherType, expected range $UNSIGNED_SMART_1_OR_2_RANGE"
            }
            require(value in UNSIGNED_SMART_1_OR_2_RANGE) {
                "Unexpected value: $value, expected range $UNSIGNED_SMART_1_OR_2_RANGE"
            }
            require(delay in UNSIGNED_SMART_1_OR_2_RANGE) {
                "Unexpected delay: $delay, expected range $UNSIGNED_SMART_1_OR_2_RANGE"
            }
        }
        blocks.hit.hitMarkList +=
            HitMark(
                sourceIndex,
                selfType.toUShort(),
                otherType.toUShort(),
                value.toUShort(),
                delay.toUShort(),
            )
        flags = flags or HITS
    }

    public fun removeHitMark(delay: Int = 0) {
        verify {
            require(delay in UNSIGNED_SMART_1_OR_2_RANGE) {
                "Unexpected delay: $delay, expected range $UNSIGNED_SMART_1_OR_2_RANGE"
            }
        }
        blocks.hit.hitMarkList += HitMark(0x7FFEu, delay.toUShort())
        flags = flags or HITS
    }

    public fun addSoakedHitMark(
        sourceIndex: Int,
        selfType: Int,
        otherType: Int = selfType,
        value: Int,
        selfSoakType: Int,
        otherSoakType: Int = selfSoakType,
        soakValue: Int,
        delay: Int = 0,
    ) {
        verify {
            require(sourceIndex == -1 || sourceIndex in 0..0x107FF) {
                "Unexpected source index: $sourceIndex, expected values: -1 to reset, " +
                    "0-65535 for NPCs, 65536-67583 for players"
            }
            require(selfType in UNSIGNED_SMART_1_OR_2_RANGE) {
                "Unexpected selfType: $selfType, expected range $UNSIGNED_SMART_1_OR_2_RANGE"
            }
            require(otherType in UNSIGNED_SMART_1_OR_2_RANGE) {
                "Unexpected otherType: $otherType, expected range $UNSIGNED_SMART_1_OR_2_RANGE"
            }
            require(value in UNSIGNED_SMART_1_OR_2_RANGE) {
                "Unexpected value: $value, expected range $UNSIGNED_SMART_1_OR_2_RANGE"
            }
            require(selfSoakType in UNSIGNED_SMART_1_OR_2_RANGE) {
                "Unexpected selfType: $selfSoakType, expected range $UNSIGNED_SMART_1_OR_2_RANGE"
            }
            require(otherSoakType in UNSIGNED_SMART_1_OR_2_RANGE) {
                "Unexpected otherType: $otherSoakType, expected range $UNSIGNED_SMART_1_OR_2_RANGE"
            }
            require(soakValue in UNSIGNED_SMART_1_OR_2_RANGE) {
                "Unexpected value: $soakValue, expected range $UNSIGNED_SMART_1_OR_2_RANGE"
            }
            require(delay in UNSIGNED_SMART_1_OR_2_RANGE) {
                "Unexpected delay: $delay, expected range $UNSIGNED_SMART_1_OR_2_RANGE"
            }
        }
        blocks.hit.hitMarkList +=
            HitMark(
                sourceIndex,
                selfType.toUShort(),
                otherType.toUShort(),
                value.toUShort(),
                selfSoakType.toUShort(),
                otherSoakType.toUShort(),
                soakValue.toUShort(),
                delay.toUShort(),
            )
        flags = flags or HITS
    }

    public fun addHeadBar(
        id: Int,
        startFill: Int,
        endFill: Int = startFill,
        startTime: Int = 0,
        endTime: Int = 0,
    ) {
        verify {
            require(id in UNSIGNED_SMART_1_OR_2_RANGE) {
                "Unexpected id: $id, expected range $UNSIGNED_SMART_1_OR_2_RANGE"
            }
            require(startFill in UNSIGNED_BYTE_RANGE) {
                "Unexpected startFill: $startFill, expected range $UNSIGNED_BYTE_RANGE"
            }
            require(endFill in UNSIGNED_BYTE_RANGE) {
                "Unexpected endFill: $endFill, expected range $UNSIGNED_BYTE_RANGE"
            }
            require(startTime in UNSIGNED_SMART_1_OR_2_RANGE) {
                "Unexpected startTime: $startTime, expected range $UNSIGNED_SMART_1_OR_2_RANGE"
            }
            require(endTime in UNSIGNED_SMART_1_OR_2_RANGE) {
                "Unexpected endTime: $endTime, expected range $UNSIGNED_SMART_1_OR_2_RANGE"
            }
        }
        blocks.hit.headBarList +=
            HeadBar(
                id.toUShort(),
                startFill.toUByte(),
                endFill.toUByte(),
                startTime.toUShort(),
                endTime.toUShort(),
            )
        flags = flags or HITS
    }

    public fun removeHeadBar(id: Int) {
        addHeadBar(
            id,
            startFill = 0,
            endTime = HeadBar.REMOVED.toInt(),
        )
    }

    public fun tinting(
        startTime: Int,
        endTime: Int,
        hue: Int,
        saturation: Int,
        lightness: Int,
        weight: Int,
        visibleToIndex: Int = -1,
    ) {
        verify {
            require(startTime in UNSIGNED_SHORT_RANGE) {
                "Unexpected startTime: $startTime, expected range $UNSIGNED_SHORT_RANGE"
            }
            require(endTime in UNSIGNED_SHORT_RANGE) {
                "Unexpected endTime: $endTime, expected range $UNSIGNED_SHORT_RANGE"
            }
            require(endTime >= startTime) {
                "End time should be equal to or greater than start time: $endTime > $startTime"
            }
            require(hue in UNSIGNED_BYTE_RANGE) {
                "Unexpected hue: $hue, expected range $UNSIGNED_BYTE_RANGE"
            }
            require(saturation in UNSIGNED_BYTE_RANGE) {
                "Unexpected saturation: $saturation, expected range $UNSIGNED_BYTE_RANGE"
            }
            require(lightness in UNSIGNED_BYTE_RANGE) {
                "Unexpected lightness: $lightness, expected range $UNSIGNED_BYTE_RANGE"
            }
            require(weight in UNSIGNED_BYTE_RANGE) {
                "Unexpected weight: $weight, expected range $UNSIGNED_BYTE_RANGE"
            }
        }
        if (visibleToIndex != -1) {
            val otherPlayerInfo = protocol.getPlayerInfo(visibleToIndex)
            requireNotNull(otherPlayerInfo) {
                "Player at index $visibleToIndex does not exist."
            }
            val tint = Tinting()
            blocks.tinting.observerDependent[visibleToIndex] = tint
            tint.start = startTime.toUShort()
            tint.end = endTime.toUShort()
            tint.hue = hue.toUByte()
            tint.saturation = saturation.toUByte()
            tint.lightness = lightness.toUByte()
            tint.weight = weight.toUByte()
            otherPlayerInfo.observerExtendedInfoFlags.addFlag(
                localIndex,
                TINTING,
            )
        } else {
            val tint = blocks.tinting.global
            tint.start = startTime.toUShort()
            tint.end = endTime.toUShort()
            tint.hue = hue.toUByte()
            tint.saturation = saturation.toUByte()
            tint.lightness = lightness.toUByte()
            tint.weight = weight.toUByte()
            flags = flags or TINTING
        }
    }

    public fun initializeAppearance(
        name: String,
        combatLevel: Int,
        skillLevel: Int,
        hidden: Boolean,
        male: Boolean,
        textGender: Int,
        skullIcon: Int,
        overheadIcon: Int,
    ) {
        verify {
            require(name.length in 1..12) {
                "Unexpected name length, expected range 1..12"
            }
            require(combatLevel in UNSIGNED_BYTE_RANGE) {
                "Unexpected combatLevel $combatLevel, expected range $UNSIGNED_BYTE_RANGE"
            }
            require(skillLevel in UNSIGNED_SHORT_RANGE) {
                "Unexpected skill level $skillLevel, expected range $UNSIGNED_SHORT_RANGE"
            }
            require(textGender in UNSIGNED_BYTE_RANGE) {
                "Unexpected textGender $textGender, expected range $UNSIGNED_BYTE_RANGE"
            }
            require(skullIcon == -1 || skullIcon in UNSIGNED_BYTE_RANGE) {
                "Unexpected skullIcon $skullIcon, expected value -1 or in range $UNSIGNED_BYTE_RANGE"
            }
            require(overheadIcon == -1 || overheadIcon in UNSIGNED_BYTE_RANGE) {
                "Unexpected overheadIcon $overheadIcon, expected value -1 or in range $UNSIGNED_BYTE_RANGE"
            }
        }
        blocks.appearance.name = name
        blocks.appearance.combatLevel = combatLevel.toUByte()
        blocks.appearance.skillLevel = skillLevel.toUShort()
        blocks.appearance.hidden = hidden
        blocks.appearance.male = male
        blocks.appearance.textGender = textGender.toUByte()
        blocks.appearance.skullIcon = skullIcon.toUByte()
        blocks.appearance.overheadIcon = overheadIcon.toUByte()
        flagAppearance()
    }

    public fun setName(name: String) {
        verify {
            require(name.length in 1..12) {
                "Unexpected name length, expected range 1..12"
            }
        }
        if (blocks.appearance.name == name) {
            return
        }
        blocks.appearance.name = name
        flagAppearance()
    }

    public fun setCombatLevel(combatLevel: Int) {
        verify {
            require(combatLevel in UNSIGNED_BYTE_RANGE) {
                "Unexpected combatLevel $combatLevel, expected range $UNSIGNED_BYTE_RANGE"
            }
        }
        val level = combatLevel.toUByte()
        if (blocks.appearance.combatLevel == level) {
            return
        }
        blocks.appearance.combatLevel = level
        flagAppearance()
    }

    public fun setSkillLevel(skillLevel: Int) {
        verify {
            require(skillLevel in UNSIGNED_SHORT_RANGE) {
                "Unexpected skill level $skillLevel, expected range $UNSIGNED_SHORT_RANGE"
            }
        }
        val level = skillLevel.toUShort()
        if (blocks.appearance.skillLevel == level) {
            return
        }
        blocks.appearance.skillLevel = level
        flagAppearance()
    }

    public fun setHidden(hidden: Boolean) {
        if (blocks.appearance.hidden == hidden) {
            return
        }
        blocks.appearance.hidden = hidden
        flagAppearance()
    }

    public fun setMale(isMale: Boolean) {
        if (blocks.appearance.male == isMale) {
            return
        }
        blocks.appearance.male = isMale
        flagAppearance()
    }

    public fun setTextGender(num: Int) {
        verify {
            require(num in UNSIGNED_BYTE_RANGE) {
                "Unexpected textGender $num, expected range $UNSIGNED_BYTE_RANGE"
            }
        }
        val textGender = num.toUByte()
        if (blocks.appearance.textGender == textGender) {
            return
        }
        blocks.appearance.textGender = textGender
        flagAppearance()
    }

    public fun setSkullIcon(icon: Int) {
        verify {
            require(icon == -1 || icon in UNSIGNED_BYTE_RANGE) {
                "Unexpected skullIcon $icon, expected value -1 or in range $UNSIGNED_BYTE_RANGE"
            }
        }
        val skullIcon = icon.toUByte()
        if (blocks.appearance.skullIcon == skullIcon) {
            return
        }
        blocks.appearance.skullIcon = skullIcon
        flagAppearance()
    }

    public fun setOverheadIcon(icon: Int) {
        verify {
            require(icon == -1 || icon in UNSIGNED_BYTE_RANGE) {
                "Unexpected overheadIcon $icon, expected value -1 or in range $UNSIGNED_BYTE_RANGE"
            }
        }
        val overheadIcon = icon.toUByte()
        if (blocks.appearance.overheadIcon == overheadIcon) {
            return
        }
        blocks.appearance.overheadIcon = overheadIcon
        flagAppearance()
    }

    public fun transformToNpc(id: Int) {
        verify {
            require(id == -1 || id in UNSIGNED_SHORT_RANGE) {
                "Unexpected id $id, expected value -1 or in range $UNSIGNED_SHORT_RANGE"
            }
        }
        val npcId = id.toUShort()
        if (blocks.appearance.transformedNpcId == npcId) {
            return
        }
        blocks.appearance.transformedNpcId = npcId
        flagAppearance()
    }

    public fun setIdentKit(
        wearpos: Int,
        value: Int,
    ) {
        verify {
            require(wearpos in 0..11) {
                "Unexpected wearPos $wearpos, expected range 0..11"
            }
            require(value in UNSIGNED_BYTE_RANGE) {
                "Unexpected value $value, expected range $UNSIGNED_BYTE_RANGE"
            }
        }
        val valueAsShort = value.toShort()
        val cur = blocks.appearance.identKit[wearpos]
        if (cur == valueAsShort) {
            return
        }
        blocks.appearance.identKit[wearpos] = valueAsShort
        flagAppearance()
    }

    public fun setWornObj(
        wearpos: Int,
        id: Int,
        wearpos2: Int,
        wearpos3: Int,
    ) {
        verify {
            require(wearpos in 0..11) {
                "Unexpected wearPos $wearpos, expected range 0..11"
            }
            require(id == -1 || id in UNSIGNED_SHORT_RANGE) {
                "Unexpected id $id, expected value -1 or range $UNSIGNED_SHORT_RANGE"
            }
            require(wearpos2 == -1 || wearpos2 in 0..11) {
                "Unexpected wearpos2 $wearpos2, expected value -1 or in range 0..11"
            }
            require(wearpos3 == -1 || wearpos3 in 0..11) {
                "Unexpected wearpos3 $wearpos3, expected value -1 or in range 0..11"
            }
        }
        val valueAsShort = id.toShort()
        val cur = blocks.appearance.wornObjs[wearpos]
        if (cur == valueAsShort) {
            return
        }
        blocks.appearance.wornObjs[wearpos] = valueAsShort
        val hiddenSlotsBitpacked = (wearpos2 and 0xF shl 4) or wearpos3 and 0xF
        blocks.appearance.hiddenWearPos[wearpos] = hiddenSlotsBitpacked.toByte()
        flagAppearance()
    }

    public fun setColour(
        slot: Int,
        value: Int,
    ) {
        verify {
            require(slot in 0..<5) {
                "Unexpected slot $slot, expected range 0..<5"
            }
            require(value in UNSIGNED_BYTE_RANGE) {
                "Unexpected value $value, expected range $UNSIGNED_BYTE_RANGE"
            }
        }
        val valueAsByte = value.toByte()
        val cur = blocks.appearance.colours[slot]
        if (cur == valueAsByte) {
            return
        }
        blocks.appearance.colours[slot] = valueAsByte
        flagAppearance()
    }

    public fun setBaseAnimationSet(
        readyAnim: Int,
        turnAnim: Int,
        walkAnim: Int,
        walkAnimBack: Int,
        walkAnimLeft: Int,
        walkAnimRight: Int,
        runAnim: Int,
    ) {
        verify {
            require(readyAnim == -1 || readyAnim in UNSIGNED_SHORT_RANGE) {
                "Unexpected readyAnim $readyAnim, expected value -1 or range $UNSIGNED_SHORT_RANGE"
            }
            require(turnAnim == -1 || turnAnim in UNSIGNED_SHORT_RANGE) {
                "Unexpected turnAnim $turnAnim, expected value -1 or range $UNSIGNED_SHORT_RANGE"
            }
            require(walkAnim == -1 || walkAnim in UNSIGNED_SHORT_RANGE) {
                "Unexpected walkAnim $walkAnim, expected value -1 or range $UNSIGNED_SHORT_RANGE"
            }
            require(walkAnimBack == -1 || walkAnimBack in UNSIGNED_SHORT_RANGE) {
                "Unexpected walkAnimBack $walkAnimBack, expected value -1 or range $UNSIGNED_SHORT_RANGE"
            }
            require(walkAnimLeft == -1 || walkAnimLeft in UNSIGNED_SHORT_RANGE) {
                "Unexpected walkAnimLeft $walkAnimLeft, expected value -1 or range $UNSIGNED_SHORT_RANGE"
            }
            require(walkAnimRight == -1 || walkAnimRight in UNSIGNED_SHORT_RANGE) {
                "Unexpected walkAnimRight $walkAnimRight, expected value -1 or range $UNSIGNED_SHORT_RANGE"
            }
            require(runAnim == -1 || runAnim in UNSIGNED_SHORT_RANGE) {
                "Unexpected runAnim $runAnim, expected value -1 or range $UNSIGNED_SHORT_RANGE"
            }
        }
        blocks.appearance.readyAnim = readyAnim.toUShort()
        blocks.appearance.turnAnim = turnAnim.toUShort()
        blocks.appearance.walkAnim = walkAnim.toUShort()
        blocks.appearance.walkAnimBack = walkAnimBack.toUShort()
        blocks.appearance.walkAnimLeft = walkAnimLeft.toUShort()
        blocks.appearance.walkAnimRight = walkAnimRight.toUShort()
        blocks.appearance.runAnim = runAnim.toUShort()
        flagAppearance()
    }

    public fun nameExtras(
        beforeName: String,
        afterName: String,
        afterCombatLevel: String,
    ) {
        verify {
            require(beforeName.length in UNSIGNED_BYTE_RANGE) {
                "Unexpected beforeName length ${beforeName.length}, expected range $UNSIGNED_BYTE_RANGE"
            }
            require(afterName.length in UNSIGNED_BYTE_RANGE) {
                "Unexpected afterName length ${afterName.length}, expected range $UNSIGNED_BYTE_RANGE"
            }
            require(afterCombatLevel.length in UNSIGNED_BYTE_RANGE) {
                "Unexpected afterCombatLevel length ${afterCombatLevel.length}, expected range $UNSIGNED_BYTE_RANGE"
            }
        }
        blocks.appearance.beforeName = beforeName
        blocks.appearance.afterName = afterName
        blocks.appearance.afterCombatLevel = afterCombatLevel
        flagAppearance()
    }

    public fun clearObjTypeCustomisation(wearpos: Int) {
        verify {
            require(wearpos in 0..11) {
                "Unexpected wearpos $wearpos, expected range 0..11"
            }
        }
        if (blocks.appearance.objTypeCustomisation[wearpos] == null) {
            return
        }
        blocks.appearance.objTypeCustomisation[wearpos] = null
        flagAppearance()
    }

    private fun allocObjCustomisation(wearpos: Int): ObjTypeCustomisation {
        var customisation = blocks.appearance.objTypeCustomisation[wearpos]
        if (customisation == null) {
            customisation = ObjTypeCustomisation()
            blocks.appearance.objTypeCustomisation[wearpos] = customisation
        }
        return customisation
    }

    public fun objRecol1(
        wearpos: Int,
        index: Int,
        value: Int,
    ) {
        verify {
            require(wearpos in 0..11) {
                "Unexpected wearpos $wearpos, expected range 0..11"
            }
            require(index in 0..14) {
                "Unexpected recol index $index, expected range 0..14"
            }
            require(value in UNSIGNED_SHORT_RANGE) {
                "Unexpected value $value, expected range $UNSIGNED_SHORT_RANGE"
            }
        }
        val customisation = allocObjCustomisation(wearpos)
        customisation.recolIndices = ((customisation.recolIndices.toInt() and 0xF0) or (index and 0xF)).toUByte()
        customisation.recol1 = value.toUShort()
        flagAppearance()
    }

    public fun objRecol2(
        wearpos: Int,
        index: Int,
        value: Int,
    ) {
        verify {
            require(wearpos in 0..11) {
                "Unexpected wearpos $wearpos, expected range 0..11"
            }
            require(index in 0..14) {
                "Unexpected recol index $index, expected range 0..14"
            }
            require(value in UNSIGNED_SHORT_RANGE) {
                "Unexpected value $value, expected range $UNSIGNED_SHORT_RANGE"
            }
        }
        val customisation = allocObjCustomisation(wearpos)
        customisation.recolIndices = ((customisation.recolIndices.toInt() and 0xF) or ((index and 0xF) shl 4)).toUByte()
        customisation.recol2 = value.toUShort()
        flagAppearance()
    }

    public fun objRetex1(
        wearpos: Int,
        index: Int,
        value: Int,
    ) {
        verify {
            require(wearpos in 0..11) {
                "Unexpected wearpos $wearpos, expected range 0..11"
            }
            require(index in 0..14) {
                "Unexpected retex index $index, expected range 0..14"
            }
            require(value in UNSIGNED_SHORT_RANGE) {
                "Unexpected value $value, expected range $UNSIGNED_SHORT_RANGE"
            }
        }
        val customisation = allocObjCustomisation(wearpos)
        customisation.retexIndices = ((customisation.retexIndices.toInt() and 0xF0) or (index and 0xF)).toUByte()
        customisation.retex1 = value.toUShort()
        flagAppearance()
    }

    public fun objRetex2(
        wearpos: Int,
        index: Int,
        value: Int,
    ) {
        verify {
            require(wearpos in 0..11) {
                "Unexpected wearpos $wearpos, expected range 0..11"
            }
            require(index in 0..14) {
                "Unexpected retex index $index, expected range 0..14"
            }
            require(value in UNSIGNED_SHORT_RANGE) {
                "Unexpected value $value, expected range $UNSIGNED_SHORT_RANGE"
            }
        }
        val customisation = allocObjCustomisation(wearpos)
        customisation.retexIndices = ((customisation.retexIndices.toInt() and 0xF) or ((index and 0xF) shl 4)).toUByte()
        customisation.retex2 = value.toUShort()
        flagAppearance()
    }

    private fun flagAppearance() {
        flags = flags or APPEARANCE
        appearanceChangesCounter++
    }

    internal fun postUpdate() {
        clearTransientExtendedInformation()
        flags = 0
    }

    internal fun reset() {
        flags = 0
        this.appearanceChangesCounter = 0
        this.otherAppearanceChangesCounter.fill(0)
        blocks.appearance.clear()
        blocks.moveSpeed.clear()
        blocks.temporaryMoveSpeed.clear()
        blocks.sequence.clear()
        blocks.facePathingEntity.clear()
        blocks.faceAngle.clear()
        blocks.say.clear()
        blocks.chat.clear()
        blocks.exactMove.clear()
        blocks.spotAnims.clear()
        blocks.hit.clear()
        blocks.tinting.clear()
    }

    internal fun getLowToHighResChangeExtendedInfoFlags(observer: PlayerAvatarExtendedInfo): Int {
        var flag = 0
        if (this.flags and APPEARANCE == 0 &&
            checkOutOfDate(observer)
        ) {
            flag = flag or APPEARANCE
        }
        if (this.flags and MOVE_SPEED == 0 &&
            blocks.moveSpeed.value != MoveSpeed.DEFAULT_MOVESPEED
        ) {
            flag = flag or MOVE_SPEED
        }
        if (this.flags and FACE_PATHINGENTITY == 0 &&
            blocks.facePathingEntity.index != FacePathingEntity.DEFAULT_VALUE
        ) {
            flag = flag or FACE_PATHINGENTITY
        }
        return flag
    }

    private fun checkOutOfDate(observer: PlayerAvatarExtendedInfo): Boolean {
        return observer.otherAppearanceChangesCounter[localIndex] != appearanceChangesCounter
    }

    internal fun precompute() {
        // Hits and tinting do not get precomputed
        if (flags and APPEARANCE != 0) {
            blocks.appearance.precompute()
        }
        if (flags and TEMP_MOVE_SPEED != 0) {
            blocks.temporaryMoveSpeed.precompute()
        }
        if (flags and SEQUENCE != 0) {
            blocks.sequence.precompute()
        }
        if (flags and FACE_ANGLE != 0) {
            blocks.faceAngle.precompute()
        }
        if (flags and SAY != 0) {
            blocks.say.precompute()
        }
        if (flags and CHAT != 0) {
            blocks.chat.precompute()
        }
        if (flags and EXACT_MOVE != 0) {
            blocks.exactMove.precompute()
        }
        if (flags and SPOTANIM != 0) {
            blocks.spotAnims.precompute()
        }
    }

    internal fun pExtendedInfo(
        platformType: PlatformType,
        buffer: JagByteBuf,
        observerFlag: Int,
        observer: PlayerAvatarExtendedInfo,
        remainingAvatars: Int,
    ) {
        val flag = this.flags or observerFlag
        if (!filter.accept(
                buffer.writableBytes(),
                flag,
                remainingAvatars,
                observer.otherAppearanceChangesCounter[localIndex] != 0,
            )
        ) {
            buffer.p1(0)
            return
        }
        val writer =
            requireNotNull(writers[platformType.id]) {
                "Extended info writer missing for platform $platformType"
            }

        // If appearance is flagged, ensure we synchronize the changes counter
        if (flag and APPEARANCE != 0) {
            observer.otherAppearanceChangesCounter[localIndex] = appearanceChangesCounter
        }
        writer.pExtendedInfo(
            buffer,
            localIndex,
            observer.localIndex,
            flag,
            blocks,
        )
    }

    private fun clearTransientExtendedInformation() {
        if (flags and TEMP_MOVE_SPEED != 0) {
            blocks.temporaryMoveSpeed.clear()
        }
        if (flags and SEQUENCE != 0) {
            blocks.sequence.clear()
        }
        if (flags and FACE_ANGLE != 0) {
            blocks.faceAngle.clear()
        }
        if (flags and SAY != 0) {
            blocks.say.clear()
        }
        if (flags and CHAT != 0) {
            blocks.chat.clear()
        }
        if (flags and EXACT_MOVE != 0) {
            blocks.exactMove.clear()
        }
        if (flags and SPOTANIM != 0) {
            blocks.spotAnims.clear()
        }
        if (flags and HITS != 0) {
            blocks.hit.clear()
        }
        if (flags and TINTING != 0) {
            blocks.tinting.clear()
        }
    }

    public companion object {
        // Observer-dependent flags, utilizing the lowest bits as we store observer flags in a byte array
        public const val APPEARANCE: Int = 0x1
        public const val MOVE_SPEED: Int = 0x2
        public const val FACE_PATHINGENTITY: Int = 0x4
        public const val TINTING: Int = 0x8

        // "Static" flags, the bit values here are irrelevant
        public const val FACE_ANGLE: Int = 0x10
        public const val SAY: Int = 0x20
        public const val HITS: Int = 0x40
        public const val SEQUENCE: Int = 0x80
        public const val CHAT: Int = 0x100
        public const val TEMP_MOVE_SPEED: Int = 0x200
        public const val EXACT_MOVE: Int = 0x400
        public const val SPOTANIM: Int = 0x800

        private val SIGNED_BYTE_RANGE: IntRange = Byte.MIN_VALUE.toInt()..Byte.MAX_VALUE.toInt()
        private val UNSIGNED_BYTE_RANGE: IntRange = UByte.MIN_VALUE.toInt()..UByte.MAX_VALUE.toInt()
        private val UNSIGNED_SHORT_RANGE: IntRange = UShort.MIN_VALUE.toInt()..UShort.MAX_VALUE.toInt()
        private val UNSIGNED_SMART_1_OR_2_RANGE: IntRange = 0..0x7FFF

        private val logger = InlineLogger()
        public val inputVerification: Boolean =
            SystemPropertyUtil.getBoolean(
                "net.rsprot.protocol.game.outgoing.info.playerinfo.inputVerification",
                true,
            )

        init {
            logger.debug {
                "-Dnet.rsprot.protocol.game.outgoing.info.playerinfo.inputVerification: $inputVerification"
            }
        }

        private inline fun verify(crossinline block: () -> Unit) {
            if (inputVerification) {
                block()
            }
        }

        private fun buildPlatformWriterArray(
            extendedInfoWriters: List<AvatarExtendedInfoWriter>,
        ): Array<AvatarExtendedInfoWriter?> {
            val array = arrayOfNulls<AvatarExtendedInfoWriter>(PlatformType.COUNT)
            for (writer in extendedInfoWriters) {
                array[writer.platformType.id] = writer
            }
            return array
        }
    }
}
