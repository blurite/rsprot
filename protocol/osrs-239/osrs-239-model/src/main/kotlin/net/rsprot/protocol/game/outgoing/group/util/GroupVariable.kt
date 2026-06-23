package net.rsprot.protocol.game.outgoing.group.util

public sealed interface GroupVariable<out T> {
    public val value: T

    /**
     * Sets the value of the group variable to the provided integer [value].
     */
    public class IntGroupVariable(
        override val value: Int,
    ) : GroupVariable<Int> {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as IntGroupVariable

            return value == other.value
        }

        override fun hashCode(): Int {
            return value
        }

        override fun toString(): String {
            return "IntGroupVariable(" +
                "value=$value" +
                ")"
        }
    }

    /**
     * Sets the value of the group variable to the provided long [value].
     */
    public class LongGroupVariable(
        override val value: Long,
    ) : GroupVariable<Long> {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as LongGroupVariable

            return value == other.value
        }

        override fun hashCode(): Int {
            return value.hashCode()
        }

        override fun toString(): String {
            return "LongGroupVariable(" +
                "value=$value" +
                ")"
        }
    }

    /**
     * Sets the value of the group variable to the provided string [value].
     */
    public class StringGroupVariable(
        override val value: String,
    ) : GroupVariable<String> {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as StringGroupVariable

            return value == other.value
        }

        override fun hashCode(): Int {
            return value.hashCode()
        }

        override fun toString(): String {
            return "StringGroupVariable(" +
                "value='$value'" +
                ")"
        }
    }
}
