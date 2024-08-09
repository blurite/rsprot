package net.rsprot.protocol.loginprot.outgoing

import io.netty.buffer.ByteBuf
import io.netty.buffer.DefaultByteBufHolder
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfo
import net.rsprot.protocol.loginprot.outgoing.util.AuthenticatorResponse
import net.rsprot.protocol.message.OutgoingLoginMessage

public sealed interface LoginResponse : OutgoingLoginMessage {
    public data class Successful(
        public val sessionId: Long?,
    ) : LoginResponse

    @Suppress("DataClassPrivateConstructor")
    public data class Ok private constructor(
        public val authenticatorResponse: AuthenticatorResponse,
        private val _staffModLevel: UByte,
        public val playerMod: Boolean,
        private val _index: UShort,
        public val member: Boolean,
        public val accountHash: Long,
        public val userId: Long,
        public val userHash: Long,
    ) : LoginResponse {
        public constructor(
            authenticatorResponse: AuthenticatorResponse,
            staffModLevel: Int,
            playerMod: Boolean,
            index: Int,
            member: Boolean,
            accountHash: Long,
            userId: Long,
            userHash: Long,
        ) : this(
            authenticatorResponse,
            staffModLevel.toUByte(),
            playerMod,
            index.toUShort(),
            member,
            accountHash,
            userId,
            userHash,
        )

        public val staffModLevel: Int
            get() = _staffModLevel.toInt()
        public val index: Int
            get() = _index.toInt()

        override fun toString(): String =
            "Ok(" +
                "authenticatorResponse=$authenticatorResponse, " +
                "playerMod=$playerMod, " +
                "member=$member, " +
                "accountHash=$accountHash, " +
                "userId=$userId, " +
                "userHash=$userHash, " +
                "staffModLevel=$staffModLevel, " +
                "index=$index" +
                ")"
    }

    public data object InvalidUsernameOrPassword : LoginResponse

    public data object Banned : LoginResponse

    public data object Duplicate : LoginResponse

    public data object ClientOutOfDate : LoginResponse

    public data object ServerFull : LoginResponse

    public data object LoginServerOffline : LoginResponse

    public data object IPLimit : LoginResponse

    public data object BadSessionId : LoginResponse

    public data object ForcePasswordChange : LoginResponse

    public data object NeedMembersAccount : LoginResponse

    public data object InvalidSave : LoginResponse

    public data object UpdateInProgress : LoginResponse

    public class ReconnectOk(
        buffer: ByteBuf,
    ) : DefaultByteBufHolder(buffer),
        LoginResponse {
        public constructor(worldId: Int, playerInfo: PlayerInfo) : this(
            initializePlayerInfo(worldId, playerInfo),
        )

        private companion object {
            private const val PLAYER_INFO_BLOCK_SIZE = ((30 + (2046 * 18)) + Byte.SIZE_BITS - 1) ushr 3

            /**
             * Initializes the player info block into a buffer provided by allocator in the playerinfo object
             * @param playerInfo the player info protocol of this player to be initialized
             * @return a buffer containing the initialization block of the player info protocol
             */
            private fun initializePlayerInfo(
                worldId: Int,
                playerInfo: PlayerInfo,
            ): ByteBuf {
                val allocator = playerInfo.allocator
                val buffer = allocator.buffer(PLAYER_INFO_BLOCK_SIZE)
                playerInfo.handleAbsolutePlayerPositions(worldId, buffer)
                return buffer
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            if (!super.equals(other)) return false
            return true
        }

        override fun hashCode(): Int = super.hashCode()

        override fun toString(): String = "ReconnectOk()"
    }

    public data object TooManyAttempts : LoginResponse

    public data object InMembersArea : LoginResponse

    public data object Locked : LoginResponse

    public data object ClosedBetaInvitedOnly : LoginResponse

    public data object InvalidLoginServer : LoginResponse

    public data object HopBlocked : LoginResponse

    public data object InvalidLoginPacket : LoginResponse

    public data object LoginServerNoReply : LoginResponse

    public data object LoginServerLoadError : LoginResponse

    public data object UnknownReplyFromLoginServer : LoginResponse

    public data object IPBlocked : LoginResponse

    public data object ServiceUnavailable : LoginResponse

    public data class DisallowedByScript(
        public val line1: String,
        public val line2: String,
        public val line3: String,
    ) : LoginResponse

    public data object DisplayNameRequired : LoginResponse

    public data object NegativeCredit : LoginResponse

    public data object InvalidSingleSignOn : LoginResponse

    public data object NoReplyFromSingleSignOn : LoginResponse

    public data object ProfileBeingEdited : LoginResponse

    public data object NoBetaAccess : LoginResponse

    public data object InstanceInvalid : LoginResponse

    public data object InstanceNotSpecified : LoginResponse

    public data object InstanceFull : LoginResponse

    public data object InQueue : LoginResponse

    public data object AlreadyInQueue : LoginResponse

    public data object BillingTimeout : LoginResponse

    public data object NotAgreedToNda : LoginResponse

    public data object EmailNotValidated : LoginResponse

    public data object ConnectFail : LoginResponse

    public data object PrivacyPolicy : LoginResponse

    public data object Authenticator : LoginResponse

    public data object InvalidAuthenticatorCode : LoginResponse

    public data object UpdateDob : LoginResponse

    public data object Timeout : LoginResponse

    public data object Kick : LoginResponse

    public data object Retry : LoginResponse

    public data object LoginFail1 : LoginResponse

    public data object LoginFail2 : LoginResponse

    public data object OutOfDateReload : LoginResponse

    public class ProofOfWork(
        public val proofOfWork: net.rsprot.protocol.loginprot.incoming.pow.ProofOfWork<*, *>,
    ) : LoginResponse

    public data object DobError : LoginResponse

    public data object DobReview : LoginResponse

    public data object ClosedBeta : LoginResponse
}
