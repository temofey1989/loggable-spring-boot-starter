package io.justdevit.spring.logging

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.of
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.aop.SpringProxy
import java.util.stream.Stream

internal class LoggableBeanPostProcessorTest {

    @Loggable
    open class AnnotatedOnClassWithPublicMethod {
        open fun test() = Unit
    }

    open class ClassWithAnnotatedPublicMethod {
        @Loggable
        open fun test() = Unit
    }

    open class NonAnnotatedClass {
        open fun test() = Unit
    }

    class FinalClass {
        @Loggable
        fun test() = Unit
    }

    @Loggable
    open class AnnotatedOnClassWithoutPublicMethod {
        fun test() = Unit
    }

    open class AnnotatedOnFinalMethod {
        @Loggable
        fun test() = Unit
    }

    private val processor = LoggableBeanPostProcessor(mockk())

    companion object {
        @JvmStatic
        fun loggableBeans(): Stream<Arguments> = Stream.of(
            of(AnnotatedOnClassWithPublicMethod()),
            of(ClassWithAnnotatedPublicMethod()),
        )

        @JvmStatic
        fun nonLoggableBeans(): Stream<Arguments> = Stream.of(
            of(NonAnnotatedClass()),
            of(FinalClass()),
            of(AnnotatedOnClassWithoutPublicMethod()),
            of(AnnotatedOnFinalMethod()),
        )
    }

    @ParameterizedTest
    @MethodSource("loggableBeans")
    fun `Should be able to create proxy`(bean: Any) {
        val result = processor.postProcessBeforeInitialization(bean, "bean")

        assertThat(result)
            .isInstanceOf(SpringProxy::class.java)
    }

    @ParameterizedTest
    @MethodSource("nonLoggableBeans")
    fun `Should be able to ignore proxying`(bean: Any) {
        val result = processor.postProcessBeforeInitialization(bean, "bean")

        assertThat(result)
            .isNotInstanceOf(SpringProxy::class.java)
            .isEqualTo(bean)
    }
}
