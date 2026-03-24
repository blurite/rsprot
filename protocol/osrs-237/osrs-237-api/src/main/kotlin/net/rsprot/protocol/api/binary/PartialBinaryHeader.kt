package net.rsprot.protocol.api.binary

import java.nio.file.Path

/**
 * A partial binary header, containing all the data that RSProt cannot gather on its own
 * and needs the server to provide.
 * @property path the path into which the .bin file will be written.
 * @property worldId the id of the world
 * @property worldFlags the world flags/properties
 * @property worldLocation the country of the world
 * @property worldHost the host address of the world
 * @property worldActivity the activity name of the world
 * @property clientName the name of the client to use
 */
public data class PartialBinaryHeader(
    public val path: Path,
    public val worldId: Int,
    public val worldFlags: Int,
    public val worldLocation: Int,
    public val worldHost: String,
    public val worldActivity: String,
    public val clientName: String,
)
