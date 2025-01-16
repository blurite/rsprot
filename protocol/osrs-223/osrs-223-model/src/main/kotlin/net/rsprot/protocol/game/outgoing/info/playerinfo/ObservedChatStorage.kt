package net.rsprot.protocol.game.outgoing.info.playerinfo

import net.rsprot.protocol.common.game.outgoing.info.playerinfo.extendedinfo.Chat
import net.rsprot.protocol.common.game.outgoing.info.shared.extendedinfo.Say

/**
 * A storage for any chat messages a player observed, in the exact order they appeared
 * in the player's client. This allows for a precise reconstruction of the chat logs
 * as one would have seen things.
 * @property captureChat whether to capture 'Chat' messages
 * @property captureSay whether to capture 'Say' messages
 * @property count the number of messages tracked so far in the current tick.
 * @property limit the maximum theoretical limit of messages in one tick.
 * @property messages an array of messages in this current tick.
 */
public class ObservedChatStorage(
    private val captureChat: Boolean,
    private val captureSay: Boolean,
) {
    private var count: Int = 0
    private val limit: Int
        get() = (if (captureChat) 2048 else 0) + (if (captureSay) 2048 else 0)
    private val messages: Array<ChatMessage?> = arrayOfNulls(limit)

    /**
     * Captures a snapshot of the messages in the current tick, returning the list of them
     * in the exact order they were received.
     * @return a list of chat messages the player observed.
     */
    public fun snapshot(): List<ChatMessage> {
        if (count == 0) return emptyList()
        val list = ArrayList<ChatMessage>(count)
        for (i in 0..<count) {
            list.add(checkNotNull(messages[i]))
        }
        return list
    }

    /**
     * Resets the tracking for the current tick.
     */
    internal fun reset() {
        count = 0
        messages.fill(null)
    }

    /**
     * Tracks a chat message, logging it for future use.
     * @param index the index of the player that sent the chat message.
     * @param chat the chat message being logged.
     */
    internal fun trackChat(
        index: Int,
        chat: Chat,
    ) {
        if (!captureChat) return
        messages[count++] =
            ChatMessage(
                index,
                chat.colour,
                chat.effects,
                chat.modicon,
                if (chat.autotyper) ChatMessageType.Autotyper else ChatMessageType.Regular,
                chat.text ?: "",
                chat.pattern,
            )
    }

    /**
     * Tracks a say message, logging it for future use.
     * @param index the index of the player that sent the say message.
     * @param say the say message being logged.
     */
    internal fun trackSay(
        index: Int,
        say: Say,
    ) {
        if (!captureSay) return
        messages[count++] =
            ChatMessage(
                index,
                0u,
                0u,
                0u,
                ChatMessageType.Say,
                say.text ?: "",
                null,
            )
    }

    /**
     * Chat message tracks all the properties of a specific chat message sent by a player.
     * @property index the index of the player that said the given message.
     * @property colour the colour used for that chat message
     * @property effects the chat effects used for that chat message
     * @property modicon the mod icon that player had at the time
     * @property type the type of the message that was sent
     * @property text the string that was sent
     * @property pattern the pattern that was used, if any.
     */
    public class ChatMessage internal constructor(
        public val index: Int,
        private val _colour: UByte,
        private val _effects: UByte,
        private val _modicon: UByte,
        public val type: ChatMessageType,
        public val text: String,
        public val pattern: ByteArray?,
    ) {
        public val colour: Int
            get() = _colour.toInt()
        public val effects: Int
            get() = _effects.toInt()
        public val modicon: Int
            get() = _modicon.toInt()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ChatMessage

            if (index != other.index) return false
            if (_colour != other._colour) return false
            if (_effects != other._effects) return false
            if (_modicon != other._modicon) return false
            if (type != other.type) return false
            if (text != other.text) return false
            if (pattern != null) {
                if (other.pattern == null) return false
                if (!pattern.contentEquals(other.pattern)) return false
            } else if (other.pattern != null) {
                return false
            }

            return true
        }

        override fun hashCode(): Int {
            var result = index
            result = 31 * result + _colour.hashCode()
            result = 31 * result + _effects.hashCode()
            result = 31 * result + _modicon.hashCode()
            result = 31 * result + type.hashCode()
            result = 31 * result + text.hashCode()
            result = 31 * result + (pattern?.contentHashCode() ?: 0)
            return result
        }

        override fun toString(): String {
            return "ChatMessage(" +
                "index=$index, " +
                "type=$type, " +
                "text='$text', " +
                "pattern=${pattern?.contentToString()}, " +
                "colour=$colour, " +
                "effects=$effects, " +
                "modicon=$modicon" +
                ")"
        }
    }

    /**
     * Chat message types categorize chat, regular messages and autotyper messages into their
     * own three categories.
     */
    public enum class ChatMessageType {
        Autotyper,
        Regular,
        Say,
    }
}
