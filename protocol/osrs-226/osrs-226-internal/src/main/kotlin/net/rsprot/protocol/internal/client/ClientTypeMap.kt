package net.rsprot.protocol.internal.client

import net.rsprot.protocol.client.ClientType

public class ClientTypeMap<out T>
    @PublishedApi
    internal constructor(
        private val array: Array<T?>,
    ) {
        public val size: Int
            get() = array.size

        public val notNullSize: Int
            get() = array.count { it != null }

        public operator fun get(clientType: ClientType): T =
            requireNotNull(array[clientType.id]) {
                "Client type $clientType not initialized!"
            }

        public fun getOrNull(clientType: ClientType): T? = array[clientType.id]

        public fun getOrNull(clientId: Int): T? = array[clientId]

        public operator fun contains(clientType: ClientType): Boolean = array[clientType.id] != null

        public companion object {
            public inline fun <reified T> of(
                elements: List<T>,
                clientCapacity: Int,
                clientTypeSelector: (T) -> ClientType,
            ): net.rsprot.protocol.internal.client.ClientTypeMap<T> {
                val array = arrayOfNulls<T>(clientCapacity)
                for (element in elements) {
                    val clientType = clientTypeSelector(element)
                    check(array[clientType.id] == null) {
                        "A client is registered more than once: $elements"
                    }
                    array[clientType.id] = element
                }
                return net.rsprot.protocol.internal.client.ClientTypeMap(array)
            }

            public inline fun <reified E> of(
                clientCapacity: Int,
                elements: List<Pair<ClientType, E>>,
            ): net.rsprot.protocol.internal.client.ClientTypeMap<E> {
                val array = arrayOfNulls<E>(clientCapacity)
                for ((clientType, element) in elements) {
                    check(array[clientType.id] == null) {
                        "A client is registered more than once: $elements"
                    }
                    array[clientType.id] = element
                }
                return net.rsprot.protocol.internal.client.ClientTypeMap(array)
            }

            public inline fun <T, reified E> ofType(
                elements: List<T>,
                clientCapacity: Int,
                clientTypeSelector: (T) -> Pair<ClientType, E>,
            ): net.rsprot.protocol.internal.client.ClientTypeMap<E> {
                val array = arrayOfNulls<E>(clientCapacity)
                for (pair in elements) {
                    val (clientType, element) = clientTypeSelector(pair)
                    check(array[clientType.id] == null) {
                        "A client is registered more than once: $elements"
                    }
                    array[clientType.id] = element
                }
                return net.rsprot.protocol.internal.client.ClientTypeMap(array)
            }
        }
    }
