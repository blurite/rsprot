package net.rsprot.protocol.common.platform

import net.rsprot.protocol.platform.Platform

public class PlatformMap<out T>
    @PublishedApi
    internal constructor(
        private val array: Array<T?>,
    ) {
        public val size: Int
            get() = array.size

        public val notNullSize: Int
            get() = array.count { it != null }

        public operator fun get(platform: Platform): T {
            return requireNotNull(array[platform.id]) {
                "Platform $platform not initialized!"
            }
        }

        public fun getOrNull(platform: Platform): T? {
            return array[platform.id]
        }

        public fun getOrNull(platformId: Int): T? {
            return array[platformId]
        }

        public operator fun contains(platform: Platform): Boolean {
            return array[platform.id] != null
        }

        public companion object {
            public inline fun <reified T> of(
                elements: List<T>,
                platformCapacity: Int,
                platformSelector: (T) -> Platform,
            ): PlatformMap<T> {
                val array = arrayOfNulls<T>(platformCapacity)
                for (element in elements) {
                    val platform = platformSelector(element)
                    check(array[platform.id] == null) {
                        "A platform is registered more than once: $elements"
                    }
                    array[platform.id] = element
                }
                return PlatformMap(array)
            }

            public inline fun <T, reified E> ofType(
                elements: List<T>,
                platformCapacity: Int,
                platformSelector: (T) -> Pair<Platform, E>,
            ): PlatformMap<E> {
                val array = arrayOfNulls<E>(platformCapacity)
                for (pair in elements) {
                    val (platform, element) = platformSelector(pair)
                    check(array[platform.id] == null) {
                        "A platform is registered more than once: $elements"
                    }
                    array[platform.id] = element
                }
                return PlatformMap(array)
            }
        }
    }
