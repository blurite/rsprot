package net.rsprot.protocol.loginprot.outgoing.prot

import net.rsprot.protocol.ProtRepository
import net.rsprot.protocol.loginprot.outgoing.LoginResponse
import net.rsprot.protocol.loginprot.outgoing.codec.DisallowedByScriptLoginResponseEncoder
import net.rsprot.protocol.loginprot.outgoing.codec.EmptyLoginResponseEncoder
import net.rsprot.protocol.loginprot.outgoing.codec.OkLoginResponseEncoder
import net.rsprot.protocol.message.codec.outgoing.MessageEncoderRepository
import net.rsprot.protocol.message.codec.outgoing.MessageEncoderRepositoryBuilder
import net.rsprot.protocol.shared.platform.PlatformType

private typealias Encoder<T> = EmptyLoginResponseEncoder<T>

public object LoginMessageEncoderRepository {
    @ExperimentalStdlibApi
    public fun build(): MessageEncoderRepository<LoginServerProt, PlatformType> {
        val protRepository = ProtRepository.of<LoginServerProt>()
        val builder =
            MessageEncoderRepositoryBuilder(
                PlatformType.DESKTOP,
                protRepository,
            ).apply {
                bind(OkLoginResponseEncoder())
                bind(DisallowedByScriptLoginResponseEncoder())
                bind(Encoder<LoginResponse.Successful>(LoginServerProt.SUCCESSFUL))
                bind(Encoder<LoginResponse.InvalidUsernameOrPassword>(LoginServerProt.INVALID_USERNAME_OR_PASSWORD))
                bind(Encoder<LoginResponse.Banned>(LoginServerProt.BANNED))
                bind(Encoder<LoginResponse.Duplicate>(LoginServerProt.DUPLICATE))
                bind(Encoder<LoginResponse.ClientOutOfDate>(LoginServerProt.CLIENT_OUT_OF_DATE))
                bind(Encoder<LoginResponse.ServerFull>(LoginServerProt.SERVER_FULL))
                bind(Encoder<LoginResponse.LoginServerOffline>(LoginServerProt.LOGINSERVER_OFFLINE))
                bind(Encoder<LoginResponse.IPLimit>(LoginServerProt.IP_LIMIT))
                bind(Encoder<LoginResponse.BadSessionId>(LoginServerProt.BAD_SESSION_ID))
                bind(Encoder<LoginResponse.ForcePasswordChange>(LoginServerProt.FORCE_PASSWORD_CHANGE))
                bind(Encoder<LoginResponse.NeedMembersAccount>(LoginServerProt.NEED_MEMBERS_ACCOUNT))
                bind(Encoder<LoginResponse.InvalidSave>(LoginServerProt.INVALID_SAVE))
                bind(Encoder<LoginResponse.UpdateInProgress>(LoginServerProt.UPDATE_IN_PROGRESS))
                bind(Encoder<LoginResponse.ReconnectOk>(LoginServerProt.RECONNECT_OK))
                bind(Encoder<LoginResponse.TooManyAttempts>(LoginServerProt.TOO_MANY_ATTEMPTS))
                bind(Encoder<LoginResponse.InMembersArea>(LoginServerProt.IN_MEMBERS_AREA))
                bind(Encoder<LoginResponse.Locked>(LoginServerProt.LOCKED))
                bind(Encoder<LoginResponse.ClosedBetaInvitedOnly>(LoginServerProt.CLOSED_BETA_INVITED_ONLY))
                bind(Encoder<LoginResponse.InvalidLoginServer>(LoginServerProt.INVALID_LOGINSERVER))
                bind(Encoder<LoginResponse.HopBlocked>(LoginServerProt.HOP_BLOCKED))
                bind(Encoder<LoginResponse.InvalidLoginPacket>(LoginServerProt.INVALID_LOGIN_PACKET))
                bind(Encoder<LoginResponse.LoginServerNoReply>(LoginServerProt.LOGINSERVER_NO_REPLY))
                bind(Encoder<LoginResponse.LoginServerLoadError>(LoginServerProt.LOGINSERVER_LOAD_ERROR))
                bind(Encoder<LoginResponse.UnknownReplyFromLoginServer>(LoginServerProt.UNKNOWN_REPLY_FROM_LOGINSERVER))
                bind(Encoder<LoginResponse.IPBlocked>(LoginServerProt.IP_BLOCKED))
                bind(Encoder<LoginResponse.ServiceUnavailable>(LoginServerProt.SERVICE_UNAVAILABLE))
                bind(Encoder<LoginResponse.DisplayNameRequired>(LoginServerProt.DISPLAYNAME_REQUIRED))
                bind(Encoder<LoginResponse.NegativeCredit>(LoginServerProt.NEGATIVE_CREDIT))
                bind(Encoder<LoginResponse.InvalidSingleSignOn>(LoginServerProt.INVALID_SINGLE_SIGNON))
                bind(Encoder<LoginResponse.NoReplyFromSingleSignOn>(LoginServerProt.NO_REPLY_FROM_SINGLE_SIGNON))
                bind(Encoder<LoginResponse.ProfileBeingEdited>(LoginServerProt.PROFILE_BEING_EDITED))
                bind(Encoder<LoginResponse.NoBetaAccess>(LoginServerProt.NO_BETA_ACCESS))
                bind(Encoder<LoginResponse.InstanceInvalid>(LoginServerProt.INSTANCE_INVALID))
                bind(Encoder<LoginResponse.InstanceNotSpecified>(LoginServerProt.INSTANCE_NOT_SPECIFIED))
                bind(Encoder<LoginResponse.InstanceFull>(LoginServerProt.INSTANCE_FULL))
                bind(Encoder<LoginResponse.InQueue>(LoginServerProt.IN_QUEUE))
                bind(Encoder<LoginResponse.AlreadyInQueue>(LoginServerProt.ALREADY_IN_QUEUE))
                bind(Encoder<LoginResponse.BillingTimeout>(LoginServerProt.BILLING_TIMEOUT))
                bind(Encoder<LoginResponse.NotAgreedToNda>(LoginServerProt.NOT_AGREED_TO_NDA))
                bind(Encoder<LoginResponse.EmailNotValidated>(LoginServerProt.EMAIL_NOT_VALIDATED))
                bind(Encoder<LoginResponse.ConnectFail>(LoginServerProt.CONNECT_FAIL))
                bind(Encoder<LoginResponse.PrivacyPolicy>(LoginServerProt.PRIVACY_POLICY))
                bind(Encoder<LoginResponse.Authenticator>(LoginServerProt.AUTHENTICATOR))
                bind(Encoder<LoginResponse.InvalidAuthenticatorCode>(LoginServerProt.INVALID_AUTHENTICATOR_CODE))
                bind(Encoder<LoginResponse.UpdateDob>(LoginServerProt.UPDATE_DOB))
                bind(Encoder<LoginResponse.Timeout>(LoginServerProt.TIMEOUT))
                bind(Encoder<LoginResponse.Kick>(LoginServerProt.KICK))
                bind(Encoder<LoginResponse.Retry>(LoginServerProt.RETRY))
                bind(Encoder<LoginResponse.LoginFail1>(LoginServerProt.LOGIN_FAIL_1))
                bind(Encoder<LoginResponse.LoginFail2>(LoginServerProt.LOGIN_FAIL_2))
                bind(Encoder<LoginResponse.OutOfDateReload>(LoginServerProt.OUT_OF_DATE_RELOAD))
                bind(Encoder<LoginResponse.ProofOfWork>(LoginServerProt.PROOF_OF_WORK))
                bind(Encoder<LoginResponse.DobError>(LoginServerProt.DOB_ERROR))
                bind(Encoder<LoginResponse.DobReview>(LoginServerProt.DOB_REVIEW))
                bind(Encoder<LoginResponse.ClosedBeta>(LoginServerProt.CLOSED_BETA))
            }
        return builder.build()
    }
}
