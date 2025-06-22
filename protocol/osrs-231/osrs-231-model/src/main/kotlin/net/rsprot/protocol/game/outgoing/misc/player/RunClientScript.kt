package net.rsprot.protocol.game.outgoing.misc.player

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.internal.RSProtFlags
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprot.protocol.message.util.estimateTextSize

/**
 * Run clientscript packet is used to execute a clientscript in the client
 * with the provided arguments.
 * @property id the id of the script to invoke
 * @property types the array of characters representing the clientscript types
 * to send to the client.
 * The supported types are IntArray('W'), Array<String>('X'), String('s') and Int ('i').
 * Note that IntArray(int[] in Java) specifically must be passed in,
 * and not an Array<Integer>(Integer[] in Java). Latter will throw an exception.
 * Any char code not in the aforementioned list will be treated as an int.
 * @property values the list of values to be sent in the client script.
 */
public class RunClientScript : OutgoingGameMessage {
    public val id: Int
    public val types: CharArray
    public val values: List<Any>

    /**
     * A primary constructor that allows ones to pass in the types from the server, in case one wishes
     * to provide accurate types. The client however only cares whether the type is a string, or isn't a string,
     * and the exact type values get discarded.
     */
    public constructor(
        id: Int,
        types: CharArray,
        values: List<Any>,
    ) {
        this.id = id
        this.types = types
        this.values = values
        if (RSProtFlags.clientscriptVerification) {
            require(types.size == values.size) {
                "Types and values sizes must match: ${types.size}, ${values.size}"
            }
            for (i in types.indices) {
                val type = types[i]
                val value = values[i]
                when (type) {
                    'W' -> {
                        require(value is IntArray) {
                            "Expected IntArray(int[] in Java) value at index $i for char $type, got: $value"
                        }
                    }
                    'X' -> {
                        require(value is Array<*>) {
                            "Expected Array<String>(String[] in Java) value at index $i for char $type, got: $value"
                        }
                        for (index in value.indices) {
                            val element = value[index]
                            require(element is String) {
                                "Only string elements expected in string arrays; " +
                                    "index $index in array is not a string: $element " +
                                    "(full array: ${value.contentToString()})"
                            }
                        }
                    }
                    's' -> {
                        require(value is String) {
                            "Expected string value at index $i for char $type, got: $value"
                        }
                    }
                    else -> {
                        require(value is Int) {
                            "Expected int value at index $i for char $type, got: $value"
                        }
                    }
                }
            }
        }
    }

    /**
     * A secondary constructor that allows one to only pass in the values and infer the types from the
     * values. All values must be IntArray, Array<String>, Int or String types, and may be mixed.
     * As client discards the actual types, there's no value in providing the exact type values to the
     * client, and we can simply infer this the same way client reverses it.
     */
    public constructor(
        id: Int,
        values: List<Any>,
    ) {
        this.id = id
        this.values = values
        this.types =
            CharArray(values.size) { index ->
                when (val value = values[index]) {
                    is IntArray -> 'W'
                    is Array<*> -> 'X'
                    is Int -> 'i'
                    is String -> 's'
                    else -> throw IllegalArgumentException(
                        "Unknown clientscript value type: " +
                            "${value.javaClass} @ $value, " +
                            "accepted types only include IntArray, Array<String>, Int and String.",
                    )
                }
            }
    }

    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun estimateSize(): Int {
        var payloadSize = 0
        // For clientscripts, as they can be so volatile,
        // we calculate an accurate length of the message based on highest possible values.
        for (i in (types.size - 1) downTo 0) {
            val type = types[i]
            when (type) {
                'W' -> {
                    payloadSize += VAR_INT_2_SIZE_ESTIMATE
                    val element = values[i] as IntArray
                    payloadSize += element.size * VAR_INT_2_SIZE_ESTIMATE
                }
                'X' -> {
                    payloadSize += VAR_INT_2_SIZE_ESTIMATE
                    val elements = values[i] as Array<*>
                    for (element in elements) {
                        payloadSize += estimateTextSize(element as String)
                    }
                }
                's' -> {
                    payloadSize += estimateTextSize(values[i] as String)
                }
                else -> {
                    payloadSize += Int.SIZE_BYTES
                }
            }
        }
        return types.size +
            Byte.SIZE_BYTES +
            payloadSize +
            Int.SIZE_BYTES
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

    override fun toString(): String =
        "RunClientScript(" +
            "id=$id, " +
            "types=${types.contentToString()}, " +
            "values=$values" +
            ")"

    private companion object {
        /**
         * A size estimate in bytes for VarInt2. Since the value is dynamic, we can only assume it
         * will be the highest possible size, which is 5 bytes.
         */
        private const val VAR_INT_2_SIZE_ESTIMATE: Int = 5
    }
}
