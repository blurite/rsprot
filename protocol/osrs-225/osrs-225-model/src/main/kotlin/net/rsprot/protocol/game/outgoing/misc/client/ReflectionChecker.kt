package net.rsprot.protocol.game.outgoing.misc.client

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Reflection checker packet will attempt to use [java.lang.reflect] to
 * perform a lookup or invocation on a method or field in the client,
 * using information provided in this packet.
 * These invocations/lookups may fail completely, which is fully supported,
 * as various exceptions get caught and special return codes are provided
 * in such cases.
 * An important thing to note, however, is that the server is responsible
 * for not requesting too much, as the client's reply packet has a var-byte
 * size, meaning the entire reply for a reflection check must fit into 255
 * bytes or fewer. There is no protection against this.
 * Additionally worth noting that the [InvokeMethod] variant, while very
 * powerful, is not utilized in OldSchool, and is rather dangerous to
 * invoke due to the aforementioned size limitation.
 *
 * @property id the id of the reflection check, sent back in the reply and
 * used to link together the request and reply, which is needed to fully
 * decode the respective replies.
 * @property checks the list of reflection checks to perform.
 */
public class ReflectionChecker(
    public val id: Int,
    public val checks: List<ReflectionCheck>,
) : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ReflectionChecker

        if (id != other.id) return false
        if (checks != other.checks) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + checks.hashCode()
        return result
    }

    override fun toString(): String =
        "ReflectionChecker(" +
            "id=$id, " +
            "checks=$checks" +
            ")"

    public sealed interface ReflectionCheck

    /**
     * Get field value is a reflection check which will aim to call the
     * [java.lang.reflect.Field.getInt] function on the respective field.
     * The value is submitted back in the reply, if a value was obtained.
     * @property className the full class name in which the field exists.
     * @property fieldName the name of the field in that class to look up.
     */
    public class GetFieldValue(
        public val className: String,
        public val fieldName: String,
    ) : ReflectionCheck {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GetFieldValue

            if (className != other.className) return false
            if (fieldName != other.fieldName) return false

            return true
        }

        override fun hashCode(): Int {
            var result = className.hashCode()
            result = 31 * result + fieldName.hashCode()
            return result
        }

        override fun toString(): String =
            "GetFieldValue(" +
                "className='$className', " +
                "fieldName='$fieldName'" +
                ")"
    }

    /**
     * Set field value aims to try to assign the provided int [value] to
     * a field in the class.
     * @property className the full class name in which the field exists.
     * @property fieldName the name of the field in that class to look up.
     * @property value the value to try to assign to the field.
     */
    public class SetFieldValue(
        public val className: String,
        public val fieldName: String,
        public val value: Int,
    ) : ReflectionCheck {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as SetFieldValue

            if (className != other.className) return false
            if (fieldName != other.fieldName) return false
            if (value != other.value) return false

            return true
        }

        override fun hashCode(): Int {
            var result = className.hashCode()
            result = 31 * result + fieldName.hashCode()
            result = 31 * result + value
            return result
        }

        override fun toString(): String =
            "SetFieldValue(" +
                "className='$className', " +
                "fieldName='$fieldName', " +
                "value=$value" +
                ")"
    }

    /**
     * Get field modifiers aims to try to look up a given field's modifiers,
     * if possible.
     * @property className the full class name in which the field exists.
     * @property fieldName the name of the field in that class to look up.
     */
    public class GetFieldModifiers(
        public val className: String,
        public val fieldName: String,
    ) : ReflectionCheck {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GetFieldModifiers

            if (className != other.className) return false
            if (fieldName != other.fieldName) return false

            return true
        }

        override fun hashCode(): Int {
            var result = className.hashCode()
            result = 31 * result + fieldName.hashCode()
            return result
        }

        override fun toString(): String =
            "GetFieldModifiers(" +
                "className='$className', " +
                "fieldName='$fieldName'" +
                ")"
    }

    /**
     * Invoke method check aims to try to invoke a function in a class
     * with the provided parameters. The [parameterValues] are turned
     * into an object using [java.io.ObjectInputStream.readObject] function.
     * @property className the full name of the class in which the function lies.
     * @property methodName the name of the function to invoke.
     * @property parameterClasses the types of the parameters that the function takes.
     * @property parameterValues the values to pass into the function,
     * represented as a serialized byte array.
     * @property returnClass the full name of the return type class
     */
    public class InvokeMethod(
        public val className: String,
        public val methodName: String,
        public val parameterClasses: List<String>,
        public val parameterValues: List<ByteArray>,
        public val returnClass: String,
    ) : ReflectionCheck {
        init {
            require(parameterClasses.size == parameterValues.size) {
                "Parameter classes and values must have an equal length: " +
                    "${parameterClasses.size}, ${parameterValues.size}"
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as InvokeMethod

            if (className != other.className) return false
            if (methodName != other.methodName) return false
            if (parameterClasses != other.parameterClasses) return false
            if (parameterValues != other.parameterValues) return false
            if (returnClass != other.returnClass) return false

            return true
        }

        override fun hashCode(): Int {
            var result = className.hashCode()
            result = 31 * result + methodName.hashCode()
            result = 31 * result + parameterClasses.hashCode()
            result = 31 * result + parameterValues.hashCode()
            result = 31 * result + returnClass.hashCode()
            return result
        }

        override fun toString(): String =
            "InvokeMethod(" +
                "className='$className', " +
                "methodName='$methodName', " +
                "parameterClasses=$parameterClasses, " +
                "parameterValues=$parameterValues, " +
                "returnClass=$returnClass" +
                ")"
    }

    /**
     * Get method modifiers will aim to try and look up a method's modifiers.
     * @property className the full name of the class in which the function lies.
     * @property methodName the name of the function to invoke.
     * @property parameterClasses the types of the parameters that the function takes.
     * @property returnClass the full name of the return type class
     */
    public class GetMethodModifiers(
        public val className: String,
        public val methodName: String,
        public val parameterClasses: List<String>,
        public val returnClass: String,
    ) : ReflectionCheck {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GetMethodModifiers

            if (className != other.className) return false
            if (methodName != other.methodName) return false
            if (parameterClasses != other.parameterClasses) return false
            if (returnClass != other.returnClass) return false

            return true
        }

        override fun hashCode(): Int {
            var result = className.hashCode()
            result = 31 * result + methodName.hashCode()
            result = 31 * result + parameterClasses.hashCode()
            result = 31 * result + returnClass.hashCode()
            return result
        }

        override fun toString(): String =
            "GetMethodModifiers(" +
                "className='$className', " +
                "methodName='$methodName', " +
                "parameterClasses=$parameterClasses, " +
                "returnClass=$returnClass" +
                ")"
    }
}
