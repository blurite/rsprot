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
        val payload = ByteArray(RemainingBetaArchives.PAYLOAD_SIZE)
        buffer.buffer.readBytes(payload)
        return RemainingBetaArchives(payload)
    }
}
