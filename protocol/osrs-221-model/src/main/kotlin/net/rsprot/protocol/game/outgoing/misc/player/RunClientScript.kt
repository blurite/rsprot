package net.rsprot.protocol.game.outgoing.misc.player

import net.rsprot.protocol.internal.RSProtFlags
import net.rsprot.protocol.message.OutgoingMessage

/**
 * Run clientscript packet is used to execute a clientscript in the client
 * with the provided arguments.
 * @property id the id of the script to invoke
 * @property types the array of characters representing the clientscript types
 * to send to the client. It is important to remember that all types which
 * aren't `'s'` will be integer-based, with `'s'` being the only string-type.
 * If the given value element cannot be cast to string/int respective
 * to its type, an exception is thrown.
 * @property values the list of int or string values to be sent to the
 * client script.
 */
public class RunClientScript(
    public val id: Int,
    public val types: CharArray,
    public val values: List<Any>,
) : OutgoingMessage {
    init {
        if (RSProtFlags.clientscriptVerification) {
            require(types.size == values.size) {
                "Types and values sizes must match: ${types.size}, ${values.size}"
            }
            for (i in types.indices) {
                val type = types[i]
                val value = values[i]
                if (type == 's') {
                    require(value is String) {
                        "Expected string value at index $i for char $type, got: $value"
                    }
                } else {
                    require(value is Int) {
                        "Expected int value at index $i for char $type, got: $value"
                    }
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RunClientScript

        if (id != other.id) return false
        if (!types.contentEquals(other.types)) return false
        if (values != other.values) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + types.contentHashCode()
        result = 31 * result + values.hashCode()
        return result
    }

    override fun toString(): String {
        return "RunClientScript(" +
            "id=$id, " +
            "types=${types.contentToString()}, " +
            "values=$values" +
            ")"
    }
}
