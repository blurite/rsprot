package net.rsprot.protocol.message

public interface OutgoingMessage : Message {
    /**
     * A function to estimate the size of a message.
     * The estimate should only consist of the payload, not the header.
     */
    public fun estimateSize(): Int = -1
}
