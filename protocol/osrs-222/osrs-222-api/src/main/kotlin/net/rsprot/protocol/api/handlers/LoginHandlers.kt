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
 */
public class LoginHandlers
    @JvmOverloads
    public constructor(
        public val sessionIdGenerator: SessionIdGenerator = DefaultSessionIdGenerator(),
        public val streamCipherProvider: StreamCipherProvider = DefaultStreamCipherProvider(),
        public val loginDecoderService: LoginDecoderService = DefaultLoginDecoderService(),
        public val proofOfWorkProvider: ProofOfWorkProvider<*, *> = DefaultSha256ProofOfWorkProvider(1),
        public val proofOfWorkChallengeWorker: ChallengeWorker = DefaultChallengeWorker,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as LoginHandlers

            if (proofOfWorkChallengeWorker != other.proofOfWorkChallengeWorker) return false
            if (proofOfWorkProvider != other.proofOfWorkProvider) return false
            if (sessionIdGenerator != other.sessionIdGenerator) return false
            if (streamCipherProvider != other.streamCipherProvider) return false
            if (loginDecoderService != other.loginDecoderService) return false

            return true
        }

        override fun hashCode(): Int {
            var result = proofOfWorkChallengeWorker.hashCode()
            result = 31 * result + proofOfWorkProvider.hashCode()
            result = 31 * result + sessionIdGenerator.hashCode()
            result = 31 * result + streamCipherProvider.hashCode()
            result = 31 * result + loginDecoderService.hashCode()
            return result
        }

        override fun toString(): String {
            return "LoginHandlers(" +
                "sessionIdGenerator=$sessionIdGenerator, " +
                "streamCipherProvider=$streamCipherProvider, " +
                "loginDecoderService=$loginDecoderService, " +
                "proofOfWorkProvider=$proofOfWorkProvider, " +
                "proofOfWorkChallengeWorker=$proofOfWorkChallengeWorker" +
                ")"
        }
    }
