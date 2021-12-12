package io.justdevit.spring.logging.action

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.lang.reflect.Method

internal class DefaultActionNameResolverProviderTest {

    class TestClass {
        fun supported() = Unit

        fun unsupported() = Unit
    }

    private val testResolver: ActionNameResolver = object : ActionNameResolver {
        override fun supports(method: Method) = method.name == "supported"

        override fun solveActionName(method: Method) = method.name
    }

    private val provider = DefaultActionNameResolverProvider(listOf(testResolver))

    @Test
    fun `Should be able find test provider`() {
        val result = provider.findActionNameResolver(TestClass::class.java.getMethod("supported"))

        assertThat(result).isEqualTo(testResolver)
    }

    @Test
    fun `Should be able return fallback provider`() {
        val result = provider.findActionNameResolver(TestClass::class.java.getMethod("unsupported"))

        assertThat(result).isInstanceOf(DefaultActionNameResolver::class.java)
    }
}
