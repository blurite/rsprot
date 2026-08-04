package net.rsprot.protocol.game.incoming.misc.user

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.game.incoming.GameClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage

/**
 * Client cheats are commands sent in chat using the :: prefix,
 * or through the console on the C++ client.
 */
public class ClientCheat private constructor(
    public val command: String,
    private val _unknown: Byte,
) : IncomingGameMessage {
    public constructor(
        command: String,
        unknown: Int,
    ) : this(
        command,
        unknown.toByte(),
    )

    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    public val unknown: Int
        get() = _unknown.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClientCheat

        if (_unknown != other._unknown) return false
        if (command != other.command) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _unknown.toInt()
        result = 31 * result + command.hashCode()
        return result
    }

    override fun toString(): String {
        return "ClientCheat(" +
            "command='$command', " +
            "unknown=$unknown" +
            ")"
    }
}
