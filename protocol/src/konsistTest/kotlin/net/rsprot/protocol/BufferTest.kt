package net.rsprot.protocol

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.container.KoScope
import com.lemonappdev.konsist.api.declaration.KoClassDeclaration
import com.lemonappdev.konsist.api.declaration.KoFunctionDeclaration
import com.lemonappdev.konsist.api.declaration.KoPropertyDeclaration
import com.lemonappdev.konsist.api.ext.list.enumConstants
import com.lemonappdev.konsist.api.ext.list.withName
import com.lemonappdev.konsist.api.ext.list.withParentInterfaceNamed
import com.lemonappdev.konsist.api.ext.list.withTypeOf
import com.lemonappdev.konsist.api.ext.list.withoutAnnotationNamed
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.reflect.KClass

class BufferTest {
    @TestFactory
    fun `p buffer matches size`() =
        Konsist
            .scopeFromProject()
            .classes()
            .withName(SERVER_ENUM_NAME)
            .map { it.moduleName }
            .map { module ->
                DynamicContainer.dynamicContainer(
                    module.substringAfterLast("/"),
                    createDynamicTestsForModule(
                        module,
                        ENCODER_INTERFACE,
                        ENCODE_METHOD,
                        SERVER_ENUM_NAME,
                        pBufferRegex,
                        ServerProt::class,
                    ),
                )
            }

    @TestFactory
    fun `g buffer matches size`() =
        Konsist
            .scopeFromProject()
            .classes()
            .withName(CLIENT_ENUM_NAME)
            .map { it.moduleName }
            .map { module ->
                DynamicContainer.dynamicContainer(
                    module.substringAfterLast("/"),
                    createDynamicTestsForModule(
                        module,
                        DECODER_INTERFACE,
                        DECODE_METHOD,
                        CLIENT_ENUM_NAME,
                        gBufferRegex,
                        ClientProt::class,
                    ),
                )
            }

    private fun createDynamicTestsForModule(
        module: String,
        interfaceName: String,
        methodName: String,
        enumName: String,
        regex: Regex,
        kClass: KClass<*>,
    ): List<DynamicTest> {
        val scope = Konsist.scopeFromModule(module)
        val encoders =
            scope
                .classes()
                .withParentInterfaceNamed(interfaceName)
                .withoutAnnotationNamed(IGNORED_CLASS_ANNOTATION)

        return encoders.map { encoder ->
            DynamicTest.dynamicTest(encoder.name) {
                encoder.validateEncoder(
                    scope,
                    methodName,
                    enumName,
                    regex,
                    kClass,
                )
            }
        }
    }

    private fun KoClassDeclaration.validateEncoder(
        scope: KoScope,
        methodName: String,
        enumName: String,
        regex: Regex,
        kClass: KClass<*>,
    ) {
        val prot = properties().withTypeOf(kClass).first()
        val declaredSize = fetchDeclaredEnumSize(prot, scope, enumName)
        if (declaredSize.startsWith(DYNAMIC_SIZE_PREFIX)) {
            // VAR_* are dynamic
            return
        }
        val expectedSize = declaredSize.toInt()
        val encodeMethod = functions().withName(methodName).first()
        val actualSize = encodeMethod.extractSize(regex)
        assert(
            expectedSize == actualSize,
            lazyMessage = { "Expected size of $expectedSize, actual $actualSize" },
        )
    }

    private fun fetchDeclaredEnumSize(
        prot: KoPropertyDeclaration,
        scope: KoScope,
        enumName: String,
    ): String {
        val enumConstantName = prot.value!!.substringAfter("$enumName.")
        val enumConstant =
            scope
                .classes()
                .enumConstants
                .withName(enumConstantName)
                .first()
        return enumConstant.arguments[1].value!!
    }

    private fun KoFunctionDeclaration.extractSize(regex: Regex): Int =
        regex.findAll(text).sumOf {
            when (val size = it.groups["size"]?.value) {
                "CombinedId" -> COMBINED_ID_SIZE
                else -> size?.toIntOrNull() ?: 0
            }
        }

    companion object {
        private val pBufferRegex = "buffer\\.p(?<size>(\\d|CombinedId)+)(Alt\\d+)?".toRegex()
        private val gBufferRegex = "buffer\\.g(?<size>(\\d|CombinedId)+)(Alt\\d+)?".toRegex()
        private const val SERVER_ENUM_NAME = "GameServerProt"
        private const val CLIENT_ENUM_NAME = "GameClientProt"
        private const val DYNAMIC_SIZE_PREFIX = "Prot.VAR_"
        private const val ENCODE_METHOD = "encode"
        private const val DECODE_METHOD = "decode"
        private const val ENCODER_INTERFACE = "MessageEncoder"
        private const val DECODER_INTERFACE = "MessageDecoder"
        private const val IGNORED_CLASS_ANNOTATION = "Consistent"
        private const val COMBINED_ID_SIZE = 4
    }
}
