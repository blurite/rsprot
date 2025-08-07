package net.rsprot.protocol.game.incoming.misc.client

import io.netty.buffer.ByteBuf
import net.rsprot.buffer.extensions.checkCRC32
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.game.incoming.GameClientProtCategory
import net.rsprot.protocol.game.outgoing.misc.client.ReflectionChecker
import net.rsprot.protocol.message.IncomingGameMessage
import java.io.IOException
import java.io.InvalidClassException
import java.io.OptionalDataException
import java.io.StreamCorruptedException
import java.lang.reflect.InvocationTargetException
import kotlin.IllegalArgumentException

/**
 * A reflection check reply is sent by the client whenever a server requests
 * a reflection checker to be performed.
 * @property id the original request id sent by the server.
 * @property result the resulting byte buffer slice.
 * As decoding reflection checks requires knowing the original request that was made,
 * we have to defer the decoding of the payload until the original request is
 * provided to us, thus, using [decode] we can obtain the real results.
 */
public class ReflectionCheckReply(
    public val id: Int,
    public val result: ByteBuf,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.CLIENT_EVENT

    /**
     * Decodes the reply using the original [request] that the server put in.
     * It is worth noting that the [result] buffer will always be released
     * after the decoding function call, so it may only be called once.
     */
    public fun decode(request: ReflectionChecker): List<ReflectionCheckResult<*>> {
        try {
            val buffer = result.toJagByteBuf()
            // Skip the id, it is necessary for CRC verification though.
            buffer.skipRead(4)
            val results = ArrayList<ReflectionCheckResult<*>>(request.checks.size)
            for (check in request.checks) {
                val opcode = buffer.g1s()
                if (opcode < 0) {
                    if (opcode <= -10) {
                        val throwable = getExecutionThrowableClass(opcode)
                        results +=
                            ErrorResult(
                                check,
                                ErrorResult.ThrowableResultType.ExecutionThrowable(throwable),
                            )
                    } else {
                        val throwable = getConstructionThrowableClass(opcode)
                        results +=
                            ErrorResult(
                                check,
                                ErrorResult.ThrowableResultType.ConstructionThrowable(throwable),
                            )
                    }
                    continue
                }
                when (check) {
                    is ReflectionChecker.GetFieldValue -> {
                        val result = buffer.g4()
                        results += GetFieldValueResult(check, result)
                    }

                    is ReflectionChecker.SetFieldValue -> {
                        results += SetFieldValueResult(check)
                    }

                    is ReflectionChecker.GetFieldModifiers -> {
                        val modifiers = buffer.g4()
                        results += GetFieldModifiersResult(check, modifiers)
                    }

                    is ReflectionChecker.InvokeMethod -> {
                        results +=
                            when (opcode) {
                                0 -> InvokeMethodResult(check, NullReturnValue)
                                1 -> InvokeMethodResult(check, NumberReturnValue(buffer.g8()))
                                2 -> InvokeMethodResult(check, StringReturnValue(buffer.gjstr()))
                                4 -> InvokeMethodResult(check, UnknownReturnValue)
                                else -> throw IllegalStateException("Unknown opcode for method invocation: $opcode")
                            }
                    }

                    is ReflectionChecker.GetMethodModifiers -> {
                        val modifiers = buffer.g4()
                        results += GetMethodModifiersResult(check, modifiers)
                    }
                }
            }
            result.readerIndex(result.writerIndex())
            if (!result.checkCRC32()) {
                throw IllegalStateException("CRC mismatch!")
            }
            return results
        } finally {
            result.release()
        }
    }

    /**
     * Gets the throwable class corresponding to each opcode during the reflection check execution.
     * @param opcode the opcode value
     * @return the throwable class corresponding to that opcode
     */
    private fun getExecutionThrowableClass(opcode: Int): Class<out Throwable> =
        when (opcode) {
            -10 -> ClassNotFoundException::class.java
            -11 -> InvalidClassException::class.java
            -12 -> StreamCorruptedException::class.java
            -13 -> OptionalDataException::class.java
            -14 -> IllegalAccessException::class.java
            -15 -> IllegalArgumentException::class.java
            -16 -> InvocationTargetException::class.java
            -17 -> SecurityException::class.java
            -18 -> IOException::class.java
            -19 -> NullPointerException::class.java
            -20 -> Exception::class.java
            -21 -> Throwable::class.java
            else -> throw IllegalArgumentException("Unknown execution throwable opcode: $opcode")
        }

    /**
     * Gets the throwable class corresponding to each opcode during the reflection check construction.
     * @param opcode the opcode value
     * @return the throwable class corresponding to that opcode
     */
    private fun getConstructionThrowableClass(opcode: Int): Class<out Throwable> =
        when (opcode) {
            -1 -> ClassNotFoundException::class.java
            -2 -> SecurityException::class.java
            -3 -> NullPointerException::class.java
            -4 -> Exception::class.java
            -5 -> Throwable::class.java
            else -> throw IllegalArgumentException("Unknown construction throwable opcode: $opcode")
        }

    override fun toString(): String =
        "ReflectionCheckReply(" +
            "id=$id, " +
            "result=$result" +
            ")"

    public sealed interface ReflectionCheckResult<T : ReflectionChecker.ReflectionCheck> {
        public val check: T
    }

    /**
     * Any error result will be in its own class, as there will not be any
     * return values included in this lot.
     * @property check the reflection check requested by the server
     * @property throwable the throwable class that the client received during either construction or execution.
     */
    public class ErrorResult<T : ReflectionChecker.ReflectionCheck, E : Class<out Throwable>>(
        override val check: T,
        public val throwable: ThrowableResultType<E>,
    ) : ReflectionCheckResult<T> {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ErrorResult<*, *>

            if (check != other.check) return false
            if (throwable != other.throwable) return false

            return true
        }

        override fun hashCode(): Int {
            var result = check.hashCode()
            result = 31 * result + throwable.hashCode()
            return result
        }

        override fun toString(): String =
            "ErrorResult(" +
                "check=$check, " +
                "throwable=$throwable" +
                ")"

        /**
         * The throwable result types notify the user whether the throwable was caught during the
         * construction of the reflection check where it looks up each class/field, or during
         * the execution, where it looks up or assigns new values to properties. As the exceptions
         * overlap, we need to distinguish the two types with a different wrapper.
         * @property throwableClass the class that was thrown.
         */
        public sealed interface ThrowableResultType<E : Class<out Throwable>> {
            public val throwableClass: E

            /**
             * A construction throwable is a throwable that was caught during the construction
             * of a reflection check, e.g. when looking up the classes or fields on which the operations
             * would be performed.
             */
            public class ConstructionThrowable<E : Class<out Throwable>>(
                override val throwableClass: E,
            ) : ThrowableResultType<E> {
                override fun equals(other: Any?): Boolean {
                    if (this === other) return true
                    if (other !is ConstructionThrowable<*>) return false

                    if (throwableClass != other.throwableClass) return false

                    return true
                }

                override fun hashCode(): Int {
                    return throwableClass.hashCode()
                }

                override fun toString(): String {
                    return "ConstructionThrowable(" +
                        "throwableClass=$throwableClass" +
                        ")"
                }
            }

            /**
             * An execution throwable is a throwable that was caught during the execution of a specific
             * operation that was requested, e.g. GetFieldModifiers or SetFieldValue.
             */
            public class ExecutionThrowable<E : Class<out Throwable>>(
                override val throwableClass: E,
            ) : ThrowableResultType<E> {
                override fun equals(other: Any?): Boolean {
                    if (this === other) return true
                    if (other !is ExecutionThrowable<*>) return false

                    if (throwableClass != other.throwableClass) return false

                    return true
                }

                override fun hashCode(): Int {
                    return throwableClass.hashCode()
                }

                override fun toString(): String {
                    return "ExecutionThrowable(" +
                        "throwableClass=$throwableClass" +
                        ")"
                }
            }
        }
    }

    /**
     * Get field value result provides a successful result for retrieving a
     * value of a field in the client.
     * @property check the reflection check requested by the server
     * @property value the value that the client received after invoking reflection
     */
    public class GetFieldValueResult(
        override val check: ReflectionChecker.GetFieldValue,
        public val value: Int,
    ) : ReflectionCheckResult<ReflectionChecker.GetFieldValue> {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GetFieldValueResult

            if (check != other.check) return false
            if (value != other.value) return false

            return true
        }

        override fun hashCode(): Int {
            var result = check.hashCode()
            result = 31 * result + value
            return result
        }

        override fun toString(): String =
            "GetFieldValueResult(" +
                "check=$check, " +
                "value=$value" +
                ")"
    }

    /**
     * Set field value results will only ever be successful if a value was
     * successfully assigned, in which case nothing gets returned.
     * @property check the reflection check requested by the server
     */
    public class SetFieldValueResult(
        override val check: ReflectionChecker.SetFieldValue,
    ) : ReflectionCheckResult<ReflectionChecker.SetFieldValue> {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as SetFieldValueResult

            return check == other.check
        }

        override fun hashCode(): Int = check.hashCode()

        override fun toString(): String = "SetFieldValueResult(check=$check)"
    }

    /**
     * Get field modifiers result will attempt to look up the modifiers
     * of a field.
     * @property check the reflection check requested by the server
     * @property modifiers the bitpacked modifier values as assigned by the JVM
     */
    public class GetFieldModifiersResult(
        override val check: ReflectionChecker.GetFieldModifiers,
        public val modifiers: Int,
    ) : ReflectionCheckResult<ReflectionChecker.GetFieldModifiers> {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GetFieldModifiersResult

            if (check != other.check) return false
            if (modifiers != other.modifiers) return false

            return true
        }

        override fun hashCode(): Int {
            var result = check.hashCode()
            result = 31 * result + modifiers
            return result
        }

        override fun toString(): String =
            "GetFieldModifiersResult(" +
                "check=$check, " +
                "modifiers=$modifiers" +
                ")"
    }

    /**
     * Invoke method result is sent when a method invocation was successfully
     * performed with the provided arguments and return type.
     * @property check the reflection check requested by the server
     * @property result the result of invoking the method
     */
    public class InvokeMethodResult<T : MethodInvocationReturnValue>(
        override val check: ReflectionChecker.InvokeMethod,
        public val result: T,
    ) : ReflectionCheckResult<ReflectionChecker.InvokeMethod> {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as InvokeMethodResult<*>

            if (check != other.check) return false
            if (result != other.result) return false

            return true
        }

        override fun hashCode(): Int {
            var result1 = check.hashCode()
            result1 = 31 * result1 + result.hashCode()
            return result1
        }

        override fun toString(): String =
            "InvokeMethodResult(" +
                "check=$check, " +
                "result=$result" +
                ")"
    }

    /**
     * Get method modifiers will attempt to look up the modifiers of a method
     * using reflection.
     * @property check the reflection check requested by the server
     * @property modifiers the bitpacked modifier values as assigned by the JVM
     */
    public class GetMethodModifiersResult(
        override val check: ReflectionChecker.GetMethodModifiers,
        public val modifiers: Int,
    ) : ReflectionCheckResult<ReflectionChecker.GetMethodModifiers> {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GetMethodModifiersResult

            if (check != other.check) return false
            if (modifiers != other.modifiers) return false

            return true
        }

        override fun hashCode(): Int {
            var result = check.hashCode()
            result = 31 * result + modifiers
            return result
        }

        override fun toString(): String =
            "GetMethodModifiersResult(" +
                "check=$check, " +
                "modifiers=$modifiers" +
                ")"
    }

    public sealed interface MethodInvocationReturnValue

    /**
     * A null return value is sent if a method invocation returned a null value.
     */
    public data object NullReturnValue : MethodInvocationReturnValue

    /**
     * A number return value is sent if a method returns any [Number] type,
     * in which case the client will call [java.lang.Number.longValue]
     * to retrieve the long representation of the value.
     * @property longValue the long representation of the numeric value.
     */
    public class NumberReturnValue(
        public val longValue: Long,
    ) : MethodInvocationReturnValue {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as NumberReturnValue

            return longValue == other.longValue
        }

        override fun hashCode(): Int = longValue.hashCode()

        override fun toString(): String = "NumberReturnValue(longValue=$longValue)"
    }

    /**
     * A string return value is provided if a method invocation results
     * in a string value.
     * @property stringValue the string value returned by the method.
     */
    public class StringReturnValue(
        public val stringValue: String,
    ) : MethodInvocationReturnValue {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as StringReturnValue

            return stringValue == other.stringValue
        }

        override fun hashCode(): Int = stringValue.hashCode()

        override fun toString(): String = "StringReturnValue(stringValue='$stringValue')"
    }

    /**
     * An unknown return value is provided when a method returns a value,
     * but that value is not a null, a number of a string - essentially
     * the 'else' case if all else falls through.
     */
    public data object UnknownReturnValue : MethodInvocationReturnValue
}
