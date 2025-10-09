package net.rsprot.protocol.js5.incoming

public sealed interface Js5GroupRequest {
    public val archiveId: Int
    public val groupId: Int

    public val bitpacked: Int
        get() = groupId or (archiveId shl 16)
}
