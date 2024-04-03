# RSProt Benchmarks

## Notes
1. All benchmarks are performed with the default configuration.
2. Direct pooled byte buffer allocator is used in these benchmarks.
While this is technically slower during benchmarks than just a heap
byte array, it results in Netty threads not having to do one extra
full copy from heap to native space.
3. These benchmarks are done on a Windows PC with some background noise.
The real production results would very likely show significantly better
results.
4. All benchmarks do 10 x 2-seconds warmup, followed by a 3 x 30-seconds
measurement.

## Hardware
- **OS:** Windows 11 Pro (10.0.22635 Build 22635, 64-bit)
- **CPU:** AMD Ryzen 3950x (16c 32t, 3.7GHz base, 4.825GHz PBO)
- **RAM:** 4x 16GB G.Skill F4-3600C18
- **VM:** JDK 1.8.0_402, 64-Bit Server VM

## Results

### [Player Info](https://github.com/blurite/rsprot/blob/master/protocol/osrs-221-desktop/src/benchmarks/kotlin/net/rsprot/protocol/game/outgoing/info/PlayerInfoBenchmark.kt)

#### Measurements

> [!NOTE]
> The error rate is relatively high due to the semi-random volatile nature
> of the benchmark. On some cycles, if lots of players end up in close
> proximity of one-another, the pressure will be significantly higher than
> cycles where they are more evenly spread apart. Furthermore, the start of the
> benchmark begins with view range 15 for all avatars, this will repeatedly
> decrease until less than 250 avatars are in high resolution.

Single-threaded measurements:
```
Benchmark                      Mode  Cnt    Score   Error  Units
PlayerInfoBenchmark.benchmark  avgt    3  170.230 ± 7.278  ms/op
```

Multi-threaded measurements (default):
```
Benchmark                      Mode  Cnt   Score   Error  Units
PlayerInfoBenchmark.benchmark  avgt    3  11.544 ± 0.154  ms/op
```

> [!TIP]
> 1 operation is equal to 1 game cycle.

Multi-threaded ratio[^1]: 0.92x

#### Benchmark Description
- 2046 players are spawned into the world,
initialized at random within a 13x13 box. All players will remain in the same
13x13 box for the duration of the benchmark.
- Every cycle of the benchmark, all 2046 players will **teleport** to a new
position within the aforementioned 13x13 box, at random. This means that very
often, all old high resolution players need to be removed, and new
low resolution players must be moved to high resolution, due to the view range
trying to stick to a number that has 250 high resolution players or less.
- Appearance is initialized for all 2046 players.
- Caching logic is utilized for appearance, so once another avatar has been
observed, the appearance extended info block is no longer written when that
player goes from low resolution to high resolution again.
- Every game cycle during the benchmark, all 2046 players will use public chat
to type a 50-character long lorem ipsum text. This will utilize Huffman
compression during the encoding.
- At the end of the benchmark, all 2046 primary player info buffers are
released back into the pool. The pre-computed chat extended info block's
buffer is additionally released back into the pool for every player.


[^1]: Multi-threaded ratio refers to how well the application multi-threads.
This is calculated by dividing the single-threaded measurements by
multi-threaded measurements, and further dividing that by the number of
CPU cores. This gives us an effective rate of how well all the CPU threads
are being utilized (on average), with a number of 1.0 implying a perfect rate
of "every CPU core is utilized as well in multi-threaded environment as it is
in the single-threaded benchmark".
