package net.rsprot.protocol.game.outgoing.info.npcinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.game.outgoing.info.AvatarExtendedInfoWriter
import net.rsprot.protocol.game.outgoing.info.filter.ExtendedInfoFilter
import net.rsprot.protocol.internal.RSProtFlags
import net.rsprot.protocol.internal.checkCommunicationThread
import net.rsprot.protocol.internal.game.outgoing.info.npcinfo.encoder.NpcExtendedInfoEncoders
import net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.BaseAnimationSet
import net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.CombatLevelChange
import net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.HeadIconCustomisation
import net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.TypeCustomisation
import net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.VisibleOps
import net.rsprot.protocol.internal.game.outgoing.info.precompute
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.FacePathingEntity
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util.HeadBar
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util.HitMark
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util.SpotAnim

public typealias NpcAvatarExtendedInfoWriter =
    AvatarExtendedInfoWriter<NpcExtendedInfoEncoders, NpcAvatarExtendedInfoBlocks>

/**
 * Npc avatar extended info is a data structure used to keep track of all the extended info
 * properties of the given avatar.
 * @property avatarIndex the index of the avatar npc
 * @property filter the filter used to ensure that the buffer does not exceed the 40kb limit.
 * @param extendedInfoWriters the list of client-specific extended info writers.
 * @property allocator the byte buffer allocator used to pre-compute extended info blocks.
 * @property huffmanCodec the huffman codec is used to compress chat messages, though
 * none are used for NPCs, the writer function still expects it.
 */
@Suppress("DuplicatedCode")
public class NpcAvatarExtendedInfo(
    private var avatarIndex: Int,
    private val filter: ExtendedInfoFilter,
    extendedInfoWriters: List<NpcAvatarExtendedInfoWriter>,
    private val allocator: ByteBufAllocator,
    private val huffmanCodec: HuffmanCodecProvider,
) {
    /**
     * The extended info blocks enabled on this NPC in a given cycle.
     */
    internal var flags: Int = 0

    /**
     * Extended info blocks used to transmit changes to the client,
     * wrapped in its own class as we must pass this onto the client-specific
     * implementations.
     */
    private val blocks: NpcAvatarExtendedInfoBlocks = NpcAvatarExtendedInfoBlocks(extendedInfoWriters)

    /**
     * The client-specific extended info writers, indexed by the respective [OldSchoolClientType]'s id.
     * All clients in use must be registered, or an exception will occur during player info encoding.
     */
    private val writers: Array<NpcAvatarExtendedInfoWriter?> =
        buildClientWriterArray(extendedInfoWriters)

    /**
     * Sets the sequence for this avatar to play.
     * @param id the id of the sequence to play, or -1 to stop playing current sequence.
     * @param delay the delay in client cycles (20ms/cc) until the avatar starts playing this sequence.
     */
    public fun setSequence(
        id: Int,
        delay: Int,
    ) {
        checkCommunicationThread()
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
        checkCommunicationThread()
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
     * Sets the overhead chat of this avatar.
     * @param text the text to render overhead.
     */
    public fun setSay(text: String) {
        checkCommunicationThread()
        verify {
            require(text.length <= 256) {
                "Unexpected say input; expected value 256 characters or less, " +
                    "input len: ${text.length}, input: $text"
            }
        }
        blocks.say.text = text
        flags = flags or SAY
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
        checkCommunicationThread()
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
        checkCommunicationThread()
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
     * @param sourceType the multi hitmark id that supports tinted and darkened variants.
     * If the value is -1, the hitmark will not render to the player with the source index,
     * only everyone else.
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
        sourceType: Int,
        otherType: Int = sourceType,
        value: Int,
        delay: Int = 0,
    ) {
        checkCommunicationThread()
        if (blocks.hit.hitMarkList.size >= 0xFF) {
            return
        }
        verify {
            // Index being incorrect would not lead to a crash
            require(sourceIndex == -1 || sourceIndex in 0..0x107FF) {
                "Unexpected source index: $sourceIndex, expected values: -1 to reset, " +
                    "0-65535 for NPCs, 65536-67583 for players"
            }
        }

        // All the properties below here would result in a crash if an invalid input was provided.
        require(sourceType in HIT_TYPE_RANGE) {
            "Unexpected sourceType: $sourceType, expected range $HIT_TYPE_RANGE"
        }
        require(otherType in HIT_TYPE_RANGE) {
            "Unexpected otherType: $otherType, expected range $HIT_TYPE_RANGE"
        }
        require(value in UNSIGNED_SMART_1_OR_2_RANGE) {
            "Unexpected value: $value, expected range $UNSIGNED_SMART_1_OR_2_RANGE"
        }
        require(delay in UNSIGNED_SMART_1_OR_2_RANGE) {
            "Unexpected delay: $delay, expected range $UNSIGNED_SMART_1_OR_2_RANGE"
        }
        blocks.hit.hitMarkList +=
            HitMark(
                sourceIndex,
                sourceType.toUShort(),
                sourceType.toUShort(),
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
        checkCommunicationThread()
        if (blocks.hit.hitMarkList.size >= 0xFF) {
            return
        }
        require(delay in UNSIGNED_SMART_1_OR_2_RANGE) {
            "Unexpected delay: $delay, expected range $UNSIGNED_SMART_1_OR_2_RANGE"
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
     * @param sourceType the multi hitmark id that supports tinted and darkened variants.
     * If the value is -1, the hitmark will not render to the player with the source index,
     * only everyone else.
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
        sourceType: Int,
        otherType: Int = sourceType,
        value: Int,
        selfSoakType: Int,
        otherSoakType: Int = selfSoakType,
        soakValue: Int,
        delay: Int = 0,
    ) {
        checkCommunicationThread()
        if (blocks.hit.hitMarkList.size >= 0xFF) {
            return
        }
        verify {
            // Index being incorrect would not lead to a crash
            require(sourceIndex == -1 || sourceIndex in 0..0x107FF) {
                "Unexpected source index: $sourceIndex, expected values: -1 to reset, " +
                    "0-65535 for NPCs, 65536-67583 for players"
            }
        }

        // All the properties below here would result in a crash if an invalid input was provided.
        require(sourceType in HIT_TYPE_RANGE) {
            "Unexpected sourceType: $sourceType, expected range $HIT_TYPE_RANGE"
        }
        require(otherType in HIT_TYPE_RANGE) {
            "Unexpected otherType: $otherType, expected range $HIT_TYPE_RANGE"
        }
        require(value in UNSIGNED_SMART_1_OR_2_RANGE) {
            "Unexpected value: $value, expected range $UNSIGNED_SMART_1_OR_2_RANGE"
        }
        require(selfSoakType in UNSIGNED_SMART_1_OR_2_RANGE) {
            "Unexpected selfSoakType: $selfSoakType, expected range $UNSIGNED_SMART_1_OR_2_RANGE"
        }
        require(otherSoakType in UNSIGNED_SMART_1_OR_2_RANGE) {
            "Unexpected otherSoakType: $otherSoakType, expected range $UNSIGNED_SMART_1_OR_2_RANGE"
        }
        require(soakValue in UNSIGNED_SMART_1_OR_2_RANGE) {
            "Unexpected soakValue: $soakValue, expected range $UNSIGNED_SMART_1_OR_2_RANGE"
        }
        require(delay in UNSIGNED_SMART_1_OR_2_RANGE) {
            "Unexpected delay: $delay, expected range $UNSIGNED_SMART_1_OR_2_RANGE"
        }
        blocks.hit.hitMarkList +=
            HitMark(
                sourceIndex,
                sourceType.toUShort(),
                sourceType.toUShort(),
                otherType.toUShort(),
                value.toUShort(),
                selfSoakType.toUShort(),
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
     * the hit, and the recipient will see the [sourceType] variant, and everyone else
     * will see the [otherType] variant, which, if set to -1 will be skipped altogether.
     * @param sourceType the id of the headbar to render to the player with the source index.
     * @param otherType the id of the headbar to render to everyone that doesn't fit the [sourceType]
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
        sourceType: Int,
        otherType: Int = sourceType,
        startFill: Int,
        endFill: Int = startFill,
        startTime: Int = 0,
        endTime: Int = 0,
    ) {
        checkCommunicationThread()
        if (blocks.hit.headBarList.size >= 0xFF) {
            return
        }
        verify {
            // Index being incorrect would not lead to a crash
            require(sourceIndex == -1 || sourceIndex in 0..0x107FF) {
                "Unexpected source index: $sourceIndex, expected values: -1 to reset, " +
                    "0-65535 for NPCs, 65536-67583 for players"
            }
            // Fills are transmitted via a byte, so they would not crash
            require(startFill in UNSIGNED_BYTE_RANGE) {
                "Unexpected startFill: $startFill, expected range $UNSIGNED_BYTE_RANGE"
            }
            require(endFill in UNSIGNED_BYTE_RANGE) {
                "Unexpected endFill: $endFill, expected range $UNSIGNED_BYTE_RANGE"
            }
        }

        // All the properties below here would result in a crash if an invalid input was provided.
        require(sourceType == -1 || sourceType in UNSIGNED_SMART_1_OR_2_RANGE) {
            "Unexpected source type: $sourceType, expected value -1 or in range $UNSIGNED_SMART_1_OR_2_RANGE"
        }
        require(otherType == -1 || otherType in UNSIGNED_SMART_1_OR_2_RANGE) {
            "Unexpected other type: $otherType, expected value -1 or in range $UNSIGNED_SMART_1_OR_2_RANGE"
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
        blocks.hit.headBarList +=
            HeadBar(
                sourceIndex,
                sourceType.toUShort(),
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
        checkCommunicationThread()
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
        setTinting(
            startTime,
            endTime,
            hue,
            saturation,
            lightness,
            weight,
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
    public fun setTinting(
        startTime: Int,
        endTime: Int,
        hue: Int,
        saturation: Int,
        lightness: Int,
        weight: Int,
    ) {
        checkCommunicationThread()
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
     * Faces the center of the absolute coordinate provided.
     * @param x the absolute x coordinate to turn towards
     * @param z the absolute z coordinate to turn towards
     * @param instant whether to turn towards the coord instantly without any turn anim,
     * or gradually. The instant property is typically used when spawning in NPCs;
     * While the low to high resolution change does support a direction, it only supports
     * in increments of 45 degrees - so utilizing this extended info blocks allows for
     * more precise control over it.
     */
    @JvmOverloads
    @Deprecated(
        message = "Deprecated. Use setFaceCoord(x, z, instant) for consistency.",
        replaceWith = ReplaceWith("setFaceCoord(x, z, instant)"),
    )
    public fun faceCoord(
        x: Int,
        z: Int,
        instant: Boolean = false,
    ) {
        setFaceCoord(x, z, instant)
    }

    /**
     * Faces the center of the absolute coordinate provided.
     * @param x the absolute x coordinate to turn towards
     * @param z the absolute z coordinate to turn towards
     * @param instant whether to turn towards the coord instantly without any turn anim,
     * or gradually. The instant property is typically used when spawning in NPCs;
     * While the low to high resolution change does support a direction, it only supports
     * in increments of 45 degrees - so utilizing this extended info blocks allows for
     * more precise control over it.
     */
    @JvmOverloads
    public fun setFaceCoord(
        x: Int,
        z: Int,
        instant: Boolean = false,
    ) {
        checkCommunicationThread()
        verify {
            require(x in 0..<16384) {
                "Unexpected x coord: $x, expected range: 0..<16384"
            }
            require(z in 0..<16384) {
                "Unexpected z coord: $z, expected range: 0..<16384"
            }
        }
        val faceCoord = blocks.faceCoord
        faceCoord.x = x.toUShort()
        faceCoord.z = z.toUShort()
        faceCoord.instant = instant
        flags = flags or FACE_COORD
    }

    /**
     * Transforms this NPC into the [id] provided.
     * It should be noted that this extended info block is transient and only applies to one cycle.
     * The server is expected to additionally change the id of the avatar itself, otherwise
     * any new observers will get the old variant.
     *
     * Additionally, note that in order to reset the NPC back to the original variant, the server
     * must transform the NPC to the original id. RSProt does not track the original id internally.
     * @param id the new id of the npc to transform to.
     */
    @Deprecated(
        message = "Deprecated. Use setTransmogrification(id) for consistency.",
        replaceWith = ReplaceWith("setTransmogrification(id)"),
    )
    public fun transformation(id: Int) {
        setTransmogrification(id)
    }

    /**
     * Transforms this NPC into the [id] provided.
     * It should be noted that this extended info block is transient and only applies to one cycle.
     * The server is expected to additionally change the id of the avatar itself via [NpcAvatar.setId],
     * otherwise any new observers will get the old variant.
     *
     * Additionally, note that in order to reset the NPC back to the original variant, the server
     * must transform the NPC to the original id. RSProt does not track the original id internally.
     * @param id the new id of the npc to transform to.
     */
    public fun setTransmogrification(id: Int) {
        checkCommunicationThread()
        verify {
            require(id in UNSIGNED_SHORT_RANGE) {
                "Unexpected id: $id, expected in range: $UNSIGNED_SHORT_RANGE"
            }
        }
        blocks.transformation.id = id.toUShort()
        flags = flags or TRANSFORMATION
    }

    /**
     * Overrides the combat level of this NPC with the provided level.
     * @param level the combat leve to render, or -1 to remove the combat level override.
     */
    @Deprecated(
        message = "Deprecated. Use setCombatLevelChange(level) for consistency.",
        replaceWith = ReplaceWith("setCombatLevelChange(level)"),
    )
    public fun combatLevelChange(level: Int) {
        setCombatLevelChange(level)
    }

    /**
     * Overrides the combat level of this NPC with the provided level.
     * @param level the combat leve to render, or -1 to remove the combat level override.
     */
    public fun setCombatLevelChange(level: Int) {
        checkCommunicationThread()
        blocks.combatLevelChange.level = level
        flags = flags or LEVEL_CHANGE
    }

    /**
     * Overrides the name of this NPC with the provided [name].
     * @param name the name to override with, or null to reset an existing override.
     */
    @Deprecated(
        message = "Deprecated. Use setNameChange(name) for consistency.",
        replaceWith = ReplaceWith("setNameChange(name)"),
    )
    public fun nameChange(name: String?) {
        setNameChange(name)
    }

    /**
     * Overrides the name of this NPC with the provided [name].
     * @param name the name to override with, or null to reset an existing override.
     */
    public fun setNameChange(name: String?) {
        checkCommunicationThread()
        blocks.nameChange.name = name
        flags = flags or NAME_CHANGE
    }

    /**
     * Sets the visible ops flag of this NPC to the provided value.
     * @param flag the bit flag to set. Only the 5 lowest bits are used,
     * and an enabled bit implies the option at that index should render.
     * Note that this extended info block is not transient and will be transmitted to
     * future players as well.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    @Deprecated(
        message = "Deprecated. Use setVisibleOps(flag) for consistency.",
        replaceWith = ReplaceWith("setVisibleOps(flag)"),
    )
    public fun visibleOps(flag: Int) {
        setVisibleOps(flag)
    }

    /**
     * Sets the visible ops flag of this NPC to the provided value.
     * @param flag the bit flag to set. Only the 5 lowest bits are used,
     * and an enabled bit implies the option at that index should render.
     * Note that this extended info block is not transient and will be transmitted to
     * future players as well.
     */
    @Deprecated(
        message = "Deprecated. Use setVisibleOps() with [net.rsprot.protocol.game.outgoing.util.OpFlags].",
        replaceWith = ReplaceWith("setVisibleOps(flag.toByte())"),
    )
    @Suppress("MemberVisibilityCanBePrivate")
    public fun setVisibleOps(flag: Int) {
        checkCommunicationThread()
        setVisibleOps(flag.toByte())
    }

    /**
     * Marks the provided right-click options as visible or invisible.
     * @param op1 whether to render op1
     * @param op2 whether to render op2
     * @param op3 whether to render op3
     * @param op4 whether to render op4
     * @param op5 whether to render op5
     */
    @Deprecated(
        message = "Deprecated. Use setVisibleOps(op1, op2, op3, op4, op5) for consistency.",
        replaceWith = ReplaceWith("setVisibleOps(op1, op2, op3, op4, op5)"),
    )
    public fun visibleOps(
        op1: Boolean,
        op2: Boolean,
        op3: Boolean,
        op4: Boolean,
        op5: Boolean,
    ) {
        setVisibleOps(op1, op2, op3, op4, op5)
    }

    /**
     * Marks the provided right-click options as visible or invisible.
     * @param op1 whether to render op1
     * @param op2 whether to render op2
     * @param op3 whether to render op3
     * @param op4 whether to render op4
     * @param op5 whether to render op5
     */
    @Deprecated(
        message = "Deprecated. Use setVisibleOps() with [net.rsprot.protocol.game.outgoing.util.OpFlags].",
        replaceWith = ReplaceWith("setVisibleOps(OpFlags.ofOps(op1, op2, op3, op4, op5))"),
    )
    public fun setVisibleOps(
        op1: Boolean,
        op2: Boolean,
        op3: Boolean,
        op4: Boolean,
        op5: Boolean,
    ) {
        var flag = 0
        if (op1) flag = flag or 0x1
        if (op2) flag = flag or 0x2
        if (op3) flag = flag or 0x4
        if (op4) flag = flag or 0x8
        if (op5) flag = flag or 0x10
        setVisibleOps(flag)
    }

    /**
     * Sets all the right-click options invisible on this NPC.
     */
    @Deprecated(
        message = "Deprecated. Use setAllOpsInvisible() for consistency.",
        replaceWith = ReplaceWith("setAllOpsInvisible()"),
    )
    public fun allOpsInvisible() {
        setAllOpsInvisible()
    }

    /**
     * Sets all the right-click options invisible on this NPC.
     */
    @Deprecated(
        message = "Deprecated. Use setVisibleOps() with [net.rsprot.protocol.game.outgoing.util.OpFlags].",
        replaceWith = ReplaceWith("setVisibleOps(OpFlags.NONE_SHOWN)"),
    )
    public fun setAllOpsInvisible() {
        setVisibleOps(0)
    }

    /**
     * Sets all the right-click options as visible on this NPC.
     */
    @Deprecated(
        message = "Deprecated. Use setAllOpsVisible() for consistency.",
        replaceWith = ReplaceWith("setAllOpsVisible()"),
    )
    public fun allOpsVisible() {
        setAllOpsVisible()
    }

    /**
     * Sets all the right-click options as visible on this NPC.
     */
    @Deprecated(
        message = "Deprecated. Use setVisibleOps() with [net.rsprot.protocol.game.outgoing.util.OpFlags].",
        replaceWith = ReplaceWith("setVisibleOps(OpFlags.ALL_SHOWN)"),
    )
    public fun setAllOpsVisible() {
        setVisibleOps(0b11111)
    }

    /**
     * Sets the visible ops flag of this NPC to the provided value.
     * @param flag the bit flag to set. Only the 5 lowest bits are used,
     * and an enabled bit implies the option at that index should render.
     * Note that this extended info block is not transient and will be transmitted to
     * future players as well.
     *
     * Use [net.rsprot.protocol.game.outgoing.util.OpFlags] class to build the flag.
     */
    public fun setVisibleOps(flag: Byte) {
        checkCommunicationThread()
        blocks.visibleOps.ops = flag.toUByte()
        flags = flags or OPS
    }

    /**
     * Sets the base animation set of this NPC with the provided values.
     * If the value is equal to [Int.MIN_VALUE], the animation will not be overwritten.
     * Only the 16 lowest bits of the animation ids are used.
     * @param turnLeftAnim the animation used when the NPC turns to the left
     * @param turnRightAnim the animation used when the NPC turns to the right
     * @param walkAnim the animation used when the NPC walks forward
     * @param walkAnimLeft the animation used when the NPC walks to the left
     * @param walkAnimRight the animation used when the NPC walks to the right
     * @param walkAnimBack the animation used when the NPC walks backwards
     * @param runAnim the animation used when the NPC runs forward
     * @param runAnimLeft the animation used when the NPC runs to the left
     * @param runAnimRight the animation used when the NPC runs to the right
     * @param runAnimBack the animation used when the NPC runs backwards
     * @param crawlAnim the animation used when the NPC crawls forward
     * @param crawlAnimLeft the animation used when the NPC crawls to the left
     * @param crawlAnimRight the animation used when the NPC crawls to the right
     * @param crawlAnimBack the animation used when the NPC crawls backwards
     * @param readyAnim the default stance animation of this NPC when it is not moving
     */
    @JvmSynthetic
    @Deprecated(
        message =
            "Deprecated. Use setBaseAnimationSet(turnLeftAnim, turnRightAnim, " +
                "walkAnim, walkAnimBack, walkAnimLeft, walkAnimRight, " +
                "runAnim, runAnimBack, runAnimLeft, runAnimRight, " +
                "crawlAnim, crawlAnimBack, crawlAnimLeft, crawlAnimRight, readyAnim) for consistency.",
        replaceWith =
            ReplaceWith(
                "setBaseAnimationSet(turnLeftAnim, turnRightAnim, " +
                    "walkAnim, walkAnimBack, walkAnimLeft, walkAnimRight, " +
                    "runAnim, runAnimBack, runAnimLeft, runAnimRight, " +
                    "crawlAnim, crawlAnimBack, crawlAnimLeft, crawlAnimRight, readyAnim)",
            ),
    )
    public fun baseAnimationSet(
        turnLeftAnim: Int = Int.MIN_VALUE,
        turnRightAnim: Int = Int.MIN_VALUE,
        walkAnim: Int = Int.MIN_VALUE,
        walkAnimBack: Int = Int.MIN_VALUE,
        walkAnimLeft: Int = Int.MIN_VALUE,
        walkAnimRight: Int = Int.MIN_VALUE,
        runAnim: Int = Int.MIN_VALUE,
        runAnimBack: Int = Int.MIN_VALUE,
        runAnimLeft: Int = Int.MIN_VALUE,
        runAnimRight: Int = Int.MIN_VALUE,
        crawlAnim: Int = Int.MIN_VALUE,
        crawlAnimBack: Int = Int.MIN_VALUE,
        crawlAnimLeft: Int = Int.MIN_VALUE,
        crawlAnimRight: Int = Int.MIN_VALUE,
        readyAnim: Int = Int.MIN_VALUE,
    ) {
        setBaseAnimationSet(
            turnLeftAnim,
            turnRightAnim,
            walkAnim,
            walkAnimBack,
            walkAnimLeft,
            walkAnimRight,
            runAnim,
            runAnimBack,
            runAnimLeft,
            runAnimRight,
            crawlAnim,
            crawlAnimBack,
            crawlAnimLeft,
            crawlAnimRight,
            readyAnim,
        )
    }

    /**
     * Sets the base animation set of this NPC with the provided values.
     * If the value is equal to [Int.MIN_VALUE], the animation will not be overwritten.
     * Only the 16 lowest bits of the animation ids are used.
     * @param turnLeftAnim the animation used when the NPC turns to the left
     * @param turnRightAnim the animation used when the NPC turns to the right
     * @param walkAnim the animation used when the NPC walks forward
     * @param walkAnimLeft the animation used when the NPC walks to the left
     * @param walkAnimRight the animation used when the NPC walks to the right
     * @param walkAnimBack the animation used when the NPC walks backwards
     * @param runAnim the animation used when the NPC runs forward
     * @param runAnimLeft the animation used when the NPC runs to the left
     * @param runAnimRight the animation used when the NPC runs to the right
     * @param runAnimBack the animation used when the NPC runs backwards
     * @param crawlAnim the animation used when the NPC crawls forward
     * @param crawlAnimLeft the animation used when the NPC crawls to the left
     * @param crawlAnimRight the animation used when the NPC crawls to the right
     * @param crawlAnimBack the animation used when the NPC crawls backwards
     * @param readyAnim the default stance animation of this NPC when it is not moving
     */
    @JvmSynthetic
    public fun setBaseAnimationSet(
        turnLeftAnim: Int = Int.MIN_VALUE,
        turnRightAnim: Int = Int.MIN_VALUE,
        walkAnim: Int = Int.MIN_VALUE,
        walkAnimBack: Int = Int.MIN_VALUE,
        walkAnimLeft: Int = Int.MIN_VALUE,
        walkAnimRight: Int = Int.MIN_VALUE,
        runAnim: Int = Int.MIN_VALUE,
        runAnimBack: Int = Int.MIN_VALUE,
        runAnimLeft: Int = Int.MIN_VALUE,
        runAnimRight: Int = Int.MIN_VALUE,
        crawlAnim: Int = Int.MIN_VALUE,
        crawlAnimBack: Int = Int.MIN_VALUE,
        crawlAnimLeft: Int = Int.MIN_VALUE,
        crawlAnimRight: Int = Int.MIN_VALUE,
        readyAnim: Int = Int.MIN_VALUE,
    ) {
        checkCommunicationThread()
        val bas = blocks.baseAnimationSet
        var flag = bas.overrides
        if (turnLeftAnim != Int.MIN_VALUE) {
            bas.turnLeftAnim = turnLeftAnim.toUShort()
            flag = flag or BaseAnimationSet.TURN_LEFT_ANIM_FLAG
        }
        if (turnRightAnim != Int.MIN_VALUE) {
            bas.turnRightAnim = turnRightAnim.toUShort()
            flag = flag or BaseAnimationSet.TURN_RIGHT_ANIM_FLAG
        }
        if (walkAnim != Int.MIN_VALUE) {
            bas.walkAnim = walkAnim.toUShort()
            flag = flag or BaseAnimationSet.WALK_ANIM_FLAG
        }
        if (walkAnimBack != Int.MIN_VALUE) {
            bas.walkAnimBack = walkAnimBack.toUShort()
            flag = flag or BaseAnimationSet.WALK_ANIM_BACK_FLAG
        }
        if (walkAnimLeft != Int.MIN_VALUE) {
            bas.walkAnimLeft = walkAnimLeft.toUShort()
            flag = flag or BaseAnimationSet.WALK_ANIM_LEFT_FLAG
        }
        if (walkAnimRight != Int.MIN_VALUE) {
            bas.walkAnimRight = walkAnimRight.toUShort()
            flag = flag or BaseAnimationSet.WALK_ANIM_RIGHT_FLAG
        }
        if (runAnim != Int.MIN_VALUE) {
            bas.runAnim = runAnim.toUShort()
            flag = flag or BaseAnimationSet.RUN_ANIM_FLAG
        }
        if (runAnimBack != Int.MIN_VALUE) {
            bas.runAnimBack = runAnimBack.toUShort()
            flag = flag or BaseAnimationSet.RUN_ANIM_BACK_FLAG
        }
        if (runAnimLeft != Int.MIN_VALUE) {
            bas.runAnimLeft = runAnimLeft.toUShort()
            flag = flag or BaseAnimationSet.RUN_ANIM_LEFT_FLAG
        }
        if (runAnimRight != Int.MIN_VALUE) {
            bas.runAnimRight = runAnimRight.toUShort()
            flag = flag or BaseAnimationSet.RUN_ANIM_RIGHT_FLAG
        }
        if (crawlAnim != Int.MIN_VALUE) {
            bas.crawlAnim = crawlAnim.toUShort()
            flag = flag or BaseAnimationSet.CRAWL_ANIM_FLAG
        }
        if (crawlAnimBack != Int.MIN_VALUE) {
            bas.crawlAnimBack = crawlAnimBack.toUShort()
            flag = flag or BaseAnimationSet.CRAWL_ANIM_BACK_FLAG
        }
        if (crawlAnimLeft != Int.MIN_VALUE) {
            bas.crawlAnimLeft = crawlAnimLeft.toUShort()
            flag = flag or BaseAnimationSet.CRAWL_ANIM_LEFT_FLAG
        }
        if (crawlAnimRight != Int.MIN_VALUE) {
            bas.crawlAnimRight = crawlAnimRight.toUShort()
            flag = flag or BaseAnimationSet.CRAWL_ANIM_RIGHT_FLAG
        }
        if (readyAnim != Int.MIN_VALUE) {
            bas.readyAnim = readyAnim.toUShort()
            flag = flag or BaseAnimationSet.READY_ANIM_FLAG
        }
        bas.overrides = flag
        flags = flags or BAS_CHANGE
    }

    /**
     * Resets any cached base animation set values, making the NPC identical to that
     * from the cache as far as base animations go.
     */
    public fun resetBaseAnimationSet() {
        checkCommunicationThread()
        val bas = blocks.baseAnimationSet
        if (bas.overrides == 0) return
        bas.overrides = 0
        flags = flags or BAS_CHANGE
    }

    /**
     * Sets the ready animation of this NPC to the provided [id].
     * @param id the ready animation id
     */
    public fun setReadyAnim(id: Int) {
        setBaseAnimationSet(readyAnim = id)
    }

    /**
     * Sets the turn left and turn right animations of this NPC.
     * @param turnLeftAnim the animation used when the NPC turns to the left, or null if
     * turn left animation should be skipped
     * @param turnRightAnim the animation used when the NPC turns to the right, or null if
     * turn right animation should be skipped.
     */
    public fun setTurnAnims(
        turnLeftAnim: Int?,
        turnRightAnim: Int?,
    ) {
        setBaseAnimationSet(
            turnLeftAnim = turnLeftAnim ?: Int.MIN_VALUE,
            turnRightAnim = turnRightAnim ?: Int.MIN_VALUE,
        )
    }

    /**
     * Sets the walk animations of this NPC. If any of the animations is null, that animation
     * will not be overwritten by the client, allowing a subset of the below animations
     * to be overridden.
     * @param walkAnim the animation used when the NPC walks forward
     * @param walkAnimBack the animation used when the NPC walks backwards
     * @param walkAnimLeft the animation used when the NPC walks to the left
     * @param walkAnimRight the animation used when the NPC walks to the right
     */
    public fun setWalkAnims(
        walkAnim: Int?,
        walkAnimBack: Int?,
        walkAnimLeft: Int?,
        walkAnimRight: Int?,
    ) {
        setBaseAnimationSet(
            walkAnim = walkAnim ?: Int.MIN_VALUE,
            walkAnimBack = walkAnimBack ?: Int.MIN_VALUE,
            walkAnimLeft = walkAnimLeft ?: Int.MIN_VALUE,
            walkAnimRight = walkAnimRight ?: Int.MIN_VALUE,
        )
    }

    /**
     * Sets the run animations of this NPC. If any of the animations is null, that animation
     * will not be overwritten by the client, allowing a subset of the below animations
     * to be overridden.
     * @param runAnim the animation used when the NPC runs forward
     * @param runAnimBack the animation used when the NPC runs backwards
     * @param runAnimLeft the animation used when the NPC runs to the left
     * @param runAnimRight the animation used when the NPC runs to the right
     */
    public fun setRunAnims(
        runAnim: Int?,
        runAnimBack: Int?,
        runAnimLeft: Int?,
        runAnimRight: Int?,
    ) {
        setBaseAnimationSet(
            runAnim = runAnim ?: Int.MIN_VALUE,
            runAnimBack = runAnimBack ?: Int.MIN_VALUE,
            runAnimLeft = runAnimLeft ?: Int.MIN_VALUE,
            runAnimRight = runAnimRight ?: Int.MIN_VALUE,
        )
    }

    /**
     * Sets the crawl animations of this NPC. If any of the animations is null, that animation
     * will not be overwritten by the client, allowing a subset of the below animations
     * to be overridden.
     * @param crawlAnim the animation used when the NPC crawls forward
     * @param crawlAnimBack the animation used when the NPC crawls backwards
     * @param crawlAnimLeft the animation used when the NPC crawls to the left
     * @param crawlAnimRight the animation used when the NPC crawls to the right
     */
    public fun setCrawlAnims(
        crawlAnim: Int?,
        crawlAnimBack: Int?,
        crawlAnimLeft: Int?,
        crawlAnimRight: Int?,
    ) {
        setBaseAnimationSet(
            crawlAnim = crawlAnim ?: Int.MIN_VALUE,
            crawlAnimBack = crawlAnimBack ?: Int.MIN_VALUE,
            crawlAnimLeft = crawlAnimLeft ?: Int.MIN_VALUE,
            crawlAnimRight = crawlAnimRight ?: Int.MIN_VALUE,
        )
    }

    /**
     * Changes the head icon of a NPC to the sprite at the provided group and sprite index.
     * @param slot the slot of the headicon, a value of 0-8 (exclusive)
     * @param group the sprite group id in the cache.
     * @param index the index of the sprite in that sprite file, as sprite files contain
     * multiple sprites together.
     */
    @Deprecated(
        message = "Deprecated. Use setHeadIconChange(slot, group, index) for consistency.",
        replaceWith = ReplaceWith("setHeadIconChange(slot, group, index)"),
    )
    public fun headIconChange(
        slot: Int,
        group: Int,
        index: Int,
    ) {
        setHeadIconChange(slot, group, index)
    }

    /**
     * Changes the head icon of a NPC to the sprite at the provided group and sprite index.
     * @param slot the slot of the headicon, a value of 0-8 (exclusive)
     * @param group the sprite group id in the cache.
     * @param index the index of the sprite in that sprite file, as sprite files contain
     * multiple sprites together.
     */
    public fun setHeadIconChange(
        slot: Int,
        group: Int,
        index: Int,
    ) {
        checkCommunicationThread()
        verify {
            require(slot in 0..<8) {
                "Unexpected headicon slot: $slot, expected slot range: 0..<8"
            }
            require(index == -1 || index in UNSIGNED_SHORT_RANGE) {
                "Unexpected headicon index: $index, expected value -1 or in range $UNSIGNED_SHORT_RANGE"
            }
        }
        val headIcons = blocks.headIconCustomisation
        headIcons.headIconGroups[slot] = group
        headIcons.headIconIndices[slot] = index.toShort()
        headIcons.flag = headIcons.flag or (1 shl slot)
        flags = flags or HEADICON_CUSTOMISATION
    }

    /**
     * Resets the head icon at the specified [slot].
     * @param slot the slot of the head icon to reset.
     */
    public fun resetHeadIcon(slot: Int) {
        checkCommunicationThread()
        verify {
            require(slot in 0..<8) {
                "Unexpected headicon slot: $slot, expected slot range: 0..<8"
            }
        }
        val headIcons = blocks.headIconCustomisation
        headIcons.headIconGroups[slot] = -1
        headIcons.headIconIndices[slot] = -1
        headIcons.flag = headIcons.flag or (1 shl slot)
        flags = flags or HEADICON_CUSTOMISATION
    }

    /**
     * Resets all head icons which have been modified in the past.
     * If the given avatar has had no headicon changes, this function will
     * have no effect.
     */
    public fun resetHeadIcons() {
        checkCommunicationThread()
        val headIcons = blocks.headIconCustomisation
        if (headIcons.flag == 0) return
        for (slot in 0..<8) {
            headIcons.headIconGroups[slot] = -1
            headIcons.headIconIndices[slot] = -1
            headIcons.flag = headIcons.flag or (1 shl slot)
        }
        flags = flags or HEADICON_CUSTOMISATION
    }

    /**
     * Resets any chathead customisations applied to this NPC.
     */
    public fun resetHeadCustomisations() {
        checkCommunicationThread()
        blocks.headCustomisation.customisation = null
        flags = flags or HEAD_CUSTOMISATION
    }

    /**
     * Sets the chathead of the NPC to be a mirror of the local player's own chathead.
     */
    public fun setHeadCustomisationMirrored() {
        checkCommunicationThread()
        blocks.headCustomisation.customisation =
            TypeCustomisation(
                emptyList(),
                emptyList(),
                emptyList(),
                true,
            )
        flags = flags or HEAD_CUSTOMISATION
    }

    /**
     * Sets the chat head customisation for this NPC.
     * @param models the list of models to override; if the list is empty, models are not overridden.
     * @param recolours the list of recolours to apply to this NPC; if the list is empty,
     * recolours are not applied. If recolours are provided, the server MUST ensure that the number of recolours
     * matches the number of source colours defined on the NPC in the cache, as the client reads based on the
     * cache configuration.
     * @param retextures the list of retextures to apply to this NPC; if the list is empty,
     * retextures are not applied. If retextures are provided, the server MUST ensure that the number of retextures
     * matches the number of source textures defined on the NPC in the cache, as the client reads based on the
     * cache configuration.
     */
    public fun setHeadCustomisation(
        models: List<Int>,
        recolours: List<Int>,
        retextures: List<Int>,
    ) {
        checkCommunicationThread()
        blocks.headCustomisation.customisation =
            TypeCustomisation(
                models,
                recolours,
                retextures,
                false,
            )
        flags = flags or HEAD_CUSTOMISATION
    }

    /**
     * Resets any NPC body customisations applied.
     */
    public fun resetBodyCustomisations() {
        checkCommunicationThread()
        blocks.bodyCustomisation.customisation = null
        flags = flags or BODY_CUSTOMISATION
    }

    /**
     * Sets the NPC to mirror the body of the local player in its entirety, including any worn gear.
     */
    public fun setBodyCustomisationMirrored() {
        checkCommunicationThread()
        blocks.bodyCustomisation.customisation =
            TypeCustomisation(
                emptyList(),
                emptyList(),
                emptyList(),
                true,
            )
        flags = flags or BODY_CUSTOMISATION
    }

    /**
     * Sets the NPC body customisation for this NPC.
     * @param models the list of models to override; if the list is empty, models are not overridden.
     * @param recolours the list of recolours to apply to this NPC; if the list is empty,
     * recolours are not applied. If recolours are provided, the server MUST ensure that the number of recolours
     * matches the number of source colours defined on the NPC in the cache, as the client reads based on the
     * cache configuration.
     * @param retextures the list of retextures to apply to this NPC; if the list is empty,
     * retextures are not applied. If retextures are provided, the server MUST ensure that the number of retextures
     * matches the number of source textures defined on the NPC in the cache, as the client reads based on the
     * cache configuration.
     */
    public fun setBodyCustomisation(
        models: List<Int>,
        recolours: List<Int>,
        retextures: List<Int>,
    ) {
        checkCommunicationThread()
        blocks.bodyCustomisation.customisation =
            TypeCustomisation(
                models,
                recolours,
                retextures,
                false,
            )
        flags = flags or BODY_CUSTOMISATION
    }

    /**
     * Clears any transient information and resets the flag to zero at the end of the cycle.
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
        blocks.sequence.clear()
        blocks.facePathingEntity.clear()
        blocks.say.clear()
        blocks.exactMove.clear()
        blocks.spotAnims.clear()
        blocks.hit.clear()
        blocks.tinting.clear()
        blocks.faceCoord.clear()
        blocks.transformation.clear()
        blocks.bodyCustomisation.clear()
        blocks.headCustomisation.clear()
        blocks.combatLevelChange.clear()
        blocks.visibleOps.clear()
        blocks.nameChange.clear()
        blocks.headIconCustomisation.clear()
        blocks.baseAnimationSet.clear()
    }

    /**
     * Checks if the avatar has any extended info flagged.
     * @return whether any extended info flags are set.
     */
    internal fun hasExtendedInfo(): Boolean {
        return this.flags != 0
    }

    /**
     * Pre-computes all the buffers for this avatar.
     * Pre-computation is done, so we don't have to calculate these extended info blocks
     * for every avatar that observes us. Instead, we can do more performance-efficient
     * operations of native memory copying to get the latest extended info blocks.
     */
    internal fun precompute() {
        // Hits and tinting do not get precomputed

        precomputeCached()
        if (flags and SEQUENCE != 0) {
            blocks.sequence.precompute(allocator, huffmanCodec)
        }
        if (flags and SAY != 0) {
            blocks.say.precompute(allocator, huffmanCodec)
        }
        if (flags and EXACT_MOVE != 0) {
            blocks.exactMove.precompute(allocator, huffmanCodec)
        }
        if (flags and SPOTANIM != 0) {
            blocks.spotAnims.precompute(allocator, huffmanCodec)
        }
        if (flags and TINTING != 0) {
            blocks.tinting.precompute(allocator, huffmanCodec)
        }
        if (flags and FACE_COORD != 0) {
            blocks.faceCoord.precompute(allocator, huffmanCodec)
        }
        if (flags and TRANSFORMATION != 0) {
            blocks.transformation.precompute(allocator, huffmanCodec)
        }
    }

    /**
     * Precomputes the extended info blocks which are cached and potentially transmitted
     * to any players who newly observe this npc. The full list of extended info blocks
     * which must be placed in here is seen in [getLowToHighResChangeExtendedInfoFlags].
     * Every condition there must be among this function, else it is possible to run into
     * scenarios where a block isn't computed but is required in the future.
     */
    internal fun precomputeCached() {
        if (flags and OPS != 0) {
            blocks.visibleOps.precompute(allocator, huffmanCodec)
        }
        if (flags and HEADICON_CUSTOMISATION != 0) {
            blocks.headIconCustomisation.precompute(allocator, huffmanCodec)
        }
        if (flags and NAME_CHANGE != 0) {
            blocks.nameChange.precompute(allocator, huffmanCodec)
        }
        if (flags and HEAD_CUSTOMISATION != 0) {
            blocks.headCustomisation.precompute(allocator, huffmanCodec)
        }
        if (flags and BODY_CUSTOMISATION != 0) {
            blocks.bodyCustomisation.precompute(allocator, huffmanCodec)
        }
        if (flags and LEVEL_CHANGE != 0) {
            blocks.combatLevelChange.precompute(allocator, huffmanCodec)
        }
        if (flags and FACE_PATHINGENTITY != 0) {
            blocks.facePathingEntity.precompute(allocator, huffmanCodec)
        }
        if (flags and BAS_CHANGE != 0) {
            blocks.baseAnimationSet.precompute(allocator, huffmanCodec)
        }
        if (flags and TRANSFORMATION != 0) {
            blocks.transformation.precompute(allocator, huffmanCodec)
        }
    }

    /**
     * Writes the extended info block of this avatar for the given observer.
     * @param oldSchoolClientType the client that the observer is using.
     * @param buffer the buffer into which the extended info block should be written.
     * @param observerIndex index of the player avatar that is observing us.
     * @param remainingAvatars the number of avatars that must still be updated for
     * the given [observerIndex], necessary to avoid memory overflow.
     */
    internal fun pExtendedInfo(
        oldSchoolClientType: OldSchoolClientType,
        buffer: JagByteBuf,
        observerIndex: Int,
        extendedIndex: Int,
        remainingAvatars: Int,
        extraFlag: Int,
    ) {
        val flag = this.flags or extraFlag
        // We _cannot_ skip the very first avatar that is meant to have extended info.
        // If our NPC info only has a single byte for extended info written as a whole,
        // the protocol will fail the `16 + 12` check (due to terminator being 16 bits,
        // plus the single byte extended info - falling below the required 28 bits threshold)
        // By ensuring the flag isn't written as zero (it can never be zero if this function
        // is executed), we ensure that at least two bytes are being written for extended
        // info as a whole - since there are no extended info blocks which write no information.
        if (extendedIndex > 0 &&
            !filter.accept(
                buffer.writableBytes(),
                flag,
                remainingAvatars,
                false,
            )
        ) {
            buffer.p1(0)
            return
        }
        val writer =
            requireNotNull(writers[oldSchoolClientType.id]) {
                "Extended info writer missing for client $oldSchoolClientType"
            }

        writer.pExtendedInfo(
            buffer,
            avatarIndex,
            observerIndex,
            flag,
            blocks,
        )
    }

    /**
     * Gets the set of extended info blocks that were previously set but also
     * need to be transmitted to any new users.
     * @return the bit flag of all the non-transient extended info blocks that were previously flagged.
     */
    internal fun getLowToHighResChangeExtendedInfoFlags(): Int {
        var flag = 0
        if (this.flags and OPS == 0 &&
            blocks.visibleOps.ops != VisibleOps.DEFAULT_OPS
        ) {
            flag = flag or OPS
        }
        if (this.flags and HEADICON_CUSTOMISATION == 0 &&
            blocks.headIconCustomisation.flag != HeadIconCustomisation.DEFAULT_FLAG
        ) {
            flag = flag or HEADICON_CUSTOMISATION
        }
        if (this.flags and NAME_CHANGE == 0 &&
            blocks.nameChange.name != null
        ) {
            flag = flag or NAME_CHANGE
        }
        if (this.flags and HEAD_CUSTOMISATION == 0 &&
            blocks.headCustomisation.customisation != null
        ) {
            flag = flag or HEAD_CUSTOMISATION
        }
        if (this.flags and BODY_CUSTOMISATION == 0 &&
            blocks.bodyCustomisation.customisation != null
        ) {
            flag = flag or BODY_CUSTOMISATION
        }
        if (this.flags and LEVEL_CHANGE == 0 &&
            blocks.combatLevelChange.level !=
            CombatLevelChange.DEFAULT_LEVEL_OVERRIDE
        ) {
            flag = flag or LEVEL_CHANGE
        }
        if (this.flags and FACE_PATHINGENTITY == 0 &&
            blocks.facePathingEntity.index != FacePathingEntity.DEFAULT_VALUE
        ) {
            flag = flag or FACE_PATHINGENTITY
        }
        if (this.flags and BAS_CHANGE == 0 &&
            blocks.baseAnimationSet.overrides != BaseAnimationSet.DEFAULT_OVERRIDES_FLAG
        ) {
            flag = flag or BAS_CHANGE
        }
        if (this.flags and TRANSFORMATION == 0 &&
            blocks.transformation.id.toInt() in EXTENDED_NPC_ID_RANGE
        ) {
            flag = flag or TRANSFORMATION
        }
        return flag
    }

    /**
     * Clears any transient extended info that was flagged in this cycle.
     */
    private fun clearTransientExtendedInformation() {
        val flags = this.flags
        if (flags == 0) return
        if (flags and SEQUENCE != 0) {
            blocks.sequence.clear()
        }
        if (flags and SAY != 0) {
            blocks.say.clear()
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
        if (flags and FACE_COORD != 0) {
            blocks.faceCoord.clear()
        }
        // While this is a persistent flag, we still need to clear any "resets",
        // so we aren't consistently sending "clear this head icon change" to any
        // future players even though there hasn't been a headicon change in a while.
        if (flags and HEADICON_CUSTOMISATION != 0) {
            val headIcons = blocks.headIconCustomisation
            val iconFlag = headIcons.flag
            for (i in 0..<8) {
                if (iconFlag and (1 shl i) == 0) continue
                val group = headIcons.headIconGroups[i]
                if (group != -1) continue
                val index = headIcons.headIconIndices[i].toInt()
                if (index != -1) continue
                // Unflag any headicons which were reset, to avoid transmitting to new future
                // observers - the NPC will by default not have any headicons anyway
                headIcons.flag = headIcons.flag and (1 shl i).inv()
            }
        }
    }

    override fun toString(): String =
        "NpcAvatarExtendedInfo(" +
            "avatarIndex=$avatarIndex, " +
            "flags=$flags" +
            ")"

    public companion object {
        private val SIGNED_BYTE_RANGE: IntRange = Byte.MIN_VALUE.toInt()..Byte.MAX_VALUE.toInt()
        private val UNSIGNED_BYTE_RANGE: IntRange = UByte.MIN_VALUE.toInt()..UByte.MAX_VALUE.toInt()
        private val UNSIGNED_SHORT_RANGE: IntRange = UShort.MIN_VALUE.toInt()..UShort.MAX_VALUE.toInt()
        private val UNSIGNED_SMART_1_OR_2_RANGE: IntRange = 0..0x7FFF
        private val EXTENDED_NPC_ID_RANGE: IntRange = 16384..65534
        private val HIT_TYPE_RANGE: IntRange = -1..0x7FFD

        // Observer-dependent flags, utilizing the lowest bits as we store observer flags in a byte array
        // IMPORTANT: As we store it in a byte array, we currently only support 8 blocks
        // all of which are currently filled. If more are needed, the data structure needs
        // to be updated to a short array.
        public const val OPS: Int = 0x1
        public const val HEADICON_CUSTOMISATION: Int = 0x2
        public const val NAME_CHANGE: Int = 0x4
        public const val HEAD_CUSTOMISATION: Int = 0x8
        public const val BODY_CUSTOMISATION: Int = 0x10
        public const val LEVEL_CHANGE: Int = 0x20
        public const val FACE_PATHINGENTITY: Int = 0x40
        public const val BAS_CHANGE: Int = 0x80

        // "Static" flags, the bit values here are irrelevant
        public const val TINTING: Int = 0x100
        public const val SAY: Int = 0x200
        public const val HITS: Int = 0x400
        public const val FACE_COORD: Int = 0x800
        public const val TRANSFORMATION: Int = 0x1000
        public const val SEQUENCE: Int = 0x2000
        public const val EXACT_MOVE: Int = 0x4000
        public const val SPOTANIM: Int = 0x8000

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
            extendedInfoWriters: List<NpcAvatarExtendedInfoWriter>,
        ): Array<NpcAvatarExtendedInfoWriter?> {
            val array =
                arrayOfNulls<NpcAvatarExtendedInfoWriter>(
                    OldSchoolClientType.COUNT,
                )
            for (writer in extendedInfoWriters) {
                array[writer.oldSchoolClientType.id] = writer
            }
            return array
        }
    }
}
