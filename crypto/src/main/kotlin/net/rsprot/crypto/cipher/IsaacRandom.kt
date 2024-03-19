package net.rsprot.crypto.cipher

/**
 * [ISAAC Random cipher](https://en.wikipedia.org/wiki/ISAAC_(cipher))
 *
 * This implementation was taken from [OpenRS2](https://github.com/openrs2/openrs2/blob/master/crypto/src/main/kotlin/org/openrs2/crypto/IsaacRandom.kt)
 */
@Suppress("DuplicatedCode")
public class IsaacRandom : StreamCipher {
    private var count = 0
    private val rsl: IntArray
    private val mem = IntArray(SIZE)
    private var a = 0
    private var b = 0
    private var c = 0

    public constructor() {
        rsl = IntArray(SIZE)
        init(false)
    }

    public constructor(seed: IntArray) {
        require(seed.size <= SIZE)

        rsl = seed.copyOf(SIZE)
        init(true)
    }

    private fun init(flag: Boolean) {
        var a = GOLDEN_RATIO
        var b = GOLDEN_RATIO
        var c = GOLDEN_RATIO
        var d = GOLDEN_RATIO
        var e = GOLDEN_RATIO
        var f = GOLDEN_RATIO
        var g = GOLDEN_RATIO
        var h = GOLDEN_RATIO

        for (i in 0..<4) {
            a = a xor (b shl 11)
            d += a
            b += c

            b = b xor (c ushr 2)
            e += b
            c += d

            c = c xor (d shl 8)
            f += c
            d += e

            d = d xor (e ushr 16)
            g += d
            e += f

            e = e xor (f shl 10)
            h += e
            f += g

            f = f xor (g ushr 4)
            a += f
            g += h

            g = g xor (h shl 8)
            b += g
            h += a

            h = h xor (a ushr 9)
            c += h
            a += b
        }

        for (i in 0..<SIZE step 8) {
            if (flag) {
                a += rsl[i]
                b += rsl[i + 1]
                c += rsl[i + 2]
                d += rsl[i + 3]
                e += rsl[i + 4]
                f += rsl[i + 5]
                g += rsl[i + 6]
                h += rsl[i + 7]
            }

            a = a xor (b shl 11)
            d += a
            b += c

            b = b xor (c ushr 2)
            e += b
            c += d

            c = c xor (d shl 8)
            f += c
            d += e

            d = d xor (e ushr 16)
            g += d
            e += f

            e = e xor (f shl 10)
            h += e
            f += g

            f = f xor (g ushr 4)
            a += f
            g += h

            g = g xor (h shl 8)
            b += g
            h += a

            h = h xor (a ushr 9)
            c += h
            a += b

            mem[i] = a
            mem[i + 1] = b
            mem[i + 2] = c
            mem[i + 3] = d
            mem[i + 4] = e
            mem[i + 5] = f
            mem[i + 6] = g
            mem[i + 7] = h
        }

        if (flag) {
            for (i in 0..<SIZE step 8) {
                a += mem[i]
                b += mem[i + 1]
                c += mem[i + 2]
                d += mem[i + 3]
                e += mem[i + 4]
                f += mem[i + 5]
                g += mem[i + 6]
                h += mem[i + 7]

                a = a xor (b shl 11)
                d += a
                b += c

                b = b xor (c ushr 2)
                e += b
                c += d

                c = c xor (d shl 8)
                f += c
                d += e

                d = d xor (e ushr 16)
                g += d
                e += f

                e = e xor (f shl 10)
                h += e
                f += g

                f = f xor (g ushr 4)
                a += f
                g += h

                g = g xor (h shl 8)
                b += g
                h += a

                h = h xor (a ushr 9)
                c += h
                a += b

                mem[i] = a
                mem[i + 1] = b
                mem[i + 2] = c
                mem[i + 3] = d
                mem[i + 4] = e
                mem[i + 5] = f
                mem[i + 6] = g
                mem[i + 7] = h
            }
        }

        isaac()
        count = SIZE
    }

    private fun isaac() {
        var a = this.a
        var b = this.b + ++this.c

        var i = 0
        var j = SIZE / 2
        var x: Int
        var y: Int
        while (i < SIZE / 2) {
            x = mem[i]
            a = a xor (a shl 13)
            a += mem[j++]
            y = mem[(x and MASK) shr 2] + a + b
            mem[i] = y
            b = mem[((y shr SIZEL) and MASK) shr 2] + x
            rsl[i++] = b

            x = mem[i]
            a = a xor (a ushr 6)
            a += mem[j++]
            y = mem[(x and MASK) shr 2] + a + b
            mem[i] = y
            b = mem[((y shr SIZEL) and MASK) shr 2] + x
            rsl[i++] = b

            x = mem[i]
            a = a xor (a shl 2)
            a += mem[j++]
            y = mem[(x and MASK) shr 2] + a + b
            mem[i] = y
            b = mem[((y shr SIZEL) and MASK) shr 2] + x
            rsl[i++] = b

            x = mem[i]
            a = a xor (a ushr 16)
            a += mem[j++]
            y = mem[(x and MASK) shr 2] + a + b
            mem[i] = y
            b = mem[((y shr SIZEL) and MASK) shr 2] + x
            rsl[i++] = b
        }

        j = 0
        while (j < SIZE / 2) {
            x = mem[i]
            a = a xor (a shl 13)
            a += mem[j++]
            y = mem[(x and MASK) shr 2] + a + b
            mem[i] = y
            b = mem[((y shr SIZEL) and MASK) shr 2] + x
            rsl[i++] = b

            x = mem[i]
            a = a xor (a ushr 6)
            a += mem[j++]
            y = mem[(x and MASK) shr 2] + a + b
            mem[i] = y
            b = mem[((y shr SIZEL) and MASK) shr 2] + x
            rsl[i++] = b

            x = mem[i]
            a = a xor (a shl 2)
            a += mem[j++]
            y = mem[(x and MASK) shr 2] + a + b
            mem[i] = y
            b = mem[((y shr SIZEL) and MASK) shr 2] + x
            rsl[i++] = b

            x = mem[i]
            a = a xor (a ushr 16)
            a += mem[j++]
            y = mem[(x and MASK) shr 2] + a + b
            mem[i] = y
            b = mem[((y shr SIZEL) and MASK) shr 2] + x
            rsl[i++] = b
        }

        this.b = b
        this.a = a
    }

    public override fun nextInt(): Int {
        if (count-- == 0) {
            isaac()
            count = SIZE - 1
        }

        return rsl[count]
    }

    private companion object {
        private const val SIZEL = 8
        private const val SIZE = 1 shl SIZEL
        private const val MASK = (SIZE - 1) shl 2
        private const val GOLDEN_RATIO = 0x9E3779B9.toInt()
    }
}
