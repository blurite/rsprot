package net.rsprot.protocol.game.outgoing.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.client.ReflectionChecker
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class ReflectionCheckerEncoder : MessageEncoder<ReflectionChecker> {
    override val prot: ServerProt = GameServerProt.REFLECTION_CHECKER

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: ReflectionChecker,
    ) {
        val checks = message.checks
        buffer.p1(checks.size)
        buffer.p4(message.id)
        for (check in checks) {
            when (check) {
                is ReflectionChecker.GetFieldValue -> {
                    buffer.p1(0)
                    buffer.pjstr(check.className)
                    buffer.pjstr(check.fieldName)
                }
                is ReflectionChecker.SetFieldValue -> {
                    buffer.p1(1)
                    buffer.pjstr(check.className)
                    buffer.pjstr(check.fieldName)
                    buffer.p4(check.value)
                }
                is ReflectionChecker.GetFieldModifiers -> {
                    buffer.p1(2)
                    buffer.pjstr(check.className)
                    buffer.pjstr(check.fieldName)
                }
                is ReflectionChecker.InvokeMethod -> {
                    buffer.p1(3)
                    buffer.pjstr(check.className)
                    buffer.pjstr(check.methodName)
                    val parameterClasses = check.parameterClasses
                    val parameterValues = check.parameterValues
                    buffer.p1(parameterClasses.size)
                    for (parameterClass in parameterClasses) {
                        buffer.pjstr(parameterClass)
                    }
                    buffer.pjstr(check.returnClass)
                    for (parameterValue in parameterValues) {
                        buffer.p4(parameterValue.size)
                        buffer.pdata(parameterValue)
                    }
                }
                is ReflectionChecker.GetMethodModifiers -> {
                    buffer.p1(4)
                    buffer.pjstr(check.className)
                    buffer.pjstr(check.methodName)
                    val parameterClasses = check.parameterClasses
                    buffer.p1(parameterClasses.size)
                    for (parameterClass in parameterClasses) {
                        buffer.pjstr(parameterClass)
                    }
                    buffer.pjstr(check.returnClass)
                }
            }
        }
    }
}
