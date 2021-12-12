@file:Suppress("UNUSED_PARAMETER", "unused")

package io.justdevit.spring.logging.extension

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class MethodExtensionTest {

    @Nested
    inner class HasParametersTests {

        @Test
        fun `Should return false on non parametrized method`() {
            class TestClass {
                fun test() = Unit
            }

            val method = TestClass::class.java.getMethod("test")

            assertThat(method.hasParameters).isFalse
        }

        @Test
        fun `Should return true for parametrized method`() {
            class TestClass {
                fun test(p0: String) = Unit
            }

            val method = TestClass::class.java.getMethod("test", String::class.java)

            assertThat(method.hasParameters).isTrue
        }
    }

    @Nested
    inner class ReturnsUnitTests {

        @Test
        fun `Should return false for non unit method`() {
            class TestClass {
                fun test(): String = "TEST"
            }

            val method = TestClass::class.java.getMethod("test")

            assertThat(method.returnsUnit).isFalse
        }

        @Test
        fun `Should return true for unit method`() {
            class TestClass {
                fun test() = Unit
            }

            val method = TestClass::class.java.getMethod("test")

            assertThat(method.returnsUnit).isTrue
        }
    }
}
