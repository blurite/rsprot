package net.rsprot.protocol.game.outgoing.misc.client

import net.rsprot.protocol.message.OutgoingMessage

/**
 * URL open packets are used to open a site on the target's default
 * browser.
 * @property url the url to connect to
 */
public class UrlOpen(
    public val url: String,
) : OutgoingMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UrlOpen

        return url == other.url
    }

    override fun hashCode(): Int {
        return url.hashCode()
    }

    override fun toString(): String {
        return "UrlOpen(url='$url')"
    }
}
