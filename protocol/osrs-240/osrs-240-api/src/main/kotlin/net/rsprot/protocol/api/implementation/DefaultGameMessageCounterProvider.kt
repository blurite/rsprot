package net.rsprot.protocol.api.implementation

import net.rsprot.protocol.api.GameMessageCounter
import net.rsprot.protocol.api.GameMessageCounterProvider

/**
 * The provider for the default game messages
 */
public class DefaultGameMessageCounterProvider : GameMessageCounterProvider {
    override fun provide(): GameMessageCounter = DefaultGameMessageCounter()
}
