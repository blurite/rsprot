package net.rsprot.protocol.loginprot.outgoing.prot

import net.rsprot.protocol.ProtRepository
import net.rsprot.protocol.loginprot.outgoing.LoginResponse
import net.rsprot.protocol.loginprot.outgoing.codec.DisallowedByScriptLoginResponseEncoder
import net.rsprot.protocol.loginprot.outgoing.codec.EmptyLoginResponseEncoder
import net.rsprot.protocol.loginprot.outgoing.codec.OkLoginResponseEncoder
import net.rsprot.protocol.message.codec.outgoing.MessageEncoderRepository
import net.rsprot.protocol.message.codec.outgoing.MessageEncoderRepositoryBuilder

private typealias Enc<T> = EmptyLoginResponseEncoder<T>

public object LoginMessageEncoderRepository {
    @ExperimentalStdlibApi
    public fun build(): MessageEncoderRepository {
        val protRepository = ProtRepository.of<LoginServerProt>()
        val builder =
            MessageEncoderRepositoryBuilder(protRepository).apply {
                bind(OkLoginResponseEncoder())
                bind(DisallowedByScriptLoginResponseEncoder())
                bind(Enc<LoginResponse.Successful>(LoginServerProt.SUCCESSFUL))
                bind(Enc<LoginResponse.InvalidUsernameOrPassword>(LoginServerProt.INVALID_USERNAME_OR_PASSWORD))
                bind(Enc<LoginResponse.Banned>(LoginServerProt.BANNED))
                bind(Enc<LoginResponse.Duplicate>(LoginServerProt.DUPLICATE))
                bind(Enc<LoginResponse.ClientOutOfDate>(LoginServerProt.CLIENT_OUT_OF_DATE))
                bind(Enc<LoginResponse.ServerFull>(LoginServerProt.SERVER_FULL))
                bind(Enc<LoginResponse.LoginServerOffline>(LoginServerProt.LOGINSERVER_OFFLINE))
                bind(Enc<LoginResponse.IPLimit>(LoginServerProt.IP_LIMIT))
                bind(Enc<LoginResponse.BadSessionId>(LoginServerProt.BAD_SESSION_ID))
                bind(Enc<LoginResponse.ForcePasswordChange>(LoginServerProt.FORCE_PASSWORD_CHANGE))
                bind(Enc<LoginResponse.NeedMembersAccount>(LoginServerProt.NEED_MEMBERS_ACCOUNT))
                bind(Enc<LoginResponse.InvalidSave>(LoginServerProt.INVALID_SAVE))
                bind(Enc<LoginResponse.UpdateInProgress>(LoginServerProt.UPDATE_IN_PROGRESS))
                bind(Enc<LoginResponse.ReconnectOk>(LoginServerProt.RECONNECT_OK))
                bind(Enc<LoginResponse.TooManyAttempts>(LoginServerProt.TOO_MANY_ATTEMPTS))
                bind(Enc<LoginResponse.InMembersArea>(LoginServerProt.IN_MEMBERS_AREA))
                bind(Enc<LoginResponse.Locked>(LoginServerProt.LOCKED))
                bind(Enc<LoginResponse.ClosedBetaInvitedOnly>(LoginServerProt.CLOSED_BETA_INVITED_ONLY))
                bind(Enc<LoginResponse.InvalidLoginServer>(LoginServerProt.INVALID_LOGINSERVER))
                bind(Enc<LoginResponse.HopBlocked>(LoginServerProt.HOP_BLOCKED))
                bind(Enc<LoginResponse.InvalidLoginPacket>(LoginServerProt.INVALID_LOGIN_PACKET))
                bind(Enc<LoginResponse.LoginServerNoReply>(LoginServerProt.LOGINSERVER_NO_REPLY))
                bind(Enc<LoginResponse.LoginServerLoadError>(LoginServerProt.LOGINSERVER_LOAD_ERROR))
                bind(Enc<LoginResponse.UnknownReplyFromLoginServer>(LoginServerProt.UNKNOWN_REPLY_FROM_LOGINSERVER))
                bind(Enc<LoginResponse.IPBlocked>(LoginServerProt.IP_BLOCKED))
                bind(Enc<LoginResponse.ServiceUnavailable>(LoginServerProt.SERVICE_UNAVAILABLE))
                bind(Enc<LoginResponse.DisplayNameRequired>(LoginServerProt.DISPLAYNAME_REQUIRED))
                bind(Enc<LoginResponse.NegativeCredit>(LoginServerProt.NEGATIVE_CREDIT))
                bind(Enc<LoginResponse.InvalidSingleSignOn>(LoginServerProt.INVALID_SINGLE_SIGNON))
                bind(Enc<LoginResponse.NoReplyFromSingleSignOn>(LoginServerProt.NO_REPLY_FROM_SINGLE_SIGNON))
                bind(Enc<LoginResponse.ProfileBeingEdited>(LoginServerProt.PROFILE_BEING_EDITED))
                bind(Enc<LoginResponse.NoBetaAccess>(LoginServerProt.NO_BETA_ACCESS))
                bind(Enc<LoginResponse.InstanceInvalid>(LoginServerProt.INSTANCE_INVALID))
                bind(Enc<LoginResponse.InstanceNotSpecified>(LoginServerProt.INSTANCE_NOT_SPECIFIED))
                bind(Enc<LoginResponse.InstanceFull>(LoginServerProt.INSTANCE_FULL))
                bind(Enc<LoginResponse.InQueue>(LoginServerProt.IN_QUEUE))
                bind(Enc<LoginResponse.AlreadyInQueue>(LoginServerProt.ALREADY_IN_QUEUE))
                bind(Enc<LoginResponse.BillingTimeout>(LoginServerProt.BILLING_TIMEOUT))
                bind(Enc<LoginResponse.NotAgreedToNda>(LoginServerProt.NOT_AGREED_TO_NDA))
                bind(Enc<LoginResponse.EmailNotValidated>(LoginServerProt.EMAIL_NOT_VALIDATED))
                bind(Enc<LoginResponse.ConnectFail>(LoginServerProt.CONNECT_FAIL))
                bind(Enc<LoginResponse.PrivacyPolicy>(LoginServerProt.PRIVACY_POLICY))
                bind(Enc<LoginResponse.Authenticator>(LoginServerProt.AUTHENTICATOR))
                bind(Enc<LoginResponse.InvalidAuthenticatorCode>(LoginServerProt.INVALID_AUTHENTICATOR_CODE))
                bind(Enc<LoginResponse.UpdateDob>(LoginServerProt.UPDATE_DOB))
                bind(Enc<LoginResponse.Timeout>(LoginServerProt.TIMEOUT))
                bind(Enc<LoginResponse.Kick>(LoginServerProt.KICK))
                bind(Enc<LoginResponse.Retry>(LoginServerProt.RETRY))
                bind(Enc<LoginResponse.LoginFail1>(LoginServerProt.LOGIN_FAIL_1))
                bind(Enc<LoginResponse.LoginFail2>(LoginServerProt.LOGIN_FAIL_2))
                bind(Enc<LoginResponse.OutOfDateReload>(LoginServerProt.OUT_OF_DATE_RELOAD))
                bind(Enc<LoginResponse.ProofOfWork>(LoginServerProt.PROOF_OF_WORK))
                bind(Enc<LoginResponse.DobError>(LoginServerProt.DOB_ERROR))
                bind(Enc<LoginResponse.DobReview>(LoginServerProt.DOB_REVIEW))
                bind(Enc<LoginResponse.ClosedBeta>(LoginServerProt.CLOSED_BETA))
            }
        return builder.build()
    }
}
