package net.rsprot.protocol.game.outgoing.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.client.SetHeatmapEnabled
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class SetHeatmapEnabledEncoder : MessageEncoder<SetHeatmapEnabled> {
    override val prot: ServerProt = GameServerProt.SET_HEATMAP_ENABLED

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: SetHeatmapEnabled,
    ) {
        buffer.pboolean(message.enabled)
    }
}
