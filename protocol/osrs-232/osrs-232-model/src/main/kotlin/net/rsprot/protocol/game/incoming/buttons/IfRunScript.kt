package net.rsprot.protocol.game.incoming.buttons

import io.netty.buffer.ByteBuf
import net.rsprot.buffer.extensions.g1
import net.rsprot.buffer.extensions.gSmart1or2
import net.rsprot.buffer.extensions.gVarInt2s
import net.rsprot.buffer.extensions.gjstr
import net.rsprot.buffer.extensions.toByteArray
import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.game.incoming.GameClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprot.protocol.message.toIntOrMinusOne
import net.rsprot.protocol.util.CombinedId

/**
 * IfRunScript is used by the client to run a server script that is associated with the component.
 * @property combinedId the bitpacked combination of [interfaceId] and [componentId].
 * @property interfaceId the interface id the player interacted with
 * @property componentId the component id on that interface the script is associated to
 * @property sub the subcomponent within that component if it has one, otherwise -1
 * @property obj the obj in that subcomponent, or -1
 * @property script the id of the server script to invoke
 * @property buffer the byte buffer containing the values sent by the packet.
 * As the packet itself does not define the types, it isn't possible to immediately decode the
 * buffer as with most packets. We require the server to provide the instructions on how the
 * buffer should be decoded for the provided server script id.
 */
@Suppress("MemberVisibilityCanBePrivate")
public class IfRunScript private constructor(
    private val _combinedId: CombinedId,
    private val _sub: UShort,
    private val _obj: UShort,
    public val script: Int,
    public val buffer: ByteBuf,
) : IncomingGameMessage {
    public constructor(
        combinedId: CombinedId,
        sub: Int,
        obj: Int,
        script: Int,
        buffer: ByteBuf,
    ) : this(
        combinedId,
        sub.toUShort(),
        obj.toUShort(),
        script,
        buffer,
    )

    public val combinedId: Int
        get() = _combinedId.combinedId
    public val interfaceId: Int
        get() = _combinedId.interfaceId
    public val componentId: Int
        get() = _combinedId.componentId
    public val sub: Int
        get() = _sub.toIntOrMinusOne()
    public val obj: Int
        get() = _obj.toIntOrMinusOne()

    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    /**
     * Decodes the script's arguments using the provided parameter types.
     * The buffer is always released after this function is invoked. It should be invoked
     * even if there are no parameters to decode, to release the empty buffer.
     * @param parameterTypes the parameter types to decode the script with. These must match
     * the ones client used to encode the packet, or an exception will be thrown.
     * @return a list of arguments passed on by the client.
     */
    public fun decode(parameterTypes: ParameterTypes): Arguments {
        val buffer = this.buffer
        check(buffer.refCnt() > 0) {
            "Buffer has already been released."
        }

        try {
            val args =
                parameterTypes.chars.mapIndexed { index, char ->
                    when (char) {
                        ParameterTypes.INT_ARRAY -> {
                            val length = buffer.gSmart1or2()
                            IntArray(length) {
                                buffer.gVarInt2s()
                            }
                        }
                        ParameterTypes.STRING_ARRAY -> {
                            val length = buffer.gSmart1or2()
                            Array(length) {
                                buffer.gjstr()
                            }
                        }
                        ParameterTypes.STRING -> {
                            buffer.gjstr()
                        }
                        ParameterTypes.INT -> {
                            buffer.gVarInt2s()
                        }
                        ParameterTypes.NULL -> {
                            buffer.g1()
                        }
                        else -> {
                            throw IllegalStateException("Invalid parameter type in index $index: $char")
                        }
                    }
                }
            check(!buffer.isReadable) {
                "Buffer still has bytes in it after decoding: ${buffer.readableBytes()} " +
                    "(values: ${buffer.toByteArray().contentToString()})"
            }
            return Arguments(args)
        } finally {
            buffer.release()
        }
    }

    /**
     * A helper class to wrap server script parameter types, as the client does not transmit the types.
     * The companion object has helpers for constructing the script values.
     */
    public data class ParameterTypes(
        public val chars: CharArray,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ParameterTypes

            return chars.contentEquals(other.chars)
        }

        override fun hashCode(): Int {
            return chars.contentHashCode()
        }

        public companion object {
            public const val INT_ARRAY: Char = 'W'
            public const val STRING_ARRAY: Char = 'X'
            public const val STRING: Char = 's'
            public const val INT: Char = 'i'
            public const val NULL: Char = 0.toChar()

            public val NONE: ParameterTypes = ParameterTypes(charArrayOf())

            /**
             * A var-arg based helper for constructing parameter types.
             * Users should use the constants defined in this object
             * to declare the types to decode the script with.
             */
            @JvmStatic
            public fun of(vararg type: Char): ParameterTypes {
                return ParameterTypes(type)
            }
        }
    }

    public data class Arguments(
        public val args: List<Any>,
    ) : List<Any> by args

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfRunScript

        if (_combinedId != other._combinedId) return false
        if (_sub != other._sub) return false
        if (_obj != other._obj) return false
        if (script != other.script) return false
        if (buffer != other.buffer) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _combinedId.hashCode()
        result = 31 * result + _sub.hashCode()
        result = 31 * result + _obj.hashCode()
        result = 31 * result + script.hashCode()
        result = 31 * result + buffer.hashCode()
        return result
    }

    override fun toString(): String =
        "IfRunScript(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId, " +
            "sub=$sub, " +
            "obj=$obj, " +
            "script=$script" +
            ")"
}
