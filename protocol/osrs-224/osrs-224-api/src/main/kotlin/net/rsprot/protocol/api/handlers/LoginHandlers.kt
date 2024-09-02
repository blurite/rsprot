package net.rsprot.protocol.api.handlers

import net.rsprot.protocol.api.LoginDecoderService
import net.rsprot.protocol.api.SessionIdGenerator
import net.rsprot.protocol.api.StreamCipherProvider
import net.rsprot.protocol.api.implementation.DefaultLoginDecoderService
import net.rsprot.protocol.api.implementation.DefaultSessionIdGenerator
import net.rsprot.protocol.api.implementation.DefaultStreamCipherProvider
import net.rsprot.protocol.loginprot.incoming.pow.ProofOfWorkProvider
import net.rsprot.protocol.loginprot.incoming.pow.challenges.ChallengeWorker
import net.rsprot.protocol.loginprot.incoming.pow.challenges.DefaultChallengeWorker
import net.rsprot.protocol.loginprot.incoming.pow.challenges.sha256.DefaultSha256ProofOfWorkProvider

/**
 * The handlers for anything to do with the login procedure.
 * @property sessionIdGenerator the generator for session ids which are initially made
 * at the very beginning when the client establishes a connection. This session id is
 * furthermore passed whenever a login occurs and validated by the library to ensure it matches.
 * @property streamCipherProvider the provider for game stream ciphers, by default, the stream
 * cipher uses the normal OldSchool client implementation.
 * @property loginDecoderService the decoder service responsible for decoding login blocks,
 * as the RSA deciphering is fairly expensive, allowing this to be done on a different thread.
 * @property proofOfWorkProvider the provider for proof of work which must be completed
 * before a login can take place. If the provider returns null, no proof of work is used.
 * @property proofOfWorkChallengeWorker the worker used to verify the validity of the challenge,
 * allowing servers to execute this off of another thread. By default, this will be
 * executed via the calling thread, as this is extremely fast to check.
 * @property suppressInvalidLoginProts whether to suppress and kill the channel whenever an invalid
 * login prot is received. This can be useful if the server is susceptible to web crawlers and
 * anything of such nature which could lead into a lot of useless errors being thrown.
 * By default, this is off, and errors will be thrown whenever an invalid prot is received.
 */
public class LoginHandlers
    @JvmOverloads
    public constructor(
        public val sessionIdGenerator: SessionIdGenerator = DefaultSessionIdGenerator(),
        public val streamCipherProvider: StreamCipherProvider = DefaultStreamCipherProvider(),
        public val loginDecoderService: LoginDecoderService = DefaultLoginDecoderService(),
        public val proofOfWorkProvider: ProofOfWorkProvider<*, *> = DefaultSha256ProofOfWorkProvider(1),
        public val proofOfWorkChallengeWorker: ChallengeWorker = DefaultChallengeWorker,
        public val suppressInvalidLoginProts: Boolean = false,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as LoginHandlers

            if (sessionIdGenerator != other.sessionIdGenerator) return false
            if (streamCipherProvider != other.streamCipherProvider) return false
            if (loginDecoderService != other.loginDecoderService) return false
            if (proofOfWorkProvider != other.proofOfWorkProvider) return false
            if (proofOfWorkChallengeWorker != other.proofOfWorkChallengeWorker) return false
            if (suppressInvalidLoginProts != other.suppressInvalidLoginProts) return false

            return true
        }

        override fun hashCode(): Int {
            var result = sessionIdGenerator.hashCode()
            result = 31 * result + streamCipherProvider.hashCode()
            result = 31 * result + loginDecoderService.hashCode()
            result = 31 * result + proofOfWorkProvider.hashCode()
            result = 31 * result + proofOfWorkChallengeWorker.hashCode()
            result = 31 * result + suppressInvalidLoginProts.hashCode()
            return result
        }

        override fun toString(): String =
            "LoginHandlers(" +
                "sessionIdGenerator=$sessionIdGenerator, " +
                "streamCipherProvider=$streamCipherProvider, " +
                "loginDecoderService=$loginDecoderService, " +
                "proofOfWorkProvider=$proofOfWorkProvider, " +
                "proofOfWorkChallengeWorker=$proofOfWorkChallengeWorker, " +
                "suppressInvalidLoginProts=$suppressInvalidLoginProts" +
                ")"
    }
