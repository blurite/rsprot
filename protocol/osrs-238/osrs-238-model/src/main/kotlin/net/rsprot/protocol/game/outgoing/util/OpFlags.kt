package net.rsprot.protocol.game.outgoing.util

/**
 * Op flags are used to hide or show certain right-click options on various
 * interactable entities.
 * This is a helper class to create various combinations of this flag.
 */
@Suppress("MemberVisibilityCanBePrivate")
public object OpFlags {
    /**
     * A constant flag for 'show all options' on an entity.
     */
    public const val ALL_SHOWN: Byte = -1

    /**
     * A constant flag for 'show no options' on an entity.
     */
    public const val NONE_SHOWN: Byte = 0

    @JvmSynthetic
    public operator fun invoke(
        op1: Boolean,
        op2: Boolean,
        op3: Boolean,
        op4: Boolean,
        op5: Boolean,
    ): Byte = ofOps(op1, op2, op3, op4, op5)

    /**
     * Returns the bitpacked op flag out of the provided booleans.
     */
    @JvmStatic
    public fun ofOps(
        op1: Boolean,
        op2: Boolean,
        op3: Boolean,
        op4: Boolean,
        op5: Boolean,
    ): Byte =
        toInt(op1)
            .or(toInt(op2) shl 1)
            .or(toInt(op3) shl 2)
            .or(toInt(op4) shl 3)
            .or(toInt(op5) shl 4)
            .toByte()

    /**
     * Turns the boolean to an integer.
     * @return 1 if the boolean is enabled, 0 otherwise.
     */
    private fun toInt(value: Boolean): Int = if (value) 1 else 0
}
