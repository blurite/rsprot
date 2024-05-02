package net.rsprot.protocol.api.implementation

import net.rsprot.protocol.api.GameMessageCounter
import net.rsprot.protocol.api.GameMessageCounterProvider

public class DefaultGameMessageCounterProvider : GameMessageCounterProvider {
    override fun provide(): GameMessageCounter {
        return DefaultGameMessageCounter()
    }
}
