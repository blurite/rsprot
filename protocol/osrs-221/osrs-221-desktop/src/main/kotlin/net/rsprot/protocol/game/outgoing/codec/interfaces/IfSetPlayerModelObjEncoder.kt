package net.rsprot.protocol.game.outgoing.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.interfaces.IfSetPlayerModelObj
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder

public class IfSetPlayerModelObjEncoder : MessageEncoder<IfSetPlayerModelObj> {
    override val prot: ServerProt = GameServerProt.IF_SETPLAYERMODEL_OBJ

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: IfSetPlayerModelObj,
    ) {
        buffer.p4Alt2(message.combinedId.combinedId)
        buffer.p4Alt3(message.obj)
    }
}
