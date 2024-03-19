package net.rsprot.protocol.loginprot.incoming.codec.shared

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.cryptography.decipherRsa
import net.rsprot.protocol.cryptography.xteaDecrypt
import net.rsprot.protocol.loginprot.incoming.util.HostPlatformStats
import net.rsprot.protocol.loginprot.incoming.util.LoginBlock
import java.math.BigInteger

public abstract class LoginBlockDecoder<T>(
    private val exp: BigInteger,
    private val mod: BigInteger,
) {
    protected abstract fun decodeAuthentication(buffer: JagByteBuf): T

    protected fun decodeLoginBlock(buffer: JagByteBuf): LoginBlock<T> {
        val version = buffer.g4()
        val subVersion = buffer.g4()
        val firstClientType = buffer.g1()
        val platformType = buffer.g1()
        val constZero1 = buffer.g1()
        val rsaBuffer =
            buffer.decipherRsa(
                exp,
                mod,
                buffer.g2(),
            )
        val encryptionCheck = rsaBuffer.g1()
        check(encryptionCheck == 1) {
            "Invalid RSA check: $encryptionCheck"
        }
        val seed =
            IntArray(4) {
                rsaBuffer.g4()
            }
        val sessionId = rsaBuffer.g8()
        val authentication = decodeAuthentication(rsaBuffer)
        val xteaBuffer = buffer.xteaDecrypt(seed)
        val username = xteaBuffer.gjstr()
        val packedClientSettings = xteaBuffer.g1()
        val lowDetail = packedClientSettings and 0x1 != 0
        val resizable = packedClientSettings and 0x2 != 0
        val width = xteaBuffer.g2()
        val height = xteaBuffer.g2()
        val uuid =
            ByteArray(24) {
                xteaBuffer.g1().toByte()
            }
        val siteSettings = xteaBuffer.gjstr()
        val affiliate = xteaBuffer.g4()
        val constZero2 = xteaBuffer.g1()
        val hostPlatformStats = decodeHostPlatformStats(xteaBuffer)
        val secondClientType = xteaBuffer.g1()
        val crcBlockHeader = xteaBuffer.g4()
        // As revision 221 isn't out yet, we will just naively read the values
        val crc =
            IntArray(21) {
                xteaBuffer.g4()
            }
        return LoginBlock(
            version,
            subVersion,
            firstClientType.toUByte(),
            platformType.toUByte(),
            constZero1.toUByte(),
            seed,
            sessionId,
            "",
            username,
            lowDetail,
            resizable,
            width.toUShort(),
            height.toUShort(),
            uuid,
            siteSettings,
            affiliate,
            constZero2.toUByte(),
            hostPlatformStats,
            secondClientType.toUByte(),
            crcBlockHeader.toUByte(),
            crc,
            authentication,
        )
    }

    private fun decodeHostPlatformStats(buffer: JagByteBuf): HostPlatformStats {
        val version = buffer.g1()
        val osType = buffer.g1()
        val os64Bit = buffer.g1() == 1
        val osVersion = buffer.g2()
        val javaVendor = buffer.g1()
        val javaVersionMajor = buffer.g1()
        val javaVersionMinor = buffer.g1()
        val javaVersionPatch = buffer.g1()
        val unknownConstZero1 = buffer.g1()
        val javaMaxMemoryMb = buffer.g2()
        val javaAvailableProcessors = buffer.g1()
        val systemMemory = buffer.g3()
        val systemSpeed = buffer.g2()
        val gpuDxName = buffer.gjstr2()
        val gpuGlName = buffer.gjstr2()
        val gpuDxVersion = buffer.gjstr2()
        val gpuGlVersion = buffer.gjstr2()
        val gpuDriverMonth = buffer.g1()
        val gpuDriverYear = buffer.g2()
        val cpuManufacturer = buffer.gjstr2()
        val cpuBrand = buffer.gjstr2()
        val cpuCount1 = buffer.g1()
        val cpuCount2 = buffer.g2()
        val cpuFeatures =
            IntArray(3) {
                buffer.g4()
            }
        val cpuSignature = buffer.g4()
        val clientName = buffer.gjstr2()
        val deviceName = buffer.gjstr2()
        return HostPlatformStats(
            version.toUByte(),
            osType.toUByte(),
            os64Bit,
            osVersion.toUShort(),
            javaVendor.toUByte(),
            javaVersionMajor.toUByte(),
            javaVersionMinor.toUByte(),
            javaVersionPatch.toUByte(),
            unknownConstZero1.toUByte(),
            javaMaxMemoryMb.toUShort(),
            javaAvailableProcessors.toUByte(),
            systemMemory,
            systemSpeed.toUShort(),
            gpuDxName,
            gpuGlName,
            gpuDxVersion,
            gpuGlVersion,
            gpuDriverMonth.toUByte(),
            gpuDriverYear.toUShort(),
            cpuManufacturer,
            cpuBrand,
            cpuCount1.toUByte(),
            cpuCount2.toUByte(),
            cpuFeatures,
            cpuSignature,
            clientName,
            deviceName,
        )
    }
}
