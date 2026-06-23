package net.rsprot.protocol.game.outgoing.group.util

public sealed interface GroupVariable {
    /**
     * Sets the value of the group variable to the provided integer [value].
     */
    public class IntGroupVariable(
        public val value: Int,
    ) : GroupVariable {
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
        public val value: Long,
    ) : GroupVariable {
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
        public val value: String,
    ) : GroupVariable {
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
