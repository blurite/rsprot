package net.rsprot.protocol.common.loginprot.incoming.codec.shared

import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.crypto.rsa.decipherRsa
import net.rsprot.crypto.xtea.xteaDecrypt
import net.rsprot.protocol.common.RSProtConstants
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.common.loginprot.incoming.codec.shared.exceptions.InvalidVersionException
import net.rsprot.protocol.common.loginprot.incoming.codec.shared.exceptions.UnsupportedClientException
import net.rsprot.protocol.loginprot.incoming.util.CyclicRedundancyCheckBlock
import net.rsprot.protocol.loginprot.incoming.util.HostPlatformStats
import net.rsprot.protocol.loginprot.incoming.util.LoginBlock
import net.rsprot.protocol.loginprot.incoming.util.LoginClientType
import java.math.BigInteger

@Suppress("DuplicatedCode")
public abstract class LoginBlockDecoder<T>(
    private val exp: BigInteger,
    private val mod: BigInteger,
) {
    protected abstract fun decodeAuthentication(buffer: JagByteBuf): T

    public fun decodeHeader(
        buffer: JagByteBuf,
        supportedClientTypes: List<OldSchoolClientType>,
    ): LoginBlock.Header {
        val version = buffer.g4()
        if (version != RSProtConstants.REVISION) {
            throw InvalidVersionException
        }
        val subVersion = buffer.g4()
        val firstClientType = buffer.g1()
        val loginClientType = LoginClientType[firstClientType]
        val oldSchoolClientType = loginClientType.toOldSchoolClientType()
        if (oldSchoolClientType !in supportedClientTypes) {
            throw UnsupportedClientException
        }
        val platformType = buffer.g1()
        val hasExternalAuthenticator = buffer.g1() == 1
        return LoginBlock.Header(
            version,
            subVersion,
            firstClientType.toUByte(),
            platformType.toUByte(),
            hasExternalAuthenticator,
        )
    }

    protected fun decodeLoginBlock(
        header: LoginBlock.Header,
        buffer: JagByteBuf,
        betaWorld: Boolean,
    ): LoginBlock<T> {
        try {
            val rsaSize = buffer.g2()
            if (!buffer.isReadable(rsaSize)) {
                throw IllegalStateException("RSA buffer not readable: $rsaSize, ${buffer.readableBytes()}")
            }
            val rsaBuffer =
                buffer.buffer
                    .decipherRsa(
                        exp,
                        mod,
                        rsaSize,
                    ).toJagByteBuf()
            try {
                val encryptionCheck = rsaBuffer.g1()
                check(encryptionCheck == 1) {
                    "Invalid RSA check '$encryptionCheck'. " +
                        "This typically means the RSA in the client does not match up with the server."
                }
                val seed =
                    IntArray(4) {
                        rsaBuffer.g4()
                    }
                val sessionId = rsaBuffer.g8()
                val authentication = decodeAuthentication(rsaBuffer)
                val xteaBuffer = buffer.buffer.xteaDecrypt(seed).toJagByteBuf()
                try {
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
                    val deepLinkCount = xteaBuffer.g1()
                    val deepLinks =
                        if (deepLinkCount == 0) {
                            emptyList()
                        } else {
                            List(deepLinkCount) {
                                xteaBuffer.g4()
                            }
                        }
                    val hostPlatformStats = decodeHostPlatformStats(xteaBuffer)
                    val secondClientType = xteaBuffer.g1()
                    if (secondClientType != header.clientType.id) {
                        throw UnsupportedClientException
                    }
                    val reflectionCheckerConst = xteaBuffer.g4()
                    val crc =
                        if (betaWorld) {
                            decodeBetaCrc(xteaBuffer)
                        } else {
                            decodeCrc(xteaBuffer)
                        }
                    return LoginBlock(
                        header,
                        seed,
                        sessionId,
                        username,
                        lowDetail,
                        resizable,
                        width.toUShort(),
                        height.toUShort(),
                        uuid,
                        siteSettings,
                        affiliate,
                        deepLinks,
                        hostPlatformStats,
                        secondClientType.toUByte(),
                        reflectionCheckerConst,
                        crc,
                        authentication,
                    )
                } finally {
                    xteaBuffer.buffer.release()
                }
            } finally {
                rsaBuffer.buffer.release()
            }
        } finally {
            buffer.buffer.release()
        }
    }

    private fun decodeCrc(buffer: JagByteBuf): CyclicRedundancyCheckBlock {
        val crc = IntArray(TRANSMITTED_CRC_COUNT)
        crc[6] = buffer.g4()
        crc[15] = buffer.g4Alt2()
        crc[2] = buffer.g4Alt1()
        crc[22] = buffer.g4Alt1()
        crc[0] = buffer.g4Alt2()
        crc[11] = buffer.g4Alt3()
        crc[4] = buffer.g4Alt2()
        crc[13] = buffer.g4Alt3()
        crc[10] = buffer.g4Alt3()
        crc[14] = buffer.g4()
        crc[21] = buffer.g4()
        crc[7] = buffer.g4Alt2()
        crc[9] = buffer.g4Alt2()
        crc[1] = buffer.g4()
        crc[12] = buffer.g4()
        crc[17] = buffer.g4Alt1()
        crc[3] = buffer.g4Alt3()
        crc[5] = buffer.g4Alt2()
        crc[16] = buffer.g4Alt2()
        crc[18] = buffer.g4Alt3()
        crc[8] = buffer.g4()
        crc[20] = buffer.g4()
        crc[19] = buffer.g4Alt2()

        return object : CyclicRedundancyCheckBlock(crc) {
            override fun validate(serverCrc: IntArray): Boolean {
                require(serverCrc.size >= TRANSMITTED_CRC_COUNT) {
                    "Server CRC length less than expected: ${serverCrc.size}, expected >= $TRANSMITTED_CRC_COUNT"
                }
                for (i in 0..<TRANSMITTED_CRC_COUNT) {
                    if (serverCrc[i] != this.clientCrc[i]) {
                        return false
                    }
                }
                return true
            }
        }
    }

    private fun decodeBetaCrc(buffer: JagByteBuf): CyclicRedundancyCheckBlock {
        val crc = IntArray(TRANSMITTED_CRC_COUNT)
        crc[6] = buffer.g4Alt2()
        crc[14] = buffer.g4()
        crc[4] = buffer.g4Alt2()
        crc[15] = buffer.g4()
        crc[8] = buffer.g4Alt2()
        crc[13] = buffer.g4Alt2()
        crc[10] = buffer.g4Alt2()
        return object : CyclicRedundancyCheckBlock(crc) {
            override fun validate(serverCrc: IntArray): Boolean {
                require(serverCrc.size >= TRANSMITTED_CRC_COUNT) {
                    "Server CRC length less than expected: ${serverCrc.size}, expected >= $TRANSMITTED_CRC_COUNT"
                }
                for (i in 0..<TRANSMITTED_CRC_COUNT) {
                    if (serverCrc[i] != this.clientCrc[i]) {
                        return false
                    }
                }
                return true
            }
        }
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
        val applet = buffer.g1() == 0
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
        val cpuCount2 = buffer.g1()
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
            applet,
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

    private companion object {
        private const val TRANSMITTED_CRC_COUNT: Int = 23
    }
}
