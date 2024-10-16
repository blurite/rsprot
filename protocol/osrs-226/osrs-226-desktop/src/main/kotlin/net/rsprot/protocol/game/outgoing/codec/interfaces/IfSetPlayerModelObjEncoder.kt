package net.rsprot.protocol.game.outgoing.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetPlayerModelObj
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.util.pCombinedIdAlt2

public class IfSetPlayerModelObjEncoder : MessageEncoder<IfSetPlayerModelObj> {
    override val prot: ServerProt = GameServerProt.IF_SETPLAYERMODEL_OBJ

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: IfSetPlayerModelObj,
    ) {
        buffer.pCombinedIdAlt2(message.combinedId)
        buffer.p4(message.obj)
    }
}
