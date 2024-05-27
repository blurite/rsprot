package net.rsprot.protocol.js5.incoming

import net.rsprot.protocol.message.IncomingJs5Message

public class XorChange(
    public val key: Int,
) : IncomingJs5Message {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is XorChange) return false

        if (key != other.key) return false

        return true
    }

    override fun hashCode(): Int {
        return key
    }

    override fun toString(): String {
        return "XorChange(key=$key)"
    }
}
