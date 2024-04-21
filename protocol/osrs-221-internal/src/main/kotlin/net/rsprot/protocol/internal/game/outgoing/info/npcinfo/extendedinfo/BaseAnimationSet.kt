package net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo

import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.internal.platform.PlatformMap

public class BaseAnimationSet(
    override val encoders: PlatformMap<PrecomputedExtendedInfoEncoder<BaseAnimationSet>>,
) : TransientExtendedInfo<BaseAnimationSet, PrecomputedExtendedInfoEncoder<BaseAnimationSet>>() {
    public var overrides: Int = DEFAULT_OVERRIDES_FLAG
    public var turnLeftAnim: UShort = 0xFFFFu
    public var turnRightAnim: UShort = 0xFFFFu
    public var walkAnim: UShort = 0xFFFFu
    public var walkAnimBack: UShort = 0xFFFFu
    public var walkAnimLeft: UShort = 0xFFFFu
    public var walkAnimRight: UShort = 0xFFFFu
    public var runAnim: UShort = 0xFFFFu
    public var runAnimBack: UShort = 0xFFFFu
    public var runAnimLeft: UShort = 0xFFFFu
    public var runAnimRight: UShort = 0xFFFFu
    public var crawlAnim: UShort = 0xFFFFu
    public var crawlAnimBack: UShort = 0xFFFFu
    public var crawlAnimLeft: UShort = 0xFFFFu
    public var crawlAnimRight: UShort = 0xFFFFu
    public var readyAnim: UShort = 0xFFFFu

    override fun clear() {
        releaseBuffers()
        overrides = DEFAULT_OVERRIDES_FLAG
        turnLeftAnim = 0xFFFFu
        turnRightAnim = 0xFFFFu
        walkAnim = 0xFFFFu
        walkAnimBack = 0xFFFFu
        walkAnimLeft = 0xFFFFu
        walkAnimRight = 0xFFFFu
        runAnim = 0xFFFFu
        runAnimBack = 0xFFFFu
        runAnimLeft = 0xFFFFu
        runAnimRight = 0xFFFFu
        crawlAnim = 0xFFFFu
        crawlAnimBack = 0xFFFFu
        crawlAnimLeft = 0xFFFFu
        crawlAnimRight = 0xFFFFu
        readyAnim = 0xFFFFu
    }

    public companion object {
        public const val DEFAULT_OVERRIDES_FLAG: Int = 0
        public const val TURN_LEFT_ANIM_FLAG: Int = 0x1
        public const val TURN_RIGHT_ANIM_FLAG: Int = 0x2
        public const val WALK_ANIM_FLAG: Int = 0x4
        public const val WALK_ANIM_BACK_FLAG: Int = 0x8
        public const val WALK_ANIM_LEFT_FLAG: Int = 0x10
        public const val WALK_ANIM_RIGHT_FLAG: Int = 0x20
        public const val RUN_ANIM_FLAG: Int = 0x40
        public const val RUN_ANIM_BACK_FLAG: Int = 0x80
        public const val RUN_ANIM_LEFT_FLAG: Int = 0x100
        public const val RUN_ANIM_RIGHT_FLAG: Int = 0x200
        public const val CRAWL_ANIM_FLAG: Int = 0x400
        public const val CRAWL_ANIM_BACK_FLAG: Int = 0x800
        public const val CRAWL_ANIM_LEFT_FLAG: Int = 0x1000
        public const val CRAWL_ANIM_RIGHT_FLAG: Int = 0x2000
        public const val READY_ANIM_FLAG: Int = 0x4000
    }
}
