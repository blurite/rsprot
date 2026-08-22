package net.rsprot.protocol.loginprot.incoming.codec

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.internal.login.LoginCrcDecoder

public object DesktopLoginCrcDecoder : LoginCrcDecoder {
    override val clientType: OldSchoolClientType = OldSchoolClientType.DESKTOP

    override fun decodeLive(buffer: JagByteBuf): IntArray {
        val crc = IntArray(LoginCrcDecoder.CRC_COUNT)
        crc[16] = buffer.g4()
        crc[20] = buffer.g4()
        crc[11] = buffer.g4Alt2()
        crc[14] = buffer.g4Alt1()
        crc[2] = buffer.g4Alt1()
        crc[19] = buffer.g4Alt3()
        crc[9] = buffer.g4Alt1()
        crc[8] = buffer.g4()
        crc[17] = buffer.g4()
        crc[4] = buffer.g4()
        crc[10] = buffer.g4Alt1()
        crc[15] = buffer.g4()
        crc[1] = buffer.g4Alt1()
        crc[3] = buffer.g4Alt3()
        crc[7] = buffer.g4()
        crc[12] = buffer.g4Alt1()
        crc[0] = buffer.g4Alt2()
        crc[5] = buffer.g4Alt1()
        crc[6] = buffer.g4Alt2()
        crc[18] = buffer.g4Alt3()
        crc[13] = buffer.g4Alt1()
        return crc
    }

    override fun decodeInitialBeta(buffer: JagByteBuf): IntArray {
        val crc = IntArray(LoginCrcDecoder.CRC_COUNT)
        crc[8] = buffer.g4Alt3()
        crc[15] = buffer.g4Alt2()
        crc[14] = buffer.g4()
        crc[6] = buffer.g4()
        crc[13] = buffer.g4Alt2()
        crc[4] = buffer.g4Alt1()
        crc[10] = buffer.g4Alt3()
        return crc
    }

    override fun decodeRemainingBeta(buffer: JagByteBuf): IntArray {
        val crc = IntArray(LoginCrcDecoder.CRC_COUNT)
        crc[3] = buffer.g4Alt2()
        crc[16] = buffer.g4Alt1()
        crc[12] = buffer.g4Alt3()
        crc[0] = buffer.g4Alt3()
        crc[11] = buffer.g4Alt3()
        crc[1] = buffer.g4Alt3()
        crc[19] = buffer.g4Alt2()
        crc[9] = buffer.g4Alt2()
        crc[2] = buffer.g4()
        crc[20] = buffer.g4Alt1()
        crc[5] = buffer.g4Alt2()
        crc[18] = buffer.g4Alt1()
        crc[7] = buffer.g4Alt2()
        crc[17] = buffer.g4Alt2()
        return crc
    }
}
