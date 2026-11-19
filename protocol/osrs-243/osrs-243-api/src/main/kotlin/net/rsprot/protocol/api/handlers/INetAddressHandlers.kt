package net.rsprot.protocol.api.handlers

import net.rsprot.protocol.api.InetAddressTracker
import net.rsprot.protocol.api.InetAddressValidator
import net.rsprot.protocol.api.implementation.DefaultInetAddressTracker
import net.rsprot.protocol.api.implementation.DefaultInetAddressValidator

/**
 * The handlers for anything to do with INet addresses.
 * @property hostAddressValidator the validator for new connections, responsible for rejecting
 * any connections after a limit has been reached.
 * @property js5InetAddressTracker the tracker for active JS5 connections
 * @property gameInetAddressTracker the tracker for active game connections
 */
public class INetAddressHandlers
    @JvmOverloads
    public constructor(
        public val inetAddressValidator: InetAddressValidator = DefaultInetAddressValidator(),
        public val js5InetAddressTracker: InetAddressTracker = DefaultInetAddressTracker(),
        public val gameInetAddressTracker: InetAddressTracker = DefaultInetAddressTracker(),
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as INetAddressHandlers

            if (inetAddressValidator != other.inetAddressValidator) return false
            if (js5InetAddressTracker != other.js5InetAddressTracker) return false
            if (gameInetAddressTracker != other.gameInetAddressTracker) return false

            return true
        }

        override fun hashCode(): Int {
            var result = inetAddressValidator.hashCode()
            result = 31 * result + js5InetAddressTracker.hashCode()
            result = 31 * result + gameInetAddressTracker.hashCode()
            return result
        }

        override fun toString(): String =
            "INetAddressHandlers(" +
                "inetAddressValidator=$inetAddressValidator, " +
                "js5InetAddressTracker=$js5InetAddressTracker, " +
                "gameInetAddressTracker=$gameInetAddressTracker" +
                ")"
    }
