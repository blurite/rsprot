package net.rsprot.protocol.common.loginprot.incoming.codec

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.common.loginprot.incoming.prot.LoginClientProt
import net.rsprot.protocol.loginprot.incoming.RemainingBetaArchives
import net.rsprot.protocol.message.codec.MessageDecoder

public class RemainingBetaArchivesDecoder : MessageDecoder<RemainingBetaArchives> {
    override val prot: ClientProt = LoginClientProt.REMAINING_BETA_ARCHIVE_CRCS

    override fun decode(buffer: JagByteBuf): RemainingBetaArchives {
        check(buffer.g2() == 58) {
            "Expected remaining beta archives size of 58"
        }
        val crc = IntArray(21)
        crc[19] = buffer.g4()
        crc[2] = buffer.g4()
        crc[0] = buffer.g4Alt3()
        crc[7] = buffer.g4Alt3()
        crc[5] = buffer.g4Alt3()
        crc[12] = buffer.g4()
        crc[17] = buffer.g4Alt2()
        crc[11] = buffer.g4()
        crc[3] = buffer.g4Alt2()
        crc[9] = buffer.g4Alt2()
        crc[16] = buffer.g4Alt2()
        crc[1] = buffer.g4Alt3()
        crc[20] = buffer.g4Alt2()
        crc[18] = buffer.g4Alt3()
        return RemainingBetaArchives(crc)
    }
}
