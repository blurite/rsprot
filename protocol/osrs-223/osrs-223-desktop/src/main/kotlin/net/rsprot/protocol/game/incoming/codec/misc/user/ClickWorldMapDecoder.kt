package net.rsprot.protocol.game.incoming.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.common.game.outgoing.info.CoordGrid
import net.rsprot.protocol.game.incoming.misc.user.ClickWorldMap
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools

public class ClickWorldMapDecoder : MessageDecoder<ClickWorldMap> {
    override val prot: ClientProt = GameClientProt.CLICKWORLDMAP

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): ClickWorldMap {
        val packed = buffer.g4Alt2()
        return ClickWorldMap(CoordGrid(packed))
    }
}
