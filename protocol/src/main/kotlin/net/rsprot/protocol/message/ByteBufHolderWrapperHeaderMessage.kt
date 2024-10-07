package net.rsprot.protocol.message

/**
 * A message that notifies our encoder about the byte buf holder packet having
 * a header message that needs to be encoded on-top of the content of this message.
 */
public interface ByteBufHolderWrapperHeaderMessage {
    /**
     * The number of bytes this message consists of, excluding the buffer that the
     * byte buf holder itself is carrying.
     * @return the number of bytes of this message, excluding byte buf holder.
     */
    public fun nonByteBufHolderSize(): Int
}
