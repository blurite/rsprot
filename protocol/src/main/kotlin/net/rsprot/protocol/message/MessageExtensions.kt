package net.rsprot.protocol.message

public fun UShort.toIntOrMinusOne(): Int {
    return if (this == UShort.MAX_VALUE) {
        -1
    } else {
        this.toInt()
    }
}

public fun UByte.toIntOrMinusOne(): Int {
    return if (this == UByte.MAX_VALUE) {
        -1
    } else {
        this.toInt()
    }
}
