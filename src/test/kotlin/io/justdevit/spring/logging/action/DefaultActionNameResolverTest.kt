package io.justdevit.spring.logging.action

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class DefaultActionNameResolverTest {

    class TestClass {
        fun test() = Unit
    }

    private val resolver = DefaultActionNameResolver()

    @Test
    fun `Should support every method`() {
        val result = resolver.supports(TestClass::class.java.getMethod("test"))

        assertThat(result).isTrue
    }

    @Test
    fun `Should return action name based on class name and method name`() {
        val result = resolver.solveActionName(TestClass::class.java.getMethod("test"))

        assertThat(result).isEqualTo("TestClass::test")
    }
}
