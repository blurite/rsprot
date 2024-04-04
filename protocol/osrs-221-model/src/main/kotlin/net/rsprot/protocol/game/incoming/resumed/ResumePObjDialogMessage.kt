package net.rsprot.protocol.game.incoming.resumed

import net.rsprot.protocol.message.IncomingMessage

/**
 * Resume p obj dialogue is sent when the user selects an obj from the
 * Grand Exchange item search box, however this packet is not necessarily
 * exclusive to that feature, and can be used in other pieces of content.
 * @property obj the id of the obj selected
 */
public class ResumePObjDialogMessage(
    public val obj: Int,
) : IncomingMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResumePObjDialogMessage

        return obj == other.obj
    }

    override fun hashCode(): Int {
        return obj
    }

    override fun toString(): String {
        return "ResumePObjDialogMessage(obj=$obj)"
    }
}
