package net.rsprot.protocol.game.outgoing.info.playerinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.internal.game.outgoing.info.ExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.ExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.encoder.ExtendedInfoEncoders
import net.rsprot.protocol.internal.game.outgoing.info.encoder.OnDemandExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.Appearance
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.Chat
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.FaceAngle
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.MoveSpeed
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.ObjTypeCustomisation
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.TemporaryMoveSpeed
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.ExactMove
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.FacePathingEntity
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.Hit
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.Say
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.Sequence
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.SpotAnimList
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.Tinting
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.TintingList
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util.HeadBar
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util.HitMark
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util.SpotAnim
import net.rsprot.protocol.shared.platform.PlatformType

// TODO: Optional bound checks (hits, etc)
@Suppress("MemberVisibilityCanBePrivate")
public class PlayerAvatarExtendedInfo(
    private val protocol: PlayerInfoProtocol,
    private val localIndex: Int,
    extendedInfoEncoders: Map<PlatformType, ExtendedInfoEncoders>,
    allocator: ByteBufAllocator,
    huffmanCodec: HuffmanCodec,
) {
    private val appearance: Appearance =
        Appearance(
            buildPlatformEncoderArray(extendedInfoEncoders, ExtendedInfoEncoders::appearance),
            allocator,
            huffmanCodec,
        )
    private val moveSpeed: MoveSpeed =
        MoveSpeed(
            buildPlatformEncoderArray(extendedInfoEncoders, ExtendedInfoEncoders::moveSpeed),
            allocator,
            huffmanCodec,
        )
    private val temporaryMoveSpeed: TemporaryMoveSpeed =
        TemporaryMoveSpeed(
            buildPlatformEncoderArray(extendedInfoEncoders, ExtendedInfoEncoders::temporaryMoveSpeed),
            allocator,
            huffmanCodec,
        )
    private val sequence: Sequence =
        Sequence(
            buildPlatformEncoderArray(extendedInfoEncoders, ExtendedInfoEncoders::sequence),
            allocator,
            huffmanCodec,
        )
    private val facePathingEntity: FacePathingEntity =
        FacePathingEntity(
            buildPlatformEncoderArray(extendedInfoEncoders, ExtendedInfoEncoders::facePathingEntity),
            allocator,
            huffmanCodec,
        )
    private val faceAngle: FaceAngle =
        FaceAngle(
            buildPlatformEncoderArray(extendedInfoEncoders, ExtendedInfoEncoders::faceAngle),
            allocator,
            huffmanCodec,
        )
    private val say: Say =
        Say(
            buildPlatformEncoderArray(extendedInfoEncoders, ExtendedInfoEncoders::say),
            allocator,
            huffmanCodec,
        )
    private val chat: Chat =
        Chat(
            buildPlatformEncoderArray(extendedInfoEncoders, ExtendedInfoEncoders::chat),
            allocator,
            huffmanCodec,
        )
    private val exactMove: ExactMove =
        ExactMove(
            buildPlatformEncoderArray(extendedInfoEncoders, ExtendedInfoEncoders::exactMove),
            allocator,
            huffmanCodec,
        )
    private val spotAnims: SpotAnimList =
        SpotAnimList(
            buildPlatformEncoderArray(extendedInfoEncoders, ExtendedInfoEncoders::spotAnim),
            allocator,
            huffmanCodec,
        )
    private val hit: Hit =
        Hit(
            buildPlatformEncoderArray(extendedInfoEncoders, ExtendedInfoEncoders::hit),
        )
    private val tinting: TintingList =
        TintingList(
            buildPlatformEncoderArray(extendedInfoEncoders, ExtendedInfoEncoders::tinting),
        )

    internal var flags: Int = 0

    public fun setMoveSpeed(value: Int) {
        moveSpeed.value = value
        flags = flags or MOVE_SPEED
    }

    public fun setTempMoveSpeed(value: Int) {
        temporaryMoveSpeed.value = value
        flags = flags or TEMP_MOVE_SPEED
    }

    public fun setSequence(
        id: Int,
        delay: Int,
    ) {
        sequence.id = id.toUShort()
        sequence.delay = delay.toUShort()
        flags = flags or SEQUENCE
    }

    public fun setFacePathingEntity(index: Int) {
        facePathingEntity.index = index
        flags = flags or FACE_PATHINGENTITY
    }

    public fun setFaceAngle(angle: Int) {
        faceAngle.angle = angle
        flags = flags or FACE_ANGLE
    }

    public fun setSay(text: String) {
        say.text = text
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
        val patternLength = if (colour in 13..20) colour - 12 else 0
        if (patternLength in 1..8) {
            requireNotNull(pattern) {
                "Pattern cannot be null if pattern length is defined."
            }
            require(pattern.size == patternLength) {
                "Pattern length does not match the size configured in the colour property."
            }
        }
        chat.colour = colour.toUByte()
        chat.effects = effects.toUByte()
        chat.modicon = modicon.toUByte()
        chat.autotyper = autotyper
        chat.text = text
        chat.pattern = pattern
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
        exactMove.deltaX1 = deltaX1.toUByte()
        exactMove.deltaZ1 = deltaZ1.toUByte()
        exactMove.delay1 = delay1.toUShort()
        exactMove.deltaX2 = deltaX2.toUByte()
        exactMove.deltaZ2 = deltaZ2.toUByte()
        exactMove.delay2 = delay2.toUShort()
        exactMove.direction = direction.toUShort()
        flags = flags or EXACT_MOVE
    }

    public fun setSpotAnim(
        slot: Int,
        id: Int,
        delay: Int,
        height: Int,
    ) {
        spotAnims.set(slot, SpotAnim(id, delay, height))
        flags = flags or SPOTANIM
    }

    public fun addHitMark(
        sourceIndex: Int,
        selfType: Int,
        otherType: Int = selfType,
        value: Int,
        delay: Int = 0,
    ) {
        hit.hitMarkList +=
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
        hit.hitMarkList += HitMark(0x7FFEu, delay.toUShort())
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
        hit.hitMarkList +=
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
        hit.headBarList +=
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
        luminance: Int,
        opacity: Int,
        visibleToIndex: Int = -1,
    ) {
        if (visibleToIndex != -1) {
            val otherPlayerInfo = protocol.getPlayerInfo(visibleToIndex)
            requireNotNull(otherPlayerInfo) {
                "Player at index $visibleToIndex does not exist."
            }
            val tint = Tinting()
            this.tinting.observerDependent[visibleToIndex] = tint
            tint.start = startTime.toUShort()
            tint.end = endTime.toUShort()
            tint.hue = hue.toUByte()
            tint.saturation = saturation.toUByte()
            tint.lightness = luminance.toUByte()
            tint.weight = opacity.toUByte()
            otherPlayerInfo.observerExtendedInfoFlags.addFlag(localIndex, TINTING)
        } else {
            val tint = this.tinting.global
            tint.start = startTime.toUShort()
            tint.end = endTime.toUShort()
            tint.hue = hue.toUByte()
            tint.saturation = saturation.toUByte()
            tint.lightness = luminance.toUByte()
            tint.weight = opacity.toUByte()
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
        appearance.name = name
        appearance.combatLevel = combatLevel.toUByte()
        appearance.skillLevel = skillLevel.toUShort()
        appearance.hidden = hidden
        appearance.male = male
        appearance.textGender = textGender.toUByte()
        appearance.skullIcon = skullIcon.toUByte()
        appearance.overheadIcon = overheadIcon.toUByte()
        flagAppearance()
    }

    public fun setName(name: String) {
        if (appearance.name == name) {
            return
        }
        appearance.name = name
        flagAppearance()
    }

    public fun setCombatLevel(combatLevel: Int) {
        val level = combatLevel.toUByte()
        if (appearance.combatLevel == level) {
            return
        }
        appearance.combatLevel = level
        flagAppearance()
    }

    public fun setSkillLevel(skillLevel: Int) {
        val level = skillLevel.toUShort()
        if (appearance.skillLevel == level) {
            return
        }
        appearance.skillLevel = level
        flagAppearance()
    }

    public fun setHidden(hidden: Boolean) {
        if (appearance.hidden == hidden) {
            return
        }
        appearance.hidden = hidden
        flagAppearance()
    }

    public fun setMale(isMale: Boolean) {
        if (appearance.male == isMale) {
            return
        }
        appearance.male = isMale
        flagAppearance()
    }

    public fun setTextGender(num: Int) {
        val textGender = num.toUByte()
        if (appearance.textGender == textGender) {
            return
        }
        appearance.textGender = textGender
        flagAppearance()
    }

    public fun setSkullIcon(icon: Int) {
        val skullIcon = icon.toUByte()
        if (appearance.skullIcon == skullIcon) {
            return
        }
        appearance.skullIcon = skullIcon
        flagAppearance()
    }

    public fun setOverheadIcon(icon: Int) {
        val overheadIcon = icon.toUByte()
        if (appearance.overheadIcon == overheadIcon) {
            return
        }
        appearance.overheadIcon = overheadIcon
        flagAppearance()
    }

    public fun transformToNpc(id: Int) {
        val npcId = id.toUShort()
        if (appearance.transformedNpcId == npcId) {
            return
        }
        appearance.transformedNpcId = npcId
        flagAppearance()
    }

    public fun setIdentKit(
        wearPos: Int,
        value: Int,
    ) {
        val valueAsShort = value.toShort()
        val cur = appearance.identKit[wearPos]
        if (cur == valueAsShort) {
            return
        }
        appearance.identKit[wearPos] = valueAsShort
        flagAppearance()
    }

    public fun setWornObj(
        wearpos: Int,
        id: Int,
        wearpos2: Int,
        wearpos3: Int,
    ) {
        val valueAsShort = id.toShort()
        val cur = appearance.wornObjs[wearpos]
        if (cur == valueAsShort) {
            return
        }
        appearance.wornObjs[wearpos] = valueAsShort
        val hiddenSlotsBitpacked = (wearpos2 and 0xF shl 4) or wearpos3 and 0xF
        appearance.hiddenWearPos[wearpos] = hiddenSlotsBitpacked.toByte()
        flagAppearance()
    }

    public fun setColour(
        slot: Int,
        value: Int,
    ) {
        val valueAsByte = value.toByte()
        val cur = appearance.colours[slot]
        if (cur == valueAsByte) {
            return
        }
        appearance.colours[slot] = valueAsByte
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
        appearance.readyAnim = readyAnim.toUShort()
        appearance.turnAnim = turnAnim.toUShort()
        appearance.walkAnim = walkAnim.toUShort()
        appearance.walkAnimBack = walkAnimBack.toUShort()
        appearance.walkAnimLeft = walkAnimLeft.toUShort()
        appearance.walkAnimRight = walkAnimRight.toUShort()
        appearance.runAnim = runAnim.toUShort()
        flagAppearance()
    }

    public fun nameExtras(
        beforeName: String,
        afterName: String,
        afterCombatLevel: String,
    ) {
        appearance.beforeName = beforeName
        appearance.afterName = afterName
        appearance.afterCombatLevel = afterCombatLevel
        flagAppearance()
    }

    public fun clearObjTypeCustomisation(wearpos: Int) {
        if (appearance.objTypeCustomisation[wearpos] == null) {
            return
        }
        appearance.objTypeCustomisation[wearpos] = null
        flagAppearance()
    }

    private fun allocObjCustomisation(wearpos: Int): ObjTypeCustomisation {
        var customisation = appearance.objTypeCustomisation[wearpos]
        if (customisation == null) {
            customisation = ObjTypeCustomisation()
            appearance.objTypeCustomisation[wearpos] = customisation
        }
        return customisation
    }

    public fun objRecol1(
        wearpos: Int,
        index: Int,
        value: Int,
    ) {
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
        val customisation = allocObjCustomisation(wearpos)
        customisation.retexIndices = ((customisation.retexIndices.toInt() and 0xF) or ((index and 0xF) shl 4)).toUByte()
        customisation.retex2 = value.toUShort()
        flagAppearance()
    }

    private fun flagAppearance() {
        flags = flags or APPEARANCE
        appearance.changeCounter++
    }

    internal fun postUpdate() {
        clearTransientExtendedInformation()
        flags = 0
    }

    internal fun reset() {
        flags = 0
        appearance.clear()
        moveSpeed.clear()
        temporaryMoveSpeed.clear()
        sequence.clear()
        facePathingEntity.clear()
        faceAngle.clear()
        say.clear()
        chat.clear()
        exactMove.clear()
        spotAnims.clear()
        hit.clear()
        tinting.clear()
    }

    internal fun getLowToHighResChangeExtendedInfoFlags(observer: PlayerAvatarExtendedInfo): Int {
        var flag = 0
        if (checkOutOfDate(observer)) {
            flag = flag or APPEARANCE
        }
        if (moveSpeed.value != MoveSpeed.DEFAULT_MOVESPEED) {
            flag = flag or MOVE_SPEED
        }
        if (facePathingEntity.index != FacePathingEntity.DEFAULT_VALUE) {
            flag = flag or FACE_PATHINGENTITY
        }
        return flag
    }

    private fun checkOutOfDate(observer: PlayerAvatarExtendedInfo): Boolean {
        val isOutOfDate = observer.appearance.otherChangesCounter[localIndex] != appearance.changeCounter
        if (isOutOfDate) {
            observer.appearance.otherChangesCounter[localIndex] = appearance.changeCounter
        }
        return isOutOfDate
    }

    internal fun precompute() {
        // Hits and tinting do not get precomputed
        if (flags and APPEARANCE != 0) {
            appearance.precompute()
        }
        if (flags and TEMP_MOVE_SPEED != 0) {
            temporaryMoveSpeed.precompute()
        }
        if (flags and SEQUENCE != 0) {
            sequence.precompute()
        }
        if (flags and FACE_ANGLE != 0) {
            faceAngle.precompute()
        }
        if (flags and SAY != 0) {
            say.precompute()
        }
        if (flags and CHAT != 0) {
            chat.precompute()
        }
        if (flags and EXACT_MOVE != 0) {
            exactMove.precompute()
        }
        if (flags and SPOTANIM != 0) {
            spotAnims.precompute()
        }
    }

    internal fun pExtendedInfo(
        platformType: PlatformType,
        buffer: JagByteBuf,
        observerFlag: Int,
        observerIndex: Int,
    ) {
        // TODO: Figure out a way to cap this out
        if (buffer.writerIndex() >= 35_000) {
            buffer.p1(0)
            return
        }
        var flag = this.flags or observerFlag
        if (flag and 0xFF.inv() != 0) flag = flag or EXTENDED_SHORT
        if (flag and 0xFFFF.inv() != 0) flag = flag or EXTENDED_MEDIUM
        buffer.p1(flag)
        if (flag and EXTENDED_SHORT != 0) {
            buffer.p1(flag shr 8)
        }
        if (flag and EXTENDED_MEDIUM != 0) {
            buffer.p1(flag shr 16)
        }

        if (flag and FACE_ANGLE != 0) {
            pCachedData(platformType, buffer, faceAngle)
        }
        // Old chat
        if (flag and SEQUENCE != 0) {
            pCachedData(platformType, buffer, sequence)
        }
        if (flag and HITS != 0) {
            pOnDemandData(platformType, buffer, hit, observerIndex)
        }
        if (flag and EXACT_MOVE != 0) {
            pCachedData(platformType, buffer, exactMove)
        }
        if (flag and CHAT != 0) {
            pCachedData(platformType, buffer, chat)
        }
        if (flag and TEMP_MOVE_SPEED != 0) {
            pCachedData(platformType, buffer, temporaryMoveSpeed)
        }
        // name extras
        if (flag and SAY != 0) {
            pCachedData(platformType, buffer, say)
        }
        if (flag and TINTING != 0) {
            pOnDemandData(platformType, buffer, tinting, observerIndex)
        }
        if (flag and MOVE_SPEED != 0) {
            pCachedData(platformType, buffer, moveSpeed)
        }
        if (flag and APPEARANCE != 0) {
            pCachedData(platformType, buffer, appearance)
        }
        if (flag and FACE_PATHINGENTITY != 0) {
            pCachedData(platformType, buffer, facePathingEntity)
        }
        if (flag and SPOTANIM != 0) {
            pCachedData(platformType, buffer, spotAnims)
        }
    }

    private fun pCachedData(
        platformType: PlatformType,
        buffer: JagByteBuf,
        block: ExtendedInfo<*, *>,
    ) {
        val precomputed =
            checkNotNull(block.getBuffer(platformType)) {
                "Buffer has not been computed on platform $platformType, ${block.javaClass.name}"
            }
        buffer.buffer.writeBytes(precomputed, precomputed.readerIndex(), precomputed.readableBytes())
    }

    private fun <T : ExtendedInfo<T, E>, E : OnDemandExtendedInfoEncoder<T>> pOnDemandData(
        platformType: PlatformType,
        buffer: JagByteBuf,
        block: T,
        observerIndex: Int,
    ) {
        val encoder =
            checkNotNull(block.getEncoder(platformType)) {
                "Encoder has not been set for platform $platformType"
            }
        encoder.encode(
            buffer,
            observerIndex,
            localIndex,
            block,
        )
    }

    private fun clearTransientExtendedInformation() {
        if (flags and TEMP_MOVE_SPEED != 0) {
            temporaryMoveSpeed.clear()
        }
        if (flags and SEQUENCE != 0) {
            sequence.clear()
        }
        if (flags and FACE_ANGLE != 0) {
            faceAngle.clear()
        }
        if (flags and SAY != 0) {
            say.clear()
        }
        if (flags and CHAT != 0) {
            chat.clear()
        }
        if (flags and EXACT_MOVE != 0) {
            exactMove.clear()
        }
        if (flags and SPOTANIM != 0) {
            spotAnims.clear()
        }
        if (flags and HITS != 0) {
            hit.clear()
        }
        if (flags and TINTING != 0) {
            tinting.clear()
        }
    }

    public companion object {
        internal const val EXTENDED_SHORT = 0x1
        internal const val FACE_ANGLE = 0x2
        internal const val APPEARANCE = 0x4
        internal const val SAY = 0x8

        @Suppress("unused")
        internal const val CHAT_OLD = 0x10
        internal const val HITS = 0x20
        internal const val FACE_PATHINGENTITY = 0x40
        internal const val SEQUENCE = 0x80
        internal const val MOVE_SPEED = 0x200
        internal const val CHAT = 0x400
        internal const val EXTENDED_MEDIUM = 0x800
        internal const val TEMP_MOVE_SPEED = 0x1000
        internal const val TINTING = 0x2000
        internal const val EXACT_MOVE = 0x4000
        internal const val SPOTANIM = 0x10000

        // Name extras are part of appearance nowadays, and thus will not be used on their own
        @Suppress("unused")
        internal const val NAME_EXTRAS = 0x100

        private inline fun <T : ExtendedInfo<T, E>, reified E : ExtendedInfoEncoder<T>> buildPlatformEncoderArray(
            allEncoders: Map<PlatformType, ExtendedInfoEncoders>,
            selector: (ExtendedInfoEncoders) -> E,
        ): Array<E?> {
            val array = arrayOfNulls<E>(PlatformType.COUNT)
            for ((platform, encoders) in allEncoders) {
                val encoder = selector(encoders)
                array[platform.id] = encoder
            }
            return array
        }
    }
}
