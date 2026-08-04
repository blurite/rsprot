package net.rsprot.protocol.game.outgoing.info

/**
 * A class that holds all the info packets for a given player in a single game cycle.
 * @property rootWorldInfoPackets all the packets that are part of the root world.
 * This includes world entity info and player info, as the two are calculated globally once.
 * @property removedWorldIndices the ids of the worlds that were removed from high resolution in this cycle,
 * allowing the server to stop tracking the worlds for zone updates.
 * @property activeWorlds a list of world info packets per active world.
 */
public class InfoPackets(
    public val rootWorldInfoPackets: RootWorldInfoPackets,
    public val removedWorldIndices: List<Int>,
    public val activeWorlds: List<WorldInfoPackets>,
)
