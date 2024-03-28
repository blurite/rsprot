package net.rsprot.protocol.game.outgoing.info.playerinfo

import net.rsprot.protocol.internal.game.outgoing.info.CachedExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.Appearance
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.Chat
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.FaceAngle
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.MoveSpeed
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.TemporaryMoveSpeed
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.util.NpcBodyType
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.util.PlayerBodyType
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

// TODO: Optional bound checks (hits, etc)
public class PlayerAvatarExtendedInfo(
    capacity: Int,
    private val protocol: PlayerInfoProtocol,
    private val localIndex: Int,
) {
    private val appearance: Appearance = Appearance(capacity)
    private val moveSpeed: MoveSpeed = MoveSpeed()
    private val temporaryMoveSpeed: TemporaryMoveSpeed = TemporaryMoveSpeed()
    private val sequence: Sequence = Sequence()
    private val facePathingEntity: FacePathingEntity = FacePathingEntity()
    private val faceAngle: FaceAngle = FaceAngle()
    private val say: Say = Say()
    private val chat: Chat = Chat()
    private val exactMove: ExactMove = ExactMove()
    private val spotAnims: SpotAnimList = SpotAnimList()
    private val hit: Hit = Hit()
    private val tinting: TintingList = TintingList()

    internal var flags: Int = 0
    internal var lastObservations: IntArray = IntArray(capacity)

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
        effects: Int,
        modicon: Int,
        autotyper: Boolean,
        text: String,
        pattern: ByteArray?,
    ) {
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
            tint.startTime = startTime.toUShort()
            tint.endTime = endTime.toUShort()
            tint.hue = hue.toUByte()
            tint.saturation = saturation.toUByte()
            tint.luminance = luminance.toUByte()
            tint.opacity = opacity.toUByte()
            otherPlayerInfo.observerExtendedInfoFlags.addFlag(localIndex, TINTING)
        } else {
            val tint = this.tinting.global
            tint.startTime = startTime.toUShort()
            tint.endTime = endTime.toUShort()
            tint.hue = hue.toUByte()
            tint.saturation = saturation.toUByte()
            tint.luminance = luminance.toUByte()
            tint.opacity = opacity.toUByte()
            flags = flags or TINTING
        }
    }

    // TODO: Implement appearance in full
    public fun setAppearanceDetails(
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
        flags = flags or APPEARANCE
    }

    public fun setPlayerColours(colours: ByteArray) {
        appearance.colours = colours
        flags = flags or APPEARANCE
    }

    public fun setNpcBodyType(npcId: Int) {
        appearance.bodyType = NpcBodyType(npcId)
        flags = flags or APPEARANCE
    }

    public fun setPlayerBodyType(values: ShortArray) {
        appearance.bodyType = PlayerBodyType(values)
        flags = flags or APPEARANCE
    }

    internal fun reset() {
        clearTransientExtendedInformation()
        flags = 0
    }

    internal fun determineCacheMisses(lastObserverUpdate: Int): Int {
        return if (isOutOfDate(lastObserverUpdate, appearance)) {
            APPEARANCE
        } else {
            0
        }
    }

    private fun isOutOfDate(
        lastObserverUpdate: Int,
        info: CachedExtendedInfo,
    ): Boolean {
        return lastObserverUpdate < info.cache[localIndex]
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
        internal const val APPEARANCE = 0x1
        internal const val SEQUENCE = 0x2
        internal const val EXTENDED_SHORT = 0x4
        internal const val HITS = 0x8
        internal const val FACE_PATHINGENTITY = 0x20
        internal const val SAY = 0x40
        internal const val FACE_ANGLE = 0x80
        internal const val TINTING = 0x100
        internal const val MOVE_SPEED = 0x200
        internal const val CHAT = 0x800
        internal const val EXACT_MOVE = 0x1000

        // Name extras are part of appearance nowadays, and thus will not be used on their own
        internal const val NAME_EXTRAS = 0x2000
        internal const val EXTENDED_MEDIUM = 0x4000
        internal const val TEMP_MOVE_SPEED = 0x8000
        internal const val SPOTANIM = 0x10000
    }
}
