package net.rsprot.protocol.game.outgoing.misc.client

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprot.protocol.message.util.estimateTextSize

/**
 * URL open packets are used to open a site on the target's default
 * browser.
 * @property url the url to connect to
 */
public class UrlOpen(
    public val url: String,
) : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun estimateSize(): Int {
        return estimateTextSize(url)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UrlOpen

        return url == other.url
    }

    override fun hashCode(): Int = url.hashCode()

    override fun toString(): String = "UrlOpen(url='$url')"
}
