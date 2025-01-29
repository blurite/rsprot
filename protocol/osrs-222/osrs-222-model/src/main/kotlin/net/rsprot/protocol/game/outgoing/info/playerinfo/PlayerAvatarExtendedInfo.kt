@file:Suppress("DuplicatedCode")

package net.rsprot.protocol.game.outgoing.info.playerinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.game.outgoing.info.AvatarExtendedInfoWriter
import net.rsprot.protocol.game.outgoing.info.filter.ExtendedInfoFilter
import net.rsprot.protocol.internal.RSProtFlags
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.encoder.PlayerExtendedInfoEncoders
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.FaceAngle
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.MoveSpeed
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.ObjTypeCustomisation
import net.rsprot.protocol.internal.game.outgoing.info.precompute
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.FacePathingEntity
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.Tinting
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util.HeadBar
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util.HitMark
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util.SpotAnim

public typealias PlayerAvatarExtendedInfoWriter =
    AvatarExtendedInfoWriter<PlayerExtendedInfoEncoders, PlayerAvatarExtendedInfoBlocks>

/**
 * This data structure keeps track of all the extended info blocks for a given player avatar.
 *  @param localIndex the index of the avatar who owns this extended info block.
 *  @param filter the filter responsible for ensuring the total packet size constraint
 *  is not broken in any way. If this filter does not conform to the contract correctly,
 *  crashes are likely to happen during encoding.
 *  @param extendedInfoWriters the list of client-specific writers & encoders of all extended
 *  info blocks. During caching procedure, all registered client buffers will be built
 *  concurrently among players.
 *  @param allocator the byte buffer allocator used to allocate buffers during the caching procedure.
 *  Any extended info block which is built on-demand is written directly into the main buffer.
 *  @param huffmanCodec the Huffman codec is used to compress public chat extended info blocks.
 */
public class PlayerAvatarExtendedInfo(
    internal var localIndex: Int,
    private val filter: ExtendedInfoFilter,
    extendedInfoWriters: List<PlayerAvatarExtendedInfoWriter>,
    private val allocator: ByteBufAllocator,
    private val huffmanCodec: HuffmanCodecProvider,
) {
    /**
     * The flags currently enabled for this avatar.
     * When an update is requested, the respective flag of that update is appended
     * onto this flag. At the end of each cycle, the flag is reset.
     * Worth noting, however, that this flag only contains constants within
     * the [Companion] of this class. For client-specific encoders, a translation
     * occurs to turn these constants into a client-specific flag.
     */
    internal var flags: Int = 0

    /**
     * Extended info blocks used to transmit changes to the client,
     * wrapped in its own class as we must pass this onto the client-specific
     * implementations.
     */
    private val blocks: PlayerAvatarExtendedInfoBlocks = PlayerAvatarExtendedInfoBlocks(extendedInfoWriters)

    /**
     * The client-specific extended info writers, indexed by the respective [OldSchoolClientType]'s id.
     * All clients in use must be registered, or an exception will occur during player info encoding.
     */
    private val writers: Array<PlayerAvatarExtendedInfoWriter?> =
        buildClientWriterArray(extendedInfoWriters)

    /**
     * An int array to track the last cycle during which we recorded other players' appearances.
     * If the values align, the client will utilize its previously cached variant.
     */
    private val otherAppearanceChangeCycles: IntArray =
        IntArray(PlayerInfoProtocol.PROTOCOL_CAPACITY) {
            -1
        }

    /**
     * The last player info cycle on which our appearance changed.
     */
    private var lastAppearanceChangeCycle: Int = 0

    /**
     * A storage of all the observed chat messages that a player saw in a tick.
     */
    public val observedChatStorage: ObservedChatStorage =
        ObservedChatStorage(
            RSProtFlags.captureChat,
            RSProtFlags.captureSay,
        )

    /**
     * Invalidates the appearance cache.
     */
    internal fun invalidateAppearanceCache() {
        otherAppearanceChangeCycles.fill(-1)
    }

    /**
     * Sets the movement speed for this avatar. This move speed will be used whenever
     * the player moves, unless a temporary move speed is utilized, which will take priority.
     * The known values are:
     *
     * ```
     * | Type       | Id |
     * |------------|----|
     * | Stationary | -1 |
     * | Crawl      | 0  |
     * | Walk       | 1  |
     * | Run        | 2  |
     * ```
     * @param value the move speed value.
     */
    public fun setMoveSpeed(value: Int) {
        verify {
            require(value in -1..2) {
                "Unexpected move speed: $value, expected values: -1, 0, 1, 2"
            }
        }
        blocks.moveSpeed.value = value
        flags = flags or MOVE_SPEED
    }

    /**
     * Sets the temporary movement speed for this avatar - this move speed will only
     * apply for a single game cycle.
     * The known values are:
     * ```
     * | Type            | Id  |
     * |-----------------|-----|
     * | Stationary      | -1  |
     * | Crawl           |  0  |
     * | Walk            |  1  |
     * | Run             |  2  |
     * | Teleport        | 127 |
     * ```
     * @param value the temporary move speed value.
     */
    public fun setTempMoveSpeed(value: Int) {
        verify {
            require(value in -1..2 || value == 127) {
                "Unexpected temporary move speed: $value, expected values: -1, 0, 1, 2, 127"
            }
        }
        blocks.temporaryMoveSpeed.value = value
        flags = flags or TEMP_MOVE_SPEED
    }

    /**
     * Sets the sequence for this avatar to play.
     * @param id the id of the sequence to play, or -1 to stop playing current sequence.
     * @param delay the delay in client cycles (20ms/cc) until the avatar starts playing this sequence.
     */
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

    /**
     * Sets the face-locking onto the avatar with index [index].
     * If the target avatar is a player, add 0x10000 to the real index value (0-2048).
     * If the target avatar is a NPC, set the index as it is.
     * In order to stop facing an entity, set the index value to -1.
     * @param index the index of the target to face-lock onto (read above)
     */
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

    /**
     * Sets the angle for this avatar to face.
     * @param angle the angle to face, value range is 0..<2048,
     * with 0 implying south, 512 west, 1024 north and 1536 east; interpolate
     * between to get finer directions.
     */
    public fun setFaceAngle(angle: Int) {
        verify {
            require(angle in 0..2047) {
                "Unexpected angle: $angle, expected range: 0-2047"
            }
        }
        blocks.faceAngle.angle = angle.toUShort()
        flags = flags or FACE_ANGLE
    }

    /**
     * Sets the overhead chat of this avatar.
     * If the [text] starts with the character `~`, the message will additionally
     * also be rendered in the chatbox of everyone nearby, although no chat icons
     * will appear alongside. The first `~` character itself will not be rendered
     * in that scenario.
     * @param text the text to render overhead.
     */
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

    /**
     * Sets the public chat of this avatar.
     *
     * Colour table:
     * ```
     * | Id    | Prefix    |          Hex Value         |
     * |-------|-----------|:--------------------------:|
     * | 0     | yellow:   |          0xFFFF00          |
     * | 1     | red:      |          0xFF0000          |
     * | 2     | green:    |          0x00FF00          |
     * | 3     | cyan:     |          0x00FFFF          |
     * | 4     | purple:   |          0xFF00FF          |
     * | 5     | white:    |          0xFFFFFF          |
     * | 6     | flash1:   |      0xFF0000/0xFFFF00     |
     * | 7     | flash2:   |      0x0000FF/0x00FFFF     |
     * | 8     | flash3:   |      0x00B000/0x80FF80     |
     * | 9     | glow1:    | 0xFF0000-0xFFFF00-0x00FFFF |
     * | 10    | glow2:    | 0xFF0000-0x00FF00-0x0000FF |
     * | 11    | glow3:    | 0xFFFFFF-0x00FF00-0x00FFFF |
     * | 12    | rainbow:  |             N/A            |
     * | 13-20 | pattern*: |             N/A            |
     * ```
     *
     * Effects table:
     * ```
     * | Id | Prefix  |
     * |----|---------|
     * | 1  | wave:   |
     * | 2  | wave2:  |
     * | 3  | shake:  |
     * | 4  | scroll: |
     * | 5  | slide:  |
     * ```
     *
     * @param colour the colour id to render (see above)
     * @param effects the effects to apply to the text (see above)
     * @param modicon the index of the sprite in the modicons group to render before the name
     * @param autotyper whether the avatar is using built-in autotyper
     * @param text the text to render overhead and in chat
     * @param pattern the pattern description if the user is using the pattern colour type
     */
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

    /**
     * Sets an exact movement for this avatar. It should be noted
     * that this is done in conjunction with actual movement, as the
     * exact move extended info block is only responsible for visualizing
     * precise movement, and will synchronize to the real coordinate once
     * the exact movement has finished.
     *
     * @param deltaX1 the coordinate delta between the current absolute
     * x coordinate and where the avatar is going.
     * @param deltaZ1 the coordinate delta between the current absolute
     * z coordinate and where the avatar is going.
     * @param delay1 how many client cycles (20ms/cc) until the avatar arrives
     * at x/z 1 coordinate.
     * @param deltaX2 the coordinate delta between the current absolute
     * x coordinate and where the avatar is going.
     * @param deltaZ2 the coordinate delta between the current absolute
     * z coordinate and where the avatar is going.
     * @param delay2 how many client cycles (20ms/cc) until the avatar arrives
     * at x/z 2 coordinate.
     * @param angle the angle the avatar will be facing throughout the exact movement,
     * with 0 implying south, 512 west, 1024 north and 1536 east; interpolate
     * between to get finer directions.
     */
    public fun setExactMove(
        deltaX1: Int,
        deltaZ1: Int,
        delay1: Int,
        deltaX2: Int,
        deltaZ2: Int,
        delay2: Int,
        angle: Int,
    ) {
        verify {
            require(delay1 >= 0) {
                "First delay cannot be negative: $delay1"
            }
            require(delay2 >= 0) {
                "Second delay cannot be negative: $delay2"
            }
            require(angle in 0..2047) {
                "Unexpected angle value: $angle, expected range: 0..2047"
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
        blocks.exactMove.direction = angle.toUShort()
        flags = flags or EXACT_MOVE
    }

    /**
     * Sets the spotanim in slot [slot], overriding any previous spotanim
     * in that slot in doing so.
     * @param slot the slot of the spotanim.
     * @param id the id of the spotanim.
     * @param delay the delay in client cycles (20ms/cc) until the given spotanim begins rendering.
     * @param height the height at which to render the spotanim.
     */
    public fun setSpotAnim(
        slot: Int,
        id: Int,
        delay: Int,
        height: Int,
    ) {
        verify {
            require(slot in 0..<RSProtFlags.spotanimListCapacity) {
                "Unexpected slot: $slot, expected range: 0..<${RSProtFlags.spotanimListCapacity}"
            }
            require(id == -1 || id in UNSIGNED_SHORT_RANGE) {
                "Unexpected id: $id, expected value -1 or in range: $UNSIGNED_SHORT_RANGE"
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

    /**
     * Adds a simple hitmark on this avatar.
     * @param sourceIndex the index of the character that dealt the hit.
     * If the target avatar is a player, add 0x10000 to the real index value (0-2048).
     * If the target avatar is a NPC, set the index as it is.
     * If there is no source, set the index to -1.
     * The index will be used for tinting purposes, as both the player who dealt
     * the hit, and the recipient will see a tinted variant.
     * Everyone else, however, will see a regular darkened hit mark.
     * @param selfType the multi hitmark id that supports tinted and darkened variants.
     * @param otherType the hitmark id to render to anyone that isn't the recipient,
     * or the one who dealt the hit. This will generally be a darkened variant.
     * If the hitmark should only render to the local player, set the [otherType]
     * value to -1, forcing it to only render to the recipient (and in the case of
     * a [sourceIndex] being defined, the one who dealt the hit)
     * @param value the value to show over the hitmark.
     * @param delay the delay in client cycles (20ms/cc) until the hitmark renders.
     */
    public fun addHitMark(
        sourceIndex: Int,
        selfType: Int,
        otherType: Int = selfType,
        value: Int,
        delay: Int = 0,
    ) {
        if (blocks.hit.hitMarkList.size >= 0xFF) {
            return
        }
        verify {
            require(sourceIndex == -1 || sourceIndex in 0..0x107FF) {
                "Unexpected source index: $sourceIndex, expected values: -1 to reset, " +
                    "0-65535 for NPCs, 65536-67583 for players"
            }
            require(selfType in UNSIGNED_SMART_1_OR_2_RANGE) {
                "Unexpected selfType: $selfType, expected range $UNSIGNED_SMART_1_OR_2_RANGE"
            }
            require(otherType == -1 || otherType in UNSIGNED_SMART_1_OR_2_RANGE) {
                "Unexpected otherType: $otherType, expected value -1 or range $UNSIGNED_SMART_1_OR_2_RANGE"
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

    /**
     * Removes the oldest currently showing hitmark on this avatar,
     * if one exists.
     * @param delay the delay in client cycles (20ms/cc) until the hitmark is removed.
     */
    public fun removeHitMark(delay: Int = 0) {
        if (blocks.hit.hitMarkList.size >= 0xFF) {
            return
        }
        verify {
            require(delay in UNSIGNED_SMART_1_OR_2_RANGE) {
                "Unexpected delay: $delay, expected range $UNSIGNED_SMART_1_OR_2_RANGE"
            }
        }
        blocks.hit.hitMarkList += HitMark(0x7FFEu, delay.toUShort())
        flags = flags or HITS
    }

    /**
     * Adds a simple hitmark on this avatar.
     * @param sourceIndex the index of the character that dealt the hit.
     * If the target avatar is a player, add 0x10000 to the real index value (0-2048).
     * If the target avatar is a NPC, set the index as it is.
     * If there is no source, set the index to -1.
     * The index will be used for tinting purposes, as both the player who dealt
     * the hit, and the recipient will see a tinted variant.
     * Everyone else, however, will see a regular darkened hit mark.
     * @param selfType the multi hitmark id that supports tinted and darkened variants.
     * @param otherType the hitmark id to render to anyone that isn't the recipient,
     * or the one who dealt the hit. This will generally be a darkened variant.
     * If the hitmark should only render to the local player, set the [otherType]
     * value to -1, forcing it to only render to the recipient (and in the case of
     * a [sourceIndex] being defined, the one who dealt the hit)
     * @param value the value to show over the hitmark.
     * @param selfSoakType the multi hitmark id that supports tinted and darkened variants,
     * shown as soaking next to the normal hitmark.
     * @param otherSoakType the hitmark id to render to anyone that isn't the recipient,
     * or the one who dealt the hit. This will generally be a darkened variant.
     * Unlike the [otherType], this does not support -1, as it is not possible to show partial
     * soaked hitmarks.
     * @param delay the delay in client cycles (20ms/cc) until the hitmark renders.
     */
    @JvmOverloads
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
        if (blocks.hit.hitMarkList.size >= 0xFF) {
            return
        }
        verify {
            require(sourceIndex == -1 || sourceIndex in 0..0x107FF) {
                "Unexpected source index: $sourceIndex, expected values: -1 to reset, " +
                    "0-65535 for NPCs, 65536-67583 for players"
            }
            require(selfType in UNSIGNED_SMART_1_OR_2_RANGE) {
                "Unexpected selfType: $selfType, expected range $UNSIGNED_SMART_1_OR_2_RANGE"
            }
            require(otherType == -1 || otherType in UNSIGNED_SMART_1_OR_2_RANGE) {
                "Unexpected otherType: $otherType, expected value -1 or in range $UNSIGNED_SMART_1_OR_2_RANGE"
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

    /**
     * Adds a headbar onto the avatar.
     * If a headbar by the same id already exists, updates the status of the old one.
     * Up to four distinct headbars can be rendered simultaneously.
     *
     * @param sourceIndex the index of the entity that dealt the hit that resulted in this headbar.
     * If the target avatar is a player, add 0x10000 to the real index value (0-2048).
     * If the target avatar is a NPC, set the index as it is.
     * If there is no source, set the index to -1.
     * The index will be used for rendering purposes, as both the player who dealt
     * the hit, and the recipient will see the [selfType] variant, and everyone else
     * will see the [otherType] variant, which, if set to -1 will be skipped altogether.
     * @param selfType the id of the headbar to render to the entity on which the headbar appears,
     * as well as the source who resulted in the creation of the headbar.
     * @param otherType the id of the headbar to render to everyone that doesn't fit the [selfType]
     * criteria. If set to -1, the headbar will not be rendered to these individuals.
     * @param startFill the number of pixels to render of this headbar at in the start.
     * The number of pixels that a headbar supports is defined in its respective headbar config.
     * @param endFill the number of pixels to render of this headbar at in the end,
     * if a [startTime] and [endTime] are defined.
     * @param startTime the delay in client cycles (20ms/cc) until the headbar renders at [startFill]
     * @param endTime the delay in client cycles (20ms/cc) until the headbar arrives at [endFill].
     */
    @JvmOverloads
    public fun addHeadBar(
        sourceIndex: Int,
        selfType: Int,
        otherType: Int = selfType,
        startFill: Int,
        endFill: Int = startFill,
        startTime: Int = 0,
        endTime: Int = 0,
    ) {
        if (blocks.hit.headBarList.size >= 0xFF) {
            return
        }
        verify {
            require(sourceIndex == -1 || sourceIndex in 0..0x107FF) {
                "Unexpected source index: $sourceIndex, expected values: -1 to reset, " +
                    "0-65535 for NPCs, 65536-67583 for players"
            }
            require(selfType == -1 || selfType in UNSIGNED_SMART_1_OR_2_RANGE) {
                "Unexpected id: $selfType, expected value -1 or in range $UNSIGNED_SMART_1_OR_2_RANGE"
            }
            require(otherType == -1 || otherType in UNSIGNED_SMART_1_OR_2_RANGE) {
                "Unexpected id: $otherType, expected value -1 or in range $UNSIGNED_SMART_1_OR_2_RANGE"
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
            require(endTime >= startTime) {
                "End time must be greater than or equal to start time: $startTime <= $endTime"
            }
        }
        blocks.hit.headBarList +=
            HeadBar(
                sourceIndex,
                selfType.toUShort(),
                otherType.toUShort(),
                startFill.toUByte(),
                endFill.toUByte(),
                startTime.toUShort(),
                endTime.toUShort(),
            )
        flags = flags or HITS
    }

    /**
     * Removes a headbar on this avatar by the id of [id], if one renders.
     * @param id the id of the head bar to remove.
     */
    public fun removeHeadBar(id: Int) {
        addHeadBar(
            -1,
            id,
            startFill = 0,
            endTime = HeadBar.REMOVED.toInt(),
        )
    }

    /**
     * Applies a tint over the non-textured parts of the character.
     * @param startTime the delay in client cycles (20ms/cc) until the tinting is applied.
     * @param endTime the timestamp in client cycles (20ms/cc) until the tinting finishes.
     * @param hue the hue of the tint.
     * @param saturation the saturation of the tint.
     * @param lightness the lightness of the tint.
     * @param weight the weight (or opacity) of the tint.
     */
    @Deprecated(
        message = "Deprecated. Use setTinting(startTime, endTime, hue, saturation, lightness, weight) for consistency.",
        replaceWith = ReplaceWith("setTinting(startTime, endTime, hue, saturation, lightness, weight)"),
    )
    public fun tinting(
        startTime: Int,
        endTime: Int,
        hue: Int,
        saturation: Int,
        lightness: Int,
        weight: Int,
    ) {
        setTinting(startTime, endTime, hue, saturation, lightness, weight)
    }

    /**
     * Applies a tint over the non-textured parts of the character.
     * @param startTime the delay in client cycles (20ms/cc) until the tinting is applied.
     * @param endTime the timestamp in client cycles (20ms/cc) until the tinting finishes.
     * @param hue the hue of the tint.
     * @param saturation the saturation of the tint.
     * @param lightness the lightness of the tint.
     * @param weight the weight (or opacity) of the tint.
     */
    public fun setTinting(
        startTime: Int,
        endTime: Int,
        hue: Int,
        saturation: Int,
        lightness: Int,
        weight: Int,
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
        val tint = blocks.tinting.global
        tint.start = startTime.toUShort()
        tint.end = endTime.toUShort()
        tint.hue = hue.toUByte()
        tint.saturation = saturation.toUByte()
        tint.lightness = lightness.toUByte()
        tint.weight = weight.toUByte()
        flags = flags or TINTING
    }

    /**
     * Applies a tint over the non-textured parts of the character.
     * @param startTime the delay in client cycles (20ms/cc) until the tinting is applied.
     * @param endTime the timestamp in client cycles (20ms/cc) until the tinting finishes.
     * @param hue the hue of the tint.
     * @param saturation the saturation of the tint.
     * @param lightness the lightness of the tint.
     * @param weight the weight (or opacity) of the tint.
     * @param visibleTo the player who will see the tint applied.
     * Note that this only accepts player indices, and not NPC ones like many other extended info blocks.
     */
    @Deprecated(
        message =
            "Deprecated. Use setSpecificTinting(startTime, endTime, hue, saturation, " +
                "lightness, weight, visibleTo) for consistency.",
        replaceWith =
            ReplaceWith(
                "setSpecificTinting(startTime, endTime, hue, saturation, " +
                    "lightness, weight, visibleTo)",
            ),
    )
    public fun specificTinting(
        startTime: Int,
        endTime: Int,
        hue: Int,
        saturation: Int,
        lightness: Int,
        weight: Int,
        visibleTo: PlayerInfo,
    ) {
        setSpecificTinting(startTime, endTime, hue, saturation, lightness, weight, visibleTo)
    }

    /**
     * Applies a tint over the non-textured parts of the character.
     * @param startTime the delay in client cycles (20ms/cc) until the tinting is applied.
     * @param endTime the timestamp in client cycles (20ms/cc) until the tinting finishes.
     * @param hue the hue of the tint.
     * @param saturation the saturation of the tint.
     * @param lightness the lightness of the tint.
     * @param weight the weight (or opacity) of the tint.
     * @param visibleTo the player who will see the tint applied.
     * Note that this only accepts player indices, and not NPC ones like many other extended info blocks.
     */
    public fun setSpecificTinting(
        startTime: Int,
        endTime: Int,
        hue: Int,
        saturation: Int,
        lightness: Int,
        weight: Int,
        visibleTo: PlayerInfo,
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
        val tint = Tinting()
        blocks.tinting.observerDependent[visibleTo.avatar.extendedInfo.localIndex] = tint
        tint.start = startTime.toUShort()
        tint.end = endTime.toUShort()
        tint.hue = hue.toUByte()
        tint.saturation = saturation.toUByte()
        tint.lightness = lightness.toUByte()
        tint.weight = weight.toUByte()
        visibleTo.observerExtendedInfoFlags.addFlag(
            localIndex,
            TINTING,
        )
    }

    /**
     * Sets the name of the avatar.
     * @param name the name to assign.
     */
    public fun setName(name: String) {
        if (blocks.appearance.name == name) {
            return
        }
        blocks.appearance.name = name
        flagAppearance()
    }

    /**
     * Sets the combat level of the avatar.
     * @param combatLevel the level to assign.
     */
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

    /**
     * Sets the skill level of the avatar, seen when right-clicking players as "skill: value",
     * instead of the usual combat level. Set to 0 to render combat level instead.
     * @param skillLevel the level to render
     */
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

    /**
     * Sets this avatar hidden (or un-hidden) client-sided.
     * If the observer is a J-Mod or above, the character will render regardless.
     * It is worth noting that plugin clients such as RuneLite will render information
     * about these avatars regardless of their hidden status.
     * @param hidden whether to hide the avatar.
     */
    public fun setHidden(hidden: Boolean) {
        if (blocks.appearance.hidden == hidden) {
            return
        }
        blocks.appearance.hidden = hidden
        flagAppearance()
    }

    /**
     * Sets the character male or female.
     * @param isMale whether to set the character male (or female, if false)
     */
    @Deprecated(
        message = "Deprecated. Use setBodyType(type) for consistency.",
        replaceWith = ReplaceWith("setBodyType(type)"),
    )
    public fun setMale(isMale: Boolean) {
        setBodyType(if (isMale) 0 else 1)
    }

    /**
     * Sets the body type of the character.
     * @param type the body type of the character.
     */
    public fun setBodyType(type: Int) {
        if (blocks.appearance.bodyType == type.toUByte()) {
            return
        }
        blocks.appearance.bodyType = type.toUByte()
        flagAppearance()
    }

    /**
     * Sets the pronoun of this avatar.
     * @param num the number to set, with the value 0 being male, 1 being female,
     * and 2 being 'other'.
     */
    @Deprecated(
        message = "Deprecated. Use setPronoun(num) for consistency.",
        replaceWith = ReplaceWith("setPronoun(num)"),
    )
    public fun setTextGender(num: Int) {
        setPronoun(num)
    }

    /**
     * Sets the pronoun of this avatar.
     * @param num the number to set, with the value 0 being male, 1 being female,
     * and 2 being 'other'.
     */
    public fun setPronoun(num: Int) {
        verify {
            require(num in UNSIGNED_BYTE_RANGE) {
                "Unexpected textGender $num, expected range $UNSIGNED_BYTE_RANGE"
            }
        }
        val pronoun = num.toUByte()
        if (blocks.appearance.pronoun == pronoun) {
            return
        }
        blocks.appearance.pronoun = pronoun
        flagAppearance()
    }

    /**
     * Sets the skull icon over this avatar.
     * @param icon the id of the icon to render, or -1 to not show any.
     */
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

    /**
     * Sets the overhead icon over this avatar (e.g. prayer icons)
     * @param icon the id of the icon to render, or -1 to not show any.
     */
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

    /**
     * Transforms this avatar to the respective NPC, or back to player if the [id] is -1.
     * @param id the id of the NPC to transform to, or -1 if resetting.
     */
    @Deprecated(
        message = "Deprecated. Use setTransmogrification(id) for consistency.",
        replaceWith = ReplaceWith("setTransmogrification(id)"),
    )
    public fun transformToNpc(id: Int) {
        setTransmogrification(id)
    }

    /**
     * Transforms this avatar to the respective NPC, or back to player if the [id] is -1.
     * @param id the id of the NPC to transform to, or -1 if resetting.
     */
    public fun setTransmogrification(id: Int) {
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

    /**
     * Sets an ident kit. Note that this function does not rely on wearpos values,
     * as those range from 0 to 11. Ident kit values only range from 0 to 6, which would
     * result in some wasted memory.
     * A list of wearpos to ident kit can also be found in
     * [net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.Appearance.identKitSlotList]
     *
     * Ident kit table:
     * ```kt
     * | Id |  Slot  |
     * |:--:|:------:|
     * |  0 |  Hair  |
     * |  1 |  Beard |
     * |  2 |  Body  |
     * |  3 |  Arms  |
     * |  4 | Gloves |
     * |  5 |  Legs  |
     * |  6 |  Boots |
     * ```
     *
     * @param identKitSlot the position in which to set this ident kit.
     * @param value the value of the ident kit config, or -1 if hidden.
     */
    public fun setIdentKit(
        identKitSlot: Int,
        value: Int,
    ) {
        verify {
            require(identKitSlot in 0..6) {
                "Unexpected wearPos $identKitSlot, expected range 0..6"
            }
            require(value == -1 || value in UNSIGNED_BYTE_RANGE) {
                "Unexpected value $value, expected value -1 or in range $UNSIGNED_BYTE_RANGE"
            }
        }
        val valueAsShort = value.toShort()
        val cur = blocks.appearance.identKit[identKitSlot]
        if (cur == valueAsShort) {
            return
        }
        blocks.appearance.identKit[identKitSlot] = valueAsShort
        flagAppearance()
    }

    /**
     * Sets a worn object in the given [wearpos].
     * @param wearpos the main wearpos in which the obj equips.
     * @param id the obj id to set in that wearpos, or -1 to not have anything.
     * @param wearpos2 the secondary wearpos that this obj utilizes, hiding whatever
     * ident kit was in that specific wearpos (e.g. hair, beard), or -1 to not use any.
     * @param wearpos3 the tertiary wearpos that this obj utilizes, hiding whatever
     * ident kit was in that specific wearpos (e.g. hair, beard), or -1 to not use any.
     */
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
        val hiddenSlotsBitpacked = (wearpos2 and 0xF shl 4) or (wearpos3 and 0xF)
        blocks.appearance.hiddenWearPos[wearpos] = hiddenSlotsBitpacked.toByte()
        flagAppearance()
    }

    /**
     * Sets the colour of this avatar's appearance.
     * @param slot the slot of the element to colour
     * @param value the 16-bit HSL colour value
     */
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

    /**
     * Sets the base animations of this avatar.
     * @param readyAnim the animation used when the avatar is standing still.
     * @param turnAnim the animation used when the avatar is turning on-spot without movement.
     * @param walkAnim the animation used when the avatar is walking forward.
     * @param walkAnimBack the animation used when the avatar is walking backwards.
     * @param walkAnimLeft the animation used when the avatar is walking to the left.
     * @param walkAnimRight the animation used when the avatar is walking to the right.
     * @param runAnim the animation used when the avatar is running.
     */
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

    /**
     * Sets the name extras of this avatar, rendered when right-clicking users.
     * @param beforeName the text to render before this avatar's name.
     * @param afterName the text to render after this avatar's name, but before the combat level.
     * @param afterCombatLevel the text to render after this avatar's combat level.
     */
    @Deprecated(
        message = "Deprecated. Use setNameExtras(beforeName, afterName, afterCombatLevel) for consistency.",
        replaceWith = ReplaceWith("setNameExtras(beforeName, afterName, afterCombatLevel)"),
    )
    public fun nameExtras(
        beforeName: String,
        afterName: String,
        afterCombatLevel: String,
    ) {
        setNameExtras(beforeName, afterName, afterCombatLevel)
    }

    /**
     * Sets the name extras of this avatar, rendered when right-clicking users.
     * @param beforeName the text to render before this avatar's name.
     * @param afterName the text to render after this avatar's name, but before the combat level.
     * @param afterCombatLevel the text to render after this avatar's combat level.
     */
    public fun setNameExtras(
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

    /**
     * Forces a model refresh client-side even if the worn objects + base colour + gender have not changed.
     * This is particularly important to enable when setting or clearing any obj type customisations,
     * as those are not considered when calculating the hash code.
     */
    @Deprecated(
        message = "Deprecated. Use setForceModelRefresh(enabled) for consistency.",
        replaceWith = ReplaceWith("setForceModelRefresh(enabled)"),
    )
    public fun forceModelRefresh(enabled: Boolean) {
        setForceModelRefresh(enabled)
    }

    /**
     * Forces a model refresh client-side even if the worn objects + base colour + gender have not changed.
     * This is particularly important to enable when setting or clearing any obj type customisations,
     * as those are not considered when calculating the hash code.
     */
    public fun setForceModelRefresh(enabled: Boolean) {
        blocks.appearance.forceModelRefresh = enabled
    }

    /**
     * Clears any obj type customisations applied to [wearpos].
     * @param wearpos the worn item slot.
     */
    @Deprecated(
        message = "Deprecated. Use resetObjTypeCustomisation(wearpos) for consistency.",
        replaceWith = ReplaceWith("resetObjTypeCustomisation(wearpos)"),
    )
    public fun clearObjTypeCustomisation(wearpos: Int) {
        resetObjTypeCustomisation(wearpos)
    }

    /**
     * Clears any obj type customisations applied to [wearpos].
     * @param wearpos the worn item slot.
     */
    public fun resetObjTypeCustomisation(wearpos: Int) {
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

    /**
     * Allocates an obj type customisation in [wearpos] if it doesn't already exist.
     * @param wearpos the wearpos in which a customisation is being made.
     * @return the customisation class holding the state overrides of this obj.
     */
    private fun allocObjCustomisation(wearpos: Int): ObjTypeCustomisation {
        var customisation = blocks.appearance.objTypeCustomisation[wearpos]
        if (customisation == null) {
            customisation = ObjTypeCustomisation()
            blocks.appearance.objTypeCustomisation[wearpos] = customisation
        }
        return customisation
    }

    /**
     * Recolours part of an obj in the first slot (out of two).
     * @param wearpos the position in which the obj is worn.
     * @param index the source index of the colour to override.
     * @param value the 16 bit HSL colour to override with.
     */
    @Deprecated(
        message = "Deprecated. Use setObjRecol1(wearpos, index, value) for consistency.",
        replaceWith = ReplaceWith("setObjRecol1(wearpos, index, value)"),
    )
    public fun objRecol1(
        wearpos: Int,
        index: Int,
        value: Int,
    ) {
        setObjRecol1(wearpos, index, value)
    }

    /**
     * Recolours part of an obj in the first slot (out of two).
     * @param wearpos the position in which the obj is worn.
     * @param index the source index of the colour to override.
     * @param value the 16 bit HSL colour to override with.
     */
    public fun setObjRecol1(
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

    /**
     * Recolours part of an obj in the second slot (out of two).
     * @param wearpos the position in which the obj is worn.
     * @param index the source index of the colour to override.
     * @param value the 16 bit HSL colour to override with.
     */
    @Deprecated(
        message = "Deprecated. Use setObjRecol2(wearpos, index, value) for consistency.",
        replaceWith = ReplaceWith("setObjRecol2(wearpos, index, value)"),
    )
    public fun objRecol2(
        wearpos: Int,
        index: Int,
        value: Int,
    ) {
        setObjRecol2(wearpos, index, value)
    }

    /**
     * Recolours part of an obj in the second slot (out of two).
     * @param wearpos the position in which the obj is worn.
     * @param index the source index of the colour to override.
     * @param value the 16 bit HSL colour to override with.
     */
    public fun setObjRecol2(
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

    /**
     * Retextures part of an obj in the first slot (out of two).
     * @param wearpos the position in which the obj is worn.
     * @param index the source index of the texture to override.
     * @param value the id of the texture to override with.
     */
    @Deprecated(
        message = "Deprecated. Use setObjRetex1(wearpos, index, value) for consistency.",
        replaceWith = ReplaceWith("setObjRetex1(wearpos, index, value)"),
    )
    public fun objRetex1(
        wearpos: Int,
        index: Int,
        value: Int,
    ) {
        setObjRetex1(wearpos, index, value)
    }

    /**
     * Retextures part of an obj in the first slot (out of two).
     * @param wearpos the position in which the obj is worn.
     * @param index the source index of the texture to override.
     * @param value the id of the texture to override with.
     */
    public fun setObjRetex1(
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

    /**
     * Retextures part of an obj in the second slot (out of two).
     * @param wearpos the position in which the obj is worn.
     * @param index the source index of the texture to override.
     * @param value the id of the texture to override with.
     */
    @Deprecated(
        message = "Deprecated. Use setObjRetex2(wearpos, index, value) for consistency.",
        replaceWith = ReplaceWith("setObjRetex2(wearpos, index, value)"),
    )
    public fun objRetex2(
        wearpos: Int,
        index: Int,
        value: Int,
    ) {
        setObjRetex2(wearpos, index, value)
    }

    /**
     * Retextures part of an obj in the second slot (out of two).
     * @param wearpos the position in which the obj is worn.
     * @param index the source index of the texture to override.
     * @param value the id of the texture to override with.
     */
    public fun setObjRetex2(
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

    /**
     * Flags appearance to have changed, in order for it to be synchronized to all observers.
     */
    private fun flagAppearance() {
        flags = flags or APPEARANCE
        lastAppearanceChangeCycle = PlayerInfoProtocol.cycleCount
    }

    /**
     * Clears any transient extended info blocks which only applied for this cycle,
     * making it ready for the next.
     */
    internal fun postUpdate() {
        clearTransientExtendedInformation()
        flags = 0
    }

    /**
     * Resets all the properties of this extended info object, making it ready for use
     * by another avatar.
     */
    internal fun reset() {
        flags = 0
        this.lastAppearanceChangeCycle = 0
        this.otherAppearanceChangeCycles.fill(-1)
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
        observedChatStorage.reset()
    }

    /**
     * Resets the cached state on reconnect, ensuring we inform the client of all that was
     * previously assigned.
     */
    internal fun onReconnect() {
        this.lastAppearanceChangeCycle = 0
        this.otherAppearanceChangeCycles.fill(-1)
    }

    /**
     * Gets all the extended info flags which must be updated for the given [observer],
     * based on what is out of date with what they last saw (if they saw the player before).
     * @param observer the avatar observing us.
     * @return the flags that need updating.
     */
    internal fun getLowToHighResChangeExtendedInfoFlags(
        observer: PlayerAvatarExtendedInfo,
        oldSchoolClientType: OldSchoolClientType,
    ): Int {
        var flag = 0
        if (this.flags and APPEARANCE == 0 &&
            checkOutOfDate(observer) &&
            blocks.appearance.isPrecomputed(oldSchoolClientType)
        ) {
            flag = flag or APPEARANCE
        }
        if (this.flags and MOVE_SPEED == 0 &&
            blocks.moveSpeed.value != MoveSpeed.DEFAULT_MOVESPEED &&
            blocks.moveSpeed.isPrecomputed(oldSchoolClientType)
        ) {
            flag = flag or MOVE_SPEED
        }
        if (this.flags and FACE_PATHINGENTITY == 0 &&
            blocks.facePathingEntity.index != FacePathingEntity.DEFAULT_VALUE &&
            blocks.facePathingEntity.isPrecomputed(oldSchoolClientType)
        ) {
            flag = flag or FACE_PATHINGENTITY
        }
        if (this.flags and FACE_ANGLE == 0 &&
            blocks.faceAngle.angle != FaceAngle.DEFAULT_VALUE &&
            blocks.faceAngle.isPrecomputed(oldSchoolClientType)
        ) {
            flag = flag or FACE_ANGLE
        }
        return flag
    }

    /**
     * Checks if the cached version of our appearance is out for date for the [observer].
     * @param observer the avatar observing us.
     * @return true if the [observer] needs an updated version of our avatar, false if the cached
     * variant is still up-to-date.
     */
    private fun checkOutOfDate(observer: PlayerAvatarExtendedInfo): Boolean =
        observer.otherAppearanceChangeCycles[localIndex] < lastAppearanceChangeCycle

    /**
     * Silently synchronizes the angle of the avatar, meaning any new observers will see them
     * at this specific angle.
     * @param angle the angle to render them under.
     */
    public fun syncAngle(angle: Int) {
        this.blocks.faceAngle.syncAngle(angle)
    }

    /**
     * Pre-computes all the buffers for this avatar.
     * Pre-computation is done, so we don't have to calculate these extended info blocks
     * for every avatar that observes us. Instead, we can do more performance-efficient
     * operations of native memory copying to get the latest extended info blocks.
     */
    internal fun precompute() {
        // Hits and tinting do not get precomputed
        if (flags and APPEARANCE != 0) {
            blocks.appearance.precompute(allocator, huffmanCodec)
        }
        if (flags and TEMP_MOVE_SPEED != 0) {
            blocks.temporaryMoveSpeed.precompute(allocator, huffmanCodec)
        }
        if (flags and SEQUENCE != 0) {
            blocks.sequence.precompute(allocator, huffmanCodec)
        }
        if (flags and FACE_ANGLE != 0 || blocks.faceAngle.outOfDate) {
            blocks.faceAngle.markUpToDate()
            blocks.faceAngle.precompute(allocator, huffmanCodec)
        }
        if (flags and SAY != 0) {
            blocks.say.precompute(allocator, huffmanCodec)
        }
        if (flags and CHAT != 0) {
            blocks.chat.precompute(allocator, huffmanCodec)
        }
        if (flags and EXACT_MOVE != 0) {
            blocks.exactMove.precompute(allocator, huffmanCodec)
        }
        if (flags and SPOTANIM != 0) {
            blocks.spotAnims.precompute(allocator, huffmanCodec)
        }
        if (flags and FACE_PATHINGENTITY != 0) {
            blocks.facePathingEntity.precompute(allocator, huffmanCodec)
        }
        if (flags and MOVE_SPEED != 0) {
            blocks.moveSpeed.precompute(allocator, huffmanCodec)
        }
    }

    /**
     * Writes the extended info block of this avatar for the given observer.
     * @param oldSchoolClientType the client that the observer is using.
     * @param buffer the buffer into which the extended info block should be written.
     * @param observerFlag the dynamic out-of-date flags that we must send to the observer
     * on-top of everything that was pre-computed earlier.
     * @param observer the avatar that is observing us.
     * @param remainingAvatars the number of avatars that must still be updated for
     * the given [observer], necessary to avoid memory overflow.
     */
    internal fun pExtendedInfo(
        oldSchoolClientType: OldSchoolClientType,
        buffer: JagByteBuf,
        observerFlag: Int,
        observer: PlayerAvatarExtendedInfo,
        remainingAvatars: Int,
    ): Boolean {
        val flag = this.flags or observerFlag
        if (!filter.accept(
                buffer.writableBytes(),
                flag,
                remainingAvatars,
                observer.otherAppearanceChangeCycles[localIndex] != -1,
            )
        ) {
            buffer.p1(0)
            return false
        }
        val writer =
            requireNotNull(writers[oldSchoolClientType.id]) {
                "Extended info writer missing for client $oldSchoolClientType"
            }

        // If appearance is flagged, ensure we synchronize the changes counter
        if (flag and APPEARANCE != 0) {
            observer.otherAppearanceChangeCycles[localIndex] = lastAppearanceChangeCycle
        }
        // Note: The order must be as client expects it, in 222 say is before chat
        if (flag and SAY != 0) {
            val appendToChatbox =
                this.blocks.say.text
                    ?.get(0) == '~'
            if (localIndex == observer.localIndex || appendToChatbox) {
                observer.observedChatStorage.trackSay(this.localIndex, this.blocks.say)
            }
        }
        if (flag and CHAT != 0) {
            observer.observedChatStorage.trackChat(this.localIndex, this.blocks.chat)
        }
        writer.pExtendedInfo(
            buffer,
            localIndex,
            observer.localIndex,
            flag,
            blocks,
        )
        return true
    }

    /**
     * Clears any flagged transient extended information blocks from this cycle.
     */
    private fun clearTransientExtendedInformation() {
        if (flags and TEMP_MOVE_SPEED != 0) {
            blocks.temporaryMoveSpeed.clear()
        }
        if (flags and SEQUENCE != 0) {
            blocks.sequence.clear()
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

    /**
     * Resets our tracked version of the target's appearance,
     * so it will be updated whenever someone else takes their index.
     */
    internal fun onOtherAvatarDeallocated(idx: Int) {
        otherAppearanceChangeCycles[idx] = -1
    }

    public companion object {
        // Observer-dependent flags, utilizing the lowest bits as we store observer flags in a byte array
        public const val APPEARANCE: Int = 0x1
        public const val MOVE_SPEED: Int = 0x2
        public const val FACE_PATHINGENTITY: Int = 0x4
        public const val TINTING: Int = 0x8
        public const val FACE_ANGLE: Int = 0x10

        // "Static" flags, the bit values here are irrelevant
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

        /**
         * Executes the [block] if input verification is enabled,
         * otherwise does nothing. Verification should be enabled for
         * development environments, to catch problems mid-development.
         * In production, or during benchmarking, verification should be disabled,
         * as there is still some overhead to running verifications.
         */
        private inline fun verify(crossinline block: () -> Unit) {
            if (RSProtFlags.extendedInfoInputVerification) {
                block()
            }
        }

        /**
         * Builds an extended info writer array indexed by provided client types.
         * All client types which are utilized must be registered to avoid runtime errors.
         */
        private fun buildClientWriterArray(
            extendedInfoWriters: List<PlayerAvatarExtendedInfoWriter>,
        ): Array<PlayerAvatarExtendedInfoWriter?> {
            val array =
                arrayOfNulls<PlayerAvatarExtendedInfoWriter>(
                    OldSchoolClientType.COUNT,
                )
            for (writer in extendedInfoWriters) {
                array[writer.oldSchoolClientType.id] = writer
            }
            return array
        }
    }
}
