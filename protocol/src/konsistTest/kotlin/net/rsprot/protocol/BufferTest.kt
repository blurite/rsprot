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

class BufferTest {
    @TestFactory
    fun `buffer matches size`() =
        Konsist
            .scopeFromProject()
            .classes()
            .withName(ENUM_NAME)
            .map { it.moduleName }
            .map { module ->
                DynamicContainer.dynamicContainer(
                    module.substringAfterLast("/"),
                    createDynamicTestsForModule(module),
                )
            }

    private fun createDynamicTestsForModule(module: String): List<DynamicTest> {
        val scope = Konsist.scopeFromModule(module)
        val encoders =
            scope
                .classes()
                .withParentInterfaceNamed(ENCODER_INTERFACE)
                .withoutAnnotationNamed(IGNORED_CLASS_ANNOTATION)

        return encoders.map { encoder ->
            DynamicTest.dynamicTest(encoder.name) { encoder.validateEncoder(scope) }
        }
    }

    private fun KoClassDeclaration.validateEncoder(scope: KoScope) {
        val prot = properties().withTypeOf(ServerProt::class).first()
        val declaredSize = fetchDeclaredEnumSize(prot, scope)
        if (declaredSize.startsWith(DYNAMIC_SIZE_PREFIX)) {
            // VAR_* are dynamic
            return
        }
        val expectedSize = declaredSize.toInt()
        val encodeMethod = functions().withName(ENCODE_METHOD).first()
        val actualSize = encodeMethod.extractSize()
        assert(
            expectedSize == actualSize,
            lazyMessage = { "Expected size of $expectedSize, actual $actualSize" },
        )
    }

    private fun fetchDeclaredEnumSize(
        prot: KoPropertyDeclaration,
        scope: KoScope,
    ): String {
        val enumConstantName = prot.value!!.substringAfter("$ENUM_NAME.")
        val enumConstant = scope.classes().enumConstants.withName(enumConstantName).first()
        val declaredSize = enumConstant.arguments[1].value!!
        return declaredSize
    }

    private fun KoFunctionDeclaration.extractSize(): Int {
        return bufferRegex.findAll(text).sumOf {
            when (val size = it.groups["size"]?.value) {
                "CombinedId" -> COMBINED_ID_SIZE
                else -> size?.toIntOrNull() ?: 0
            }
        }
    }

    companion object {
        private val bufferRegex = "buffer\\.p(?<size>(\\d|CombinedId)+)(Alt\\d+)?".toRegex()
        private const val ENUM_NAME = "GameServerProt"
        private const val DYNAMIC_SIZE_PREFIX = "Prot.VAR_"
        private const val ENCODE_METHOD = "encode"
        private const val ENCODER_INTERFACE = "MessageEncoder"
        private const val IGNORED_CLASS_ANNOTATION = "Consistent"
        private const val COMBINED_ID_SIZE = 4
    }
}
