package net.rsprot.protocol.game.outgoing.info.util

import net.rsprot.protocol.message.OutgoingGameMessage
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.fold
import kotlin.getOrElse
import kotlin.getOrThrow
import kotlin.map
import kotlin.mapCatching
import kotlin.recover
import kotlin.recoverCatching

/**
 * A non-inline variant of Kotlin's [Result] class, to make it consumable from Java.
 * This file is a simple copy of what Kotlin offers, with the wrapper for failure eliminated,
 * as only this project can construct instances of it. An upper bound of [OutgoingGameMessage]
 * was added to ensure no one can map it into an exception and end up with faulty behavior.
 * Any top-level functions are removed, only extensions on [net.rsprot.protocol.game.outgoing.info.util.PacketResult]
 * are retained.
 */
public class PacketResult<out T : OutgoingGameMessage>
    @PublishedApi
    internal constructor(
        @PublishedApi
        internal val value: Any?,
    ) {
        /**
         * Returns `true` if this instance represents a successful outcome.
         * In this case [isFailure] returns `false`.
         */
        public val isSuccess: Boolean get() = value !is Throwable

        /**
         * Returns `true` if this instance represents a failed outcome.
         * In this case [isSuccess] returns `false`.
         */
        public val isFailure: Boolean get() = value is Throwable

        // value & exception retrieval

        /**
         * Returns the encapsulated value if this instance represents [success][PacketResult.isSuccess] or `null`
         * if it is [failure][PacketResult.isFailure].
         *
         * This function is a shorthand for `getOrElse { null }` (see [getOrElse]) or
         * `fold(onSuccess = { it }, onFailure = { null })` (see [fold]).
         */
        @Suppress("UNCHECKED_CAST")
        public fun getOrNull(): T? =
            when {
                isFailure -> null
                else -> value as T
            }

        /**
         * Returns the encapsulated [Throwable] exception if this instance represents [failure][isFailure] or `null`
         * if it is [success][isSuccess].
         *
         * This function is a shorthand for `fold(onSuccess = { null }, onFailure = { it })` (see [fold]).
         */
        public fun exceptionOrNull(): Throwable? =
            when (value) {
                is Throwable -> value
                else -> null
            }

        /**
         * Returns a string `Success(v)` if this instance represents [success][PacketResult.isSuccess]
         * where `v` is a string representation of the value or a string `Failure(x)` if
         * it is [failure][isFailure] where `x` is a string representation of the exception.
         */
        public override fun toString(): String =
            when (value) {
                is Throwable -> value.toString()
                else -> "Success($value)"
            }

        @PublishedApi
        internal companion object {
            /**
             * Returns an instance that encapsulates the given [value] as successful value.
             */
            fun <T : OutgoingGameMessage> success(value: T): PacketResult<T> {
                return PacketResult(value)
            }

            /**
             * Returns an instance that encapsulates the given [Throwable] [exception] as failure.
             */
            fun <T : OutgoingGameMessage> failure(exception: Throwable): PacketResult<T> {
                return PacketResult(exception)
            }
        }
    }

/**
 * Throws exception if the result is failure. This internal function minimizes
 * inlined bytecode for [getOrThrow] and makes sure that in the future we can
 * add some exception-augmenting logic here (if needed).
 */
@PublishedApi
@SinceKotlin("1.3")
internal fun PacketResult<*>.throwOnFailure() {
    if (value is Throwable) throw value
}

// -- extensions ---

/**
 * Returns the encapsulated value if this instance represents [success][PacketResult.isSuccess] or throws the encapsulated [Throwable] exception
 * if it is [failure][PacketResult.isFailure].
 *
 * This function is a shorthand for `getOrElse { throw it }` (see [getOrElse]).
 */
public fun <T : OutgoingGameMessage> PacketResult<T>.getOrThrow(): T {
    throwOnFailure()
    @Suppress("UNCHECKED_CAST")
    return value as T
}

/**
 * Returns the encapsulated value if this instance represents [success][PacketResult.isSuccess] or the
 * result of [onFailure] function for the encapsulated [Throwable] exception if it is [failure][PacketResult.isFailure].
 *
 * Note, that this function rethrows any [Throwable] exception thrown by [onFailure] function.
 *
 * This function is a shorthand for `fold(onSuccess = { it }, onFailure = onFailure)` (see [fold]).
 */
@OptIn(ExperimentalContracts::class)
public inline fun <R : OutgoingGameMessage, T : R> PacketResult<T>.getOrElse(
    onFailure: (exception: Throwable) -> R,
): R {
    contract {
        callsInPlace(onFailure, InvocationKind.AT_MOST_ONCE)
    }
    @Suppress("UNCHECKED_CAST")
    return when (val exception = exceptionOrNull()) {
        null -> value as T
        else -> onFailure(exception)
    }
}

/**
 * Returns the encapsulated value if this instance represents [success][PacketResult.isSuccess] or the
 * [defaultValue] if it is [failure][PacketResult.isFailure].
 *
 * This function is a shorthand for `getOrElse { defaultValue }` (see [getOrElse]).
 */
public fun <R : OutgoingGameMessage, T : R> PacketResult<T>.getOrDefault(defaultValue: R): R {
    if (isFailure) return defaultValue
    @Suppress("UNCHECKED_CAST")
    return value as T
}

/**
 * Returns the result of [onSuccess] for the encapsulated value if this instance represents [success][PacketResult.isSuccess]
 * or the result of [onFailure] function for the encapsulated [Throwable] exception if it is [failure][PacketResult.isFailure].
 *
 * Note, that this function rethrows any [Throwable] exception thrown by [onSuccess] or by [onFailure] function.
 */
@OptIn(ExperimentalContracts::class)
public inline fun <R, T : OutgoingGameMessage> PacketResult<T>.fold(
    onSuccess: (value: T) -> R,
    onFailure: (exception: Throwable) -> R,
): R {
    contract {
        callsInPlace(onSuccess, InvocationKind.AT_MOST_ONCE)
        callsInPlace(onFailure, InvocationKind.AT_MOST_ONCE)
    }
    @Suppress("UNCHECKED_CAST")
    return when (val exception = exceptionOrNull()) {
        null -> onSuccess(value as T)
        else -> onFailure(exception)
    }
}

// transformation

/**
 * Returns the encapsulated result of the given [transform] function applied to the encapsulated value
 * if this instance represents [success][PacketResult.isSuccess] or the
 * original encapsulated [Throwable] exception if it is [failure][PacketResult.isFailure].
 *
 * Note, that this function rethrows any [Throwable] exception thrown by [transform] function.
 * See [mapCatching] for an alternative that encapsulates exceptions.
 */
@OptIn(ExperimentalContracts::class)
public inline fun <R : OutgoingGameMessage, T : OutgoingGameMessage> PacketResult<T>.map(
    transform: (value: T) -> R,
): PacketResult<R> {
    contract {
        callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
    }
    @Suppress("UNCHECKED_CAST")
    return when {
        isSuccess -> PacketResult.success(transform(value as T))
        else -> PacketResult(value)
    }
}

/**
 * Returns the encapsulated result of the given [transform] function applied to the encapsulated value
 * if this instance represents [success][PacketResult.isSuccess] or the
 * original encapsulated [Throwable] exception if it is [failure][PacketResult.isFailure].
 *
 * This function catches any [Throwable] exception thrown by [transform] function and encapsulates it as a failure.
 * See [map] for an alternative that rethrows exceptions from `transform` function.
 */
public inline fun <R : OutgoingGameMessage, T : OutgoingGameMessage> PacketResult<T>.mapCatching(
    transform: (value: T) -> R,
): PacketResult<R> {
    return when {
        isSuccess ->
            try {
                @Suppress("UNCHECKED_CAST")
                PacketResult.success(transform(value as T))
            } catch (t: Throwable) {
                return PacketResult.failure(t)
            }
        else -> PacketResult(value)
    }
}

/**
 * Returns the encapsulated result of the given [transform] function applied to the encapsulated [Throwable] exception
 * if this instance represents [failure][PacketResult.isFailure] or the
 * original encapsulated value if it is [success][PacketResult.isSuccess].
 *
 * Note, that this function rethrows any [Throwable] exception thrown by [transform] function.
 * See [recoverCatching] for an alternative that encapsulates exceptions.
 */
@OptIn(ExperimentalContracts::class)
public inline fun <R : OutgoingGameMessage, T : R> PacketResult<T>.recover(
    transform: (exception: Throwable) -> R,
): PacketResult<R> {
    contract {
        callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
    }
    return when (val exception = exceptionOrNull()) {
        null -> this
        else -> PacketResult.success(transform(exception))
    }
}

/**
 * Returns the encapsulated result of the given [transform] function applied to the encapsulated [Throwable] exception
 * if this instance represents [failure][PacketResult.isFailure] or the
 * original encapsulated value if it is [success][PacketResult.isSuccess].
 *
 * This function catches any [Throwable] exception thrown by [transform] function and encapsulates it as a failure.
 * See [recover] for an alternative that rethrows exceptions.
 */
public inline fun <R : OutgoingGameMessage, T : R> PacketResult<T>.recoverCatching(
    transform: (exception: Throwable) -> R,
): PacketResult<R> {
    return when (val exception = exceptionOrNull()) {
        null -> this
        else -> {
            try {
                PacketResult.success(transform(exception))
            } catch (t: Throwable) {
                PacketResult.failure(t)
            }
        }
    }
}

// "peek" onto value/exception and pipe

/**
 * Performs the given [action] on the encapsulated [Throwable] exception if this instance represents [failure][PacketResult.isFailure].
 * Returns the original `Result` unchanged.
 */
@OptIn(ExperimentalContracts::class)
public inline fun <T : OutgoingGameMessage> PacketResult<T>.onFailure(
    action: (exception: Throwable) -> Unit,
): PacketResult<T> {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }
    exceptionOrNull()?.let { action(it) }
    return this
}

/**
 * Performs the given [action] on the encapsulated value if this instance represents [success][PacketResult.isSuccess].
 * Returns the original `Result` unchanged.
 */
@OptIn(ExperimentalContracts::class)
public inline fun <T : OutgoingGameMessage> PacketResult<T>.onSuccess(action: (value: T) -> Unit): PacketResult<T> {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }
    @Suppress("UNCHECKED_CAST")
    if (isSuccess) action(value as T)
    return this
}
