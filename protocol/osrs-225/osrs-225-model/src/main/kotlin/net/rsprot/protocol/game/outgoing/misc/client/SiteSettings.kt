package net.rsprot.protocol.game.outgoing.misc.client

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprot.protocol.message.util.estimateTextSize

/**
 * Site settings packet is used to identify the given client.
 * The settings are sent as part of the URL when connecting to services
 * or secure RuneScape URLs.
 * @property settings the settings string to assign
 */
public class SiteSettings(
    public val settings: String,
) : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT

    override fun estimateSize(): Int {
        return estimateTextSize(settings)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SiteSettings

        return settings == other.settings
    }

    override fun hashCode(): Int = settings.hashCode()

    override fun toString(): String = "SiteSettings(settings='$settings')"
}
