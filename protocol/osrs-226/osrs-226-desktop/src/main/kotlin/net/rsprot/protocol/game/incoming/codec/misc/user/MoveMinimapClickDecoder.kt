package net.rsprot.protocol.game.incoming.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.misc.user.MoveMinimapClick
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class MoveMinimapClickDecoder : MessageDecoder<MoveMinimapClick> {
    override val prot: ClientProt = GameClientProt.MOVE_MINIMAPCLICK

    override fun decode(buffer: JagByteBuf): MoveMinimapClick {
        // The x, z and keyCombination get scrambled between revisions
        val keyCombination = buffer.g1Alt1()
        val x = buffer.g2Alt3()
        val z = buffer.g2Alt2()

        // The arguments below are consistent across revisions
        val minimapWidth = buffer.g1()
        val minimapHeight = buffer.g1()
        val cameraAngleY = buffer.g2()
        val checkpoint1 = buffer.g1()
        check(checkpoint1 == 57) {
            "Invalid checkpoint 1: $checkpoint1"
        }
        val checkpoint2 = buffer.g1()
        check(checkpoint2 == 0) {
            "Invalid checkpoint 2: $checkpoint2"
        }
        val checkpoint3 = buffer.g1()
        check(checkpoint3 == 0) {
            "Invalid checkpoint 3: $checkpoint3"
        }
        val checkpoint4 = buffer.g1()
        check(checkpoint4 == 89) {
            "Invalid checkpoint 4: $checkpoint4"
        }
        val fineX = buffer.g2()
        val fineZ = buffer.g2()
        val checkpoint5 = buffer.g1()
        check(checkpoint5 == 63) {
            "Invalid checkpoint 5: $checkpoint5"
        }
        return MoveMinimapClick(
            x,
            z,
            keyCombination,
            minimapWidth,
            minimapHeight,
            cameraAngleY,
            fineX,
            fineZ,
        )
    }
}
