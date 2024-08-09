package net.rsprot.protocol.message

public fun UShort.toIntOrMinusOne(): Int =
    if (this == UShort.MAX_VALUE) {
        -1
    } else {
        this.toInt()
    }

public fun UByte.toIntOrMinusOne(): Int =
    if (this == UByte.MAX_VALUE) {
        -1
    } else {
        this.toInt()
    }
