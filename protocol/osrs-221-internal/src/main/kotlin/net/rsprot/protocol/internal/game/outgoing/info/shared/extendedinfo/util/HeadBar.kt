package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util

public class HeadBar(
    public var id: UShort,
    public var startFill: UByte,
    public var endFill: UByte,
    public var endTime: UShort,
    public var startTime: UShort,
) {
    public companion object {
        public const val REMOVED: UShort = 0x7FFFu
    }
}
