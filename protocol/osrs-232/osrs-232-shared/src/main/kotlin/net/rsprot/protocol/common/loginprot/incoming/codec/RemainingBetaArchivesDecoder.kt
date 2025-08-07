package net.rsprot.protocol.common.loginprot.incoming.codec

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.common.loginprot.incoming.prot.LoginClientProt
import net.rsprot.protocol.loginprot.incoming.RemainingBetaArchives
import net.rsprot.protocol.message.codec.MessageDecoder

public class RemainingBetaArchivesDecoder : MessageDecoder<RemainingBetaArchives> {
    override val prot: ClientProt = LoginClientProt.REMAINING_BETA_ARCHIVE_CRCS

    override fun decode(buffer: JagByteBuf): RemainingBetaArchives {
        check(buffer.g2() == 66) {
            "Expected remaining beta archives size of 66"
        }
        val crc = IntArray(23)
        crc[12] = buffer.g4()
        crc[0] = buffer.g4()
        crc[11] = buffer.g4()
        crc[5] = buffer.g4()
        crc[7] = buffer.g4Alt1()
        crc[18] = buffer.g4Alt2()
        crc[19] = buffer.g4Alt3()
        crc[20] = buffer.g4Alt2()
        crc[21] = buffer.g4Alt2()
        crc[3] = buffer.g4()
        crc[9] = buffer.g4Alt2()
        crc[17] = buffer.g4()
        crc[1] = buffer.g4Alt3()
        crc[2] = buffer.g4Alt3()
        crc[22] = buffer.g4Alt2()
        crc[16] = buffer.g4Alt1()
        return RemainingBetaArchives(crc)
    }
}
