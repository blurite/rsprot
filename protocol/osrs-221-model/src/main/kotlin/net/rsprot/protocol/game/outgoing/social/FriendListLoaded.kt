package net.rsprot.protocol.game.outgoing.social

import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Friend list loaded is used to mark the friend list
 * as loaded if there are no friends to be sent.
 * If there are friends to be sent, use the [UpdateFriendList]
 * packet instead without this.
 */
public data object FriendListLoaded : OutgoingGameMessage
