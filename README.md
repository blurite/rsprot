# RSProt

[![GitHub Actions][actions-badge]][actions] [![MIT license][mit-badge]][mit]

## Introduction
RSProt is an all-in-one networking library for private servers,
primarily targeting the OldSchool RuneScape scene. Contributions for
other revisions are welcome, but will not be provided by default.

## Prerequisites
- Kotlin 1.9.23
- Java 1.8

## Design Choices

### Memory-Optimized Messages
A common design choice throughout this library will be to utilize smaller data types wherever applicable.
The end-user will always get access to normalized messages though.

Below are two examples of the same data structure, one in a compressed data structure, another in a traditional data class:
<details>
  <summary>Compressed HostPlatformStats</summary>

```kt
public class HostPlatformStats(
    private val _version: UByte,
    private val _osType: UByte,
    public val os64Bit: Boolean,
    private val _osVersion: UShort,
    private val _javaVendor: UByte,
    private val _javaVersionMajor: UByte,
    private val _javaVersionMinor: UByte,
    private val _javaVersionPatch: UByte,
    private val _unknownConstZero1: UByte,
    private val _javaMaxMemoryMb: UShort,
    private val _javaAvailableProcessors: UByte,
    public val systemMemory: Int,
    private val _systemSpeed: UShort,
    public val gpuDxName: String,
    public val gpuGlName: String,
    public val gpuDxVersion: String,
    public val gpuGlVersion: String,
    private val _gpuDriverMonth: UByte,
    private val _gpuDriverYear: UShort,
    public val cpuManufacturer: String,
    public val cpuBrand: String,
    private val _cpuCount1: UByte,
    private val _cpuCount2: UByte,
    public val cpuFeatures: IntArray,
    public val cpuSignature: Int,
    public val clientName: String,
    public val deviceName: String,
) {
    public val version: Int
        get() = _version.toInt()
    public val osType: Int
        get() = _osType.toInt()
    public val osVersion: Int
        get() = _osVersion.toInt()
    public val javaVendor: Int
        get() = _javaVendor.toInt()
    public val javaVersionMajor: Int
        get() = _javaVersionMajor.toInt()
    public val javaVersionMinor: Int
        get() = _javaVersionMinor.toInt()
    public val javaVersionPatch: Int
        get() = _javaVersionPatch.toInt()
    public val unknownConstZero: Int
        get() = _unknownConstZero1.toInt()
    public val javaMaxMemoryMb: Int
        get() = _javaMaxMemoryMb.toInt()
    public val javaAvailableProcessors: Int
        get() = _javaAvailableProcessors.toInt()
    public val systemSpeed: Int
        get() = _systemSpeed.toInt()
    public val gpuDriverMonth: Int
        get() = _gpuDriverMonth.toInt()
    public val gpuDriverYear: Int
        get() = _gpuDriverYear.toInt()
    public val cpuCount1: Int
        get() = _cpuCount1.toInt()
    public val cpuCount2: Int
        get() = _cpuCount2.toInt()
}
```
</details>

<details>
  <summary>Traditional HostPlatformStats</summary>

```kt
public data class HostPlatformStats(
    public val version: Int,
    public val osType: Int,
    public val os64Bit: Boolean,
    public val osVersion: Int,
    public val javaVendor: Int,
    public val javaVersionMajor: Int,
    public val javaVersionMinor: Int,
    public val javaVersionPatch: Int,
    public val unknownConstZero1: Int,
    public val javaMaxMemoryMb: Int,
    public val javaAvailableProcessors: Int,
    public val systemMemory: Int,
    public val systemSpeed: Int,
    public val gpuDxName: String,
    public val gpuGlName: String,
    public val gpuDxVersion: String,
    public val gpuGlVersion: String,
    public val gpuDriverMonth: Int,
    public val gpuDriverYear: Int,
    public val cpuManufacturer: String,
    public val cpuBrand: String,
    public val cpuCount1: Int,
    public val cpuCount2: Int,
    public val cpuFeatures: IntArray,
    public val cpuSignature: Int,
    public val clientName: String,
    public val deviceName: String,
)
```
 </details>

> [!IMPORTANT]
> There is a common misconception among developers that types on heap smaller than ints are only useful in their respective primitive arrays.
> In reality, this is only sometimes true. There are a lot more aspects to consider. Below is a breakdown on the differences.


<details>
  <summary>Memory Alignment Breakdown</summary>

[JVM's memory alignment](https://www.baeldung.com/java-memory-layout) is the reason why we prioritize compressed messages over traditional ones.
It is commonly believed that primitives like bytes and shorts do not matter on the heap and end up consuming the same amount of memory as an int,
but this is simply not true. The object itself is subject to memory alignment and will be padded to a specific amount of bytes as a whole.
Given this information, we can see the stark differences between the two objects by adding up the memory usage of each of the properties.
For this example, we will assume all the strings are empty and stored in the
[JVM's string constant pool](https://www.baeldung.com/java-string-constant-pool-heap-stack), so we only consider the reference of those.
The cpuFeatures array is a size-3 int array.
By adding up all the properties of the compressed variant of the HostPlatformStats, we come to the following results:
| Type | Count | Data Size (bytes) |
| --- | --- | --- |
| byte | 11 | 1 |
| boolean | 1 | 1 |
| short | 4 | 2 |
| int | 2 | 4 |
| intarray | 1 | Special |
| reference | 8 | Special |

By adding up all the data types, we come to a sum of (11 x 1) + (1 x 1) + (4 x 2) + (2 x 4) + (1 x intarray) + (8 x reference),
which adds up to 28 + (1 x intarray) + (8 x reference) bytes.

However, now, let's look at the traditional variant:
| Type | Count | Data Size (bytes) |
| --- | --- | --- |
| int | 18 | 4 |
| intarray | 1 | Special |
| reference | 8 | Special |

The total adds up to (18 x 4) + (1 x intarray) + (8 x reference),
which adds up to 72 + (1 x intarray) + (8 x reference) bytes.

<ins>So, what about the special types?</ins>

This is where things become less certain. It is down to the JVM and the amount of memory allocated to the JVM process.

On a 32-bit JVM, the memory layout looks like this:
| Type | Data Size (bytes) |
| --- | --- |
| Object Header | 8 |
| Object Reference | 4 |
| Byte Alignment | 4 |

On a 64-bit JVM, the memory layout is as following:
| Type | Data Size (bytes) |
| --- | --- |
| Object Header | 12 |
| Object Reference (xmx <= 32gb, compressed OOPs[^1]) | 4 |
| Object Reference (xmx > 32gb) | 8 |
| Byte Alignment | 8 |

So, how much do our HostPlatformStats objects consume in the end?
If we assume we are on a 64-bit JVM with the maximum heap size set to 32GB or less, the object memory consumption boils down to the following:
From the earlier example, the intarray will consume 12 + (3 * 4) bytes, and the string references will consume 4 bytes each.
So if we now add these values up, we come to a total of:
Compressed HostPlatformStats: 84 bytes
Traditional HostPlatformStats: 128 bytes
Due to the JVM's 8 byte alignment however, all objects are aligned to consume a multiple of 8 bytes.
In this scenario, because our compressed implementation comes to 84 bytes, which is not a multiple-of-8 bytes, a small waste occurs.
The JVM will allocate 4 extra bytes to fit the 8-byte alignment constraint, giving us a total of 88 bytes consumed.
In the case of the traditional implementation, since it is already a multiple of 8, it will remain as 128 bytes.

 </details>

> [!NOTE]
> The reason we prefer compressed implementations is to reduce the memory footprint of the library. As demonstrated above,
> the compressed implementation consumes 31.25% less memory than the traditional counterpart.
> While the compressed code may be harder to read and take longer to implement, this is a one-time job as the models rarely change.
> On the larger scale, this could result in a considerably smaller footprint of the library for servers, and less work for garbage collectors.

## Benchmarks
Benchmarks can be found [here](BENCHMARKS.md). Only performance-critical
aspects of the application will be benchmarked.

[^1]: [Compressed ordinary object pointers](https://www.baeldung.com/jvm-compressed-oops) are a trick utilized by the 64-bit JVM to compress object references into 4 bytes instead of the traditional 8. This is only possible if the Xmx is set to 32GB or less. Since Java 7, compressed OOPs are enabled by default if available.

[actions-badge]: https://github.com/blurite/rsprot/actions/workflows/ci.yml/badge.svg
[actions]: https://github.com/blurite/rsprot/actions
[mit-badge]: https://img.shields.io/badge/license-MIT-informational
[mit]: https://opensource.org/license/MIT
