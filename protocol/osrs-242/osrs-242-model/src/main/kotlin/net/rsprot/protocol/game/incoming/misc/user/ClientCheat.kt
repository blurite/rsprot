package net.rsprot.protocol.game.incoming.misc.user

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.game.incoming.GameClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage

/**
 * Client cheats are commands sent in chat using the :: prefix,
 * or through the console on the C++ client.
 * @property command the input command typed by the user
 * @property autocomplete whether the user hit the tab key on the console on the C++ client.
 */
public class ClientCheat(
    public val command: String,
    public val autocomplete: Boolean,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClientCheat

        if (autocomplete != other.autocomplete) return false
        if (command != other.command) return false

        return true
    }

    override fun hashCode(): Int {
        var result = autocomplete.hashCode()
        result = 31 * result + command.hashCode()
        return result
    }

    override fun toString(): String {
        return "ClientCheat(" +
            "command='$command', " +
            "autocomplete=$autocomplete" +
            ")"
    }
}
