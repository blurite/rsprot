package net.rsprot.protocol.api.handlers

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufHolder
import io.netty.channel.FileRegion
import io.netty.channel.MessageSizeEstimator
import net.rsprot.protocol.Prot
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.api.NetworkService
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprot.protocol.message.OutgoingJs5Message
import net.rsprot.protocol.message.OutgoingLoginMessage
import net.rsprot.protocol.message.OutgoingMessage

public class OutgoingMessageSizeEstimator(
    networkService: NetworkService<*>,
) : MessageSizeEstimator {
    private val supportsMultiplePlatforms =
        networkService
            .encoderRepositories
            .gameMessageDecoderRepositories
            .notNullSize > 1
    private val gameEncoder =
        networkService
            .encoderRepositories
            .gameMessageDecoderRepositories[ESTIMATOR_CLIENT_TYPE]
    private val loginEncoder =
        networkService
            .encoderRepositories
            .loginMessageDecoderRepository

    private val singleton = OutgoingMessageSizeEstimatorHandle()

    override fun newHandle(): MessageSizeEstimator.Handle = singleton

    private inner class OutgoingMessageSizeEstimatorHandle : MessageSizeEstimator.Handle {
        override fun size(msg: Any): Int {
            try {
                when (msg) {
                    is OutgoingGameMessage -> {
                        return estimateGameMessageSize(msg)
                    }
                    is OutgoingLoginMessage -> {
                        return estimateLoginMessageSize(msg)
                    }
                    is OutgoingJs5Message -> {
                        return estimateJs5MessageSize(msg)
                    }
                }
                if (msg is ByteBuf) {
                    return msg.readableBytes()
                }
                if (msg is ByteBufHolder) {
                    return msg.content().readableBytes()
                }
                if (msg is FileRegion) {
                    return FILE_REGION_SIZE
                }
                return UNKNOWN_MESSAGE_SIZE
            } catch (t: Throwable) {
                logger.error(t) {
                    "Unable to estimate the size of message $msg"
                }
                return UNKNOWN_MESSAGE_SIZE
            }
        }

        private fun estimateGameMessageSize(msg: OutgoingGameMessage): Int {
            val prot = gameEncoder.getEncoder(msg.javaClass).prot
            return estimateRegularProtocolMessage(msg, prot)
        }

        private fun estimateLoginMessageSize(msg: OutgoingLoginMessage): Int {
            val prot = loginEncoder.getEncoder(msg.javaClass).prot
            return estimateRegularProtocolMessage(msg, prot)
        }

        private fun estimateJs5MessageSize(msg: OutgoingJs5Message): Int {
            val estimate = msg.estimateSize()
            if (estimate != -1) {
                return estimate
            }
            if (msg is ByteBufHolder) {
                return msg.content().readableBytes()
            }
            return UNKNOWN_JS5_MESSAGE_PAYLOAD_SIZE
        }

        private fun estimateRegularProtocolMessage(
            msg: OutgoingMessage,
            prot: ServerProt,
        ): Int {
            // First reserve one or two bytes for the opcode, depending on circumstances
            // If we know there's only the desktop platform registered, we can rely on that
            // to know if the opcode requires two bytes or one
            // If multiple platforms are used however, we always just assume two bytes
            // to avoid the expensive resizing operation
            var headerSize =
                if (supportsMultiplePlatforms || prot.opcode >= TWO_BYTE_OPCODE_THRESHOLD) {
                    Short.SIZE_BYTES
                } else {
                    Byte.SIZE_BYTES
                }

            // Next up, add 1 byte for var-byte and 2 bytes for var-short
            // If the message is not a var-* one, add the constant size and return early
            val constantSize = prot.size
            headerSize +=
                if (constantSize == Prot.VAR_BYTE) {
                    Byte.SIZE_BYTES
                } else if (constantSize == Prot.VAR_SHORT) {
                    Short.SIZE_BYTES
                } else {
                    // If no dynamic payload, return here
                    return headerSize + constantSize
                }

            // If we override the estimation, add that to the size and return it
            val estimate = msg.estimateSize()
            if (estimate != -1) {
                return headerSize + estimate
            }
            // If an override was not provided, check if the message is a byte buf holder which
            // will already tell is the exact size of the message
            if (msg is ByteBufHolder) {
                return headerSize + msg.content().readableBytes()
            }

            // If all else fails, just assume the payload size is 8 bytes.
            // We should try to avoid reaching this stage as it could be off quite a lot
            return headerSize + UNKNOWN_MESSAGE_SIZE
        }
    }

    private companion object {
        private val ESTIMATOR_CLIENT_TYPE: OldSchoolClientType = OldSchoolClientType.DESKTOP
        private const val TWO_BYTE_OPCODE_THRESHOLD: Int = 0x80
        private const val FILE_REGION_SIZE: Int = 0
        private const val UNKNOWN_MESSAGE_SIZE: Int = 8
        private const val UNKNOWN_JS5_MESSAGE_PAYLOAD_SIZE: Int = 512
        private val logger = InlineLogger()
    }
}
