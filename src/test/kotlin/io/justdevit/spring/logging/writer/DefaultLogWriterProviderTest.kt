package io.justdevit.spring.logging.writer

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.lang.reflect.Method

internal class DefaultLogWriterProviderTest {

    class TestClass {
        fun supported() = Unit

        fun unsupported() = Unit
    }

    private val testLogWriter: LogWriter = object : LogWriter {
        override fun supports(method: Method) = method.name == "supported"
        override fun onStart(context: OnStartLogContext) = Unit
        override fun onFinish(context: OnFinishLogContext) = Unit
        override fun onThrow(context: OnThrowLogContext) = Unit

    }

    private val provider = DefaultLogWriterProvider(listOf(testLogWriter))

    @Test
    fun `Should be able find test provider`() {
        val result = provider.findLogWriter(TestClass::class.java.getMethod("supported"))

        assertThat(result).isEqualTo(testLogWriter)
    }

    @Test
    fun `Should be able return fallback provider`() {
        val result = provider.findLogWriter(TestClass::class.java.getMethod("unsupported"))

        assertThat(result).isNull()
    }
}
