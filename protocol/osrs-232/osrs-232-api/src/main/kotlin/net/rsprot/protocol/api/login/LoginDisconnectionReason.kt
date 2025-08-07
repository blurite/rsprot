package net.rsprot.protocol.api.login

import net.rsprot.protocol.loginprot.outgoing.LoginResponse

public enum class LoginDisconnectionReason {
    CHANNEL_EXCEPTION,
    CHANNEL_IDLE,
    CHANNEL_OUT_OF_DATE,
    CHANNEL_IP_LIMIT,
    CHANNEL_WRITE_FAILED,
    CHANNEL_UNKNOWN_PACKET,

    CONNECTION_EXCEPTION,
    CONNECTION_IDLE,
    CONNECTION_UNKNOWN_PACKET,
    CONNECTION_INVALID_STEP_AWAITING_BETA_RESPONSE,
    CONNECTION_INVALID_STEP_UNINITIALIZED,
    CONNECTION_INVALID_STEP_REQUESTED_PROOF_OF_WORK,
    CONNECTION_PROOF_OF_WORK_FAILED,
    CONNECTION_PROOF_OF_WORK_EXCEPTION,
    CONNECTION_GAME_LOGIN_DECODING_FAILED,
    CONNECTION_GAME_RECONNECT_FAILED,
    CONNECTION_SESSION_ID_MISMATCH,

    GAME_CHANNEL_INACTIVE,

    // The entries below are 1:1 linked to LoginServerProt
    GAME_INVALID_USERNAME_OR_PASSWORD,
    GAME_BANNED,
    GAME_DUPLICATE,
    GAME_CLIENT_OUT_OF_DATE,
    GAME_SERVER_FULL,
    GAME_LOGINSERVER_OFFLINE,
    GAME_IP_LIMIT,
    GAME_BAD_SESSION_ID,
    GAME_FORCE_PASSWORD_CHANGE,
    GAME_NEED_MEMBERS_ACCOUNT,
    GAME_INVALID_SAVE,
    GAME_UPDATE_IN_PROGRESS,
    GAME_RECONNECT_OK,
    GAME_TOO_MANY_ATTEMPTS,
    GAME_IN_MEMBERS_AREA,
    GAME_LOCKED,
    GAME_CLOSED_BETA_INVITED_ONLY,
    GAME_INVALID_LOGINSERVER,
    GAME_HOP_BLOCKED,
    GAME_INVALID_LOGIN_PACKET,
    GAME_LOGINSERVER_NO_REPLY,
    GAME_LOGINSERVER_LOAD_ERROR,
    GAME_UNKNOWN_REPLY_FROM_LOGINSERVER,
    GAME_IP_BLOCKED,
    GAME_SERVICE_UNAVAILABLE,
    GAME_DISALLOWED_BY_SCRIPT,
    GAME_DISPLAYNAME_REQUIRED,
    GAME_NEGATIVE_CREDIT,
    GAME_INVALID_SINGLE_SIGNON,
    GAME_NO_REPLY_FROM_SINGLE_SIGNON,
    GAME_PROFILE_BEING_EDITED,
    GAME_NO_BETA_ACCESS,
    GAME_INSTANCE_INVALID,
    GAME_INSTANCE_NOT_SPECIFIED,
    GAME_INSTANCE_FULL,
    GAME_IN_QUEUE,
    GAME_ALREADY_IN_QUEUE,
    GAME_BILLING_TIMEOUT,
    GAME_NOT_AGREED_TO_NDA,
    GAME_EMAIL_NOT_VALIDATED,
    GAME_CONNECT_FAIL,
    GAME_PRIVACY_POLICY,
    GAME_AUTHENTICATOR,
    GAME_INVALID_AUTHENTICATOR_CODE,
    GAME_UPDATE_DOB,
    GAME_TIMEOUT,
    GAME_KICK,
    GAME_RETRY,
    GAME_LOGIN_FAIL_1,
    GAME_LOGIN_FAIL_2,
    GAME_OUT_OF_DATE_RELOAD,
    GAME_PROOF_OF_WORK,
    GAME_DOB_ERROR,
    GAME_WEBSITE_DOB,
    GAME_DOB_REVIEW,
    GAME_CLOSED_BETA,
    ;

    internal companion object {
        internal val responseToReasonMap: Map<LoginResponse, LoginDisconnectionReason> = buildResponseToReasonMap()

        private fun buildResponseToReasonMap(): Map<LoginResponse, LoginDisconnectionReason> =
            buildMap {
                put(LoginResponse.InvalidUsernameOrPassword, GAME_INVALID_USERNAME_OR_PASSWORD)
                put(LoginResponse.Banned, GAME_BANNED)
                put(LoginResponse.Duplicate, GAME_DUPLICATE)
                put(LoginResponse.ClientOutOfDate, GAME_CLIENT_OUT_OF_DATE)
                put(LoginResponse.ServerFull, GAME_SERVER_FULL)
                put(LoginResponse.LoginServerOffline, GAME_LOGINSERVER_OFFLINE)
                put(LoginResponse.IPLimit, GAME_IP_LIMIT)
                put(LoginResponse.BadSessionId, GAME_BAD_SESSION_ID)
                put(LoginResponse.ForcePasswordChange, GAME_FORCE_PASSWORD_CHANGE)
                put(LoginResponse.NeedMembersAccount, GAME_NEED_MEMBERS_ACCOUNT)
                put(LoginResponse.InvalidSave, GAME_INVALID_SAVE)
                put(LoginResponse.UpdateInProgress, GAME_UPDATE_IN_PROGRESS)
                put(LoginResponse.TooManyAttempts, GAME_TOO_MANY_ATTEMPTS)
                put(LoginResponse.InMembersArea, GAME_IN_MEMBERS_AREA)
                put(LoginResponse.Locked, GAME_LOCKED)
                put(LoginResponse.ClosedBetaInvitedOnly, GAME_CLOSED_BETA_INVITED_ONLY)
                put(LoginResponse.InvalidLoginServer, GAME_INVALID_LOGINSERVER)
                put(LoginResponse.HopBlocked, GAME_HOP_BLOCKED)
                put(LoginResponse.InvalidLoginPacket, GAME_INVALID_LOGIN_PACKET)
                put(LoginResponse.LoginServerNoReply, GAME_LOGINSERVER_NO_REPLY)
                put(LoginResponse.LoginServerLoadError, GAME_LOGINSERVER_LOAD_ERROR)
                put(LoginResponse.UnknownReplyFromLoginServer, GAME_UNKNOWN_REPLY_FROM_LOGINSERVER)
                put(LoginResponse.IPBlocked, GAME_IP_BLOCKED)
                put(LoginResponse.ServiceUnavailable, GAME_SERVICE_UNAVAILABLE)
                put(LoginResponse.DisplayNameRequired, GAME_DISPLAYNAME_REQUIRED)
                put(LoginResponse.NegativeCredit, GAME_NEGATIVE_CREDIT)
                put(LoginResponse.InvalidSingleSignOn, GAME_INVALID_SINGLE_SIGNON)
                put(LoginResponse.NoReplyFromSingleSignOn, GAME_NO_REPLY_FROM_SINGLE_SIGNON)
                put(LoginResponse.ProfileBeingEdited, GAME_PROFILE_BEING_EDITED)
                put(LoginResponse.NoBetaAccess, GAME_NO_BETA_ACCESS)
                put(LoginResponse.InstanceInvalid, GAME_INSTANCE_INVALID)
                put(LoginResponse.InstanceNotSpecified, GAME_INSTANCE_NOT_SPECIFIED)
                put(LoginResponse.InstanceFull, GAME_INSTANCE_FULL)
                put(LoginResponse.InQueue, GAME_IN_QUEUE)
                put(LoginResponse.AlreadyInQueue, GAME_ALREADY_IN_QUEUE)
                put(LoginResponse.BillingTimeout, GAME_BILLING_TIMEOUT)
                put(LoginResponse.NotAgreedToNda, GAME_NOT_AGREED_TO_NDA)
                put(LoginResponse.EmailNotValidated, GAME_EMAIL_NOT_VALIDATED)
                put(LoginResponse.ConnectFail, GAME_CONNECT_FAIL)
                put(LoginResponse.PrivacyPolicy, GAME_PRIVACY_POLICY)
                put(LoginResponse.Authenticator, GAME_AUTHENTICATOR)
                put(LoginResponse.InvalidAuthenticatorCode, GAME_INVALID_AUTHENTICATOR_CODE)
                put(LoginResponse.UpdateDob, GAME_UPDATE_DOB)
                put(LoginResponse.Timeout, GAME_TIMEOUT)
                put(LoginResponse.Kick, GAME_KICK)
                put(LoginResponse.Retry, GAME_RETRY)
                put(LoginResponse.LoginFail1, GAME_LOGIN_FAIL_1)
                put(LoginResponse.LoginFail2, GAME_LOGIN_FAIL_2)
                put(LoginResponse.OutOfDateReload, GAME_OUT_OF_DATE_RELOAD)
                put(LoginResponse.DobError, GAME_DOB_ERROR)
                put(LoginResponse.DobReview, GAME_DOB_REVIEW)
                put(LoginResponse.ClosedBeta, GAME_CLOSED_BETA)
            }
    }
}
