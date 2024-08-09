package net.rsprot.protocol.game.outgoing.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetPlayerModelBodyType
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class IfSetPlayerModelBodyTypeEncoder : MessageEncoder<IfSetPlayerModelBodyType> {
    override val prot: ServerProt = GameServerProt.IF_SETPLAYERMODEL_BODYTYPE

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: IfSetPlayerModelBodyType,
    ) {
        buffer.p1Alt3(message.bodyType)
        buffer.p4Alt2(message.combinedId.combinedId)
    }
}
