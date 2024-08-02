@file:Suppress("DuplicatedCode")

package net.rsprot.compression

/**
 * A helper class for base-37 encoding and decoding of strings.
 */
public data object Base37 {
    private const val BASE_37: Long = 37
    private const val MAXIMUM_POSSIBLE_12_CHARACTER_VALUE: Long = 6582952005840035280L
    private const val NBSP: Int = 160
    private val ALPHABET: CharArray =
        charArrayOf(
            '_',
            'a',
            'b',
            'c',
            'd',
            'e',
            'f',
            'g',
            'h',
            'i',
            'j',
            'k',
            'l',
            'm',
            'n',
            'o',
            'p',
            'q',
            'r',
            's',
            't',
            'u',
            'v',
            'w',
            'x',
            'y',
            'z',
            '0',
            '1',
            '2',
            '3',
            '4',
            '5',
            '6',
            '7',
            '8',
            '9',
        )

    /**
     * Encodes the [charSequence] using base-37 encoding.
     * @param charSequence the char sequence to encode.
     * Valid characters include: `a-z`, `A-Z`, `0-9`, `_`
     * @return the base-37 encoded long representation of the string.
     * @throws IllegalArgumentException if the [charSequence] is longer
     * than 12 characters, or uses characters not listed in the valid
     * characters list.
     */
    public fun encode(charSequence: CharSequence): Long {
        require(charSequence.length <= 12) {
            "Char sequence length must be 12 characters or less."
        }
        val indexOfInvalidCharacter =
            charSequence.indexOfFirst {
                it.lowercaseChar() !in ALPHABET
            }
        require(indexOfInvalidCharacter == -1) {
            "Invalid character in charSequence at index $indexOfInvalidCharacter: " +
                "${charSequence[indexOfInvalidCharacter]}"
        }
        var encoded = 0L
        for (element in charSequence) {
            encoded *= BASE_37
            when (element) {
                in 'A'..'Z' -> {
                    encoded += (element.code + 1 - 'A'.code).toLong()
                }

                in 'a'..'z' -> {
                    encoded += (element.code + 1 - 'a'.code).toLong()
                }

                in '0'..'9' -> {
                    encoded += (element.code + 27 - '0'.code).toLong()
                }
            }
        }

        while (encoded % BASE_37 == 0L && encoded != 0L) {
            encoded /= BASE_37
        }

        return encoded
    }

    /**
     * Decodes a base-37 encoded long into the original string.
     * If the input long is within the correct range, but isn't v
     * @param encoded the base-37 encoded long value.
     * @return the string that was encoded in base-37 encoding.
     * @throws IllegalArgumentException if the encoded value exceeds
     * the maximum 12-character long value, or if the value
     * isn't in base-37 representation.
     */
    public fun decode(encoded: Long): String {
        if (encoded == 0L) {
            return ""
        }
        require(encoded in 0..MAXIMUM_POSSIBLE_12_CHARACTER_VALUE) {
            "Invalid encoded value: $encoded"
        }
        require(encoded % BASE_37 != 0L) {
            "Encoded value not in base-37: $encoded"
        }
        var length = 0

        var lengthCounter = encoded
        while (lengthCounter != 0L) {
            ++length
            lengthCounter /= BASE_37
        }

        val builder = StringBuilder(length)

        var rem = encoded
        while (rem != 0L) {
            val cur = rem
            rem /= BASE_37
            builder.append(ALPHABET[(cur - rem * BASE_37).toInt()])
        }

        return builder
            .reverse()
            .toString()
    }

    /**
     * Decodes a base-37 encoded long into the respective string,
     * replacing all underscores with spaces, as well as all first
     * letters of each individual word to begin with an uppercase
     * letter.
     * If the input long is within the correct range, but isn't v
     * @param encoded the base-37 encoded long value.
     * @return the string that was encoded in base-37 encoding.
     * @throws IllegalArgumentException if the encoded value exceeds
     * the maximum 12-character long value, or if the value
     * isn't in base-37 representation.
     */
    public fun decodeWithCase(encoded: Long): String {
        if (encoded == 0L) {
            return ""
        }
        require(encoded in 0..MAXIMUM_POSSIBLE_12_CHARACTER_VALUE) {
            "Invalid encoded value: $encoded"
        }
        require(encoded % BASE_37 != 0L) {
            "Encoded value not in base-37: $encoded"
        }
        var length = 0
        var lengthCounter = encoded
        while (lengthCounter != 0L) {
            ++length
            lengthCounter /= BASE_37
        }
        val builder = StringBuilder(length)
        var rem = encoded
        while (rem != 0L) {
            val var6 = rem
            rem /= BASE_37
            var char = ALPHABET[(var6 - rem * BASE_37).toInt()]
            if (char == '_') {
                val lastIndex = builder.length - 1
                builder.setCharAt(lastIndex, builder[lastIndex].uppercaseChar())
                char = NBSP.toChar()
            }
            builder.append(char)
        }

        builder.reverse()
        builder.setCharAt(0, builder[0].uppercaseChar())
        return builder.toString()
    }
}
