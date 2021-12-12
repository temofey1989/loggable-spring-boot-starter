@file:Suppress("unused", "UNUSED_PARAMETER")

package io.justdevit.spring.logging

import io.justdevit.spring.logging.action.ActionNameResolver
import io.justdevit.spring.logging.action.ActionNameResolverProvider
import io.justdevit.spring.logging.writer.LogWriter
import io.justdevit.spring.logging.writer.LogWriterProvider
import io.justdevit.spring.logging.writer.OnFinishLogContext
import io.justdevit.spring.logging.writer.OnStartLogContext
import io.justdevit.spring.logging.writer.OnThrowLogContext
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.slot
import io.mockk.verify
import org.aopalliance.intercept.MethodInvocation
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.MDC
import java.lang.reflect.Method

@ExtendWith(MockKExtension::class)
internal class DefaultLoggableMethodInterceptorTest {

    private val processingReturnValue = "PROCESSED"

    @MockK
    lateinit var invocation: MethodInvocation

    @MockK
    lateinit var actionNameResolverProvider: ActionNameResolverProvider

    @MockK
    lateinit var actionNameResolver: ActionNameResolver

    @MockK
    lateinit var logWriterProvider: LogWriterProvider

    @MockK
    lateinit var logWriter: LogWriter

    @InjectMockKs
    lateinit var interceptor: DefaultLoggableMethodInterceptor

    @BeforeEach
    fun setUp() {
        every { invocation.arguments } returns emptyArray()
        every { invocation.proceed() } returns processingReturnValue
        every { actionNameResolverProvider.findActionNameResolver(any()) } returns actionNameResolver
        every { actionNameResolver.solveActionName(any()) } answers { (invocation.args[0] as Method).name }
        every { logWriterProvider.findLogWriter(any()) } returns logWriter
        justRun { logWriter.onStart(any()) }
        justRun { logWriter.onFinish(any()) }
        justRun { logWriter.onThrow(any()) }
    }

    @Test
    fun `Should be able to find annotation on method`() {
        class TestClass {
            @Loggable
            fun annotatedFunction() = Unit
        }
        every { invocation.method } returns TestClass::class.java.getMethod("annotatedFunction")

        val result = interceptor.invoke(invocation)

        assertThat(result).isEqualTo(processingReturnValue)
        val onStartSlot = slot<OnStartLogContext>()
        verify(exactly = 1) { logWriter.onStart(capture(onStartSlot)) }
        assertThat(onStartSlot.captured.action).isEqualTo("annotatedFunction")
        val onFinishSlot = slot<OnFinishLogContext>()
        verify(exactly = 1) { logWriter.onFinish(capture(onFinishSlot)) }
        assertThat(onFinishSlot.captured.action).isEqualTo("annotatedFunction")
    }

    @Test
    fun `Should be able to find annotation on class`() {
        @Loggable
        class TestClass {
            fun notAnnotatedFunction() = Unit
        }
        every { invocation.method } returns TestClass::class.java.getMethod("notAnnotatedFunction")

        val result = interceptor.invoke(invocation)

        assertThat(result).isEqualTo(processingReturnValue)
        val onStartSlot = slot<OnStartLogContext>()
        verify(exactly = 1) { logWriter.onStart(capture(onStartSlot)) }
        assertThat(onStartSlot.captured.action).isEqualTo("notAnnotatedFunction")
        val onFinishSlot = slot<OnFinishLogContext>()
        verify(exactly = 1) { logWriter.onFinish(capture(onFinishSlot)) }
        assertThat(onFinishSlot.captured.action).isEqualTo("notAnnotatedFunction")
    }

    @Test
    fun `Should be able to inject parameters to writer`() {
        @Loggable(ignoreAllParameters = false)
        class TestClass {
            fun test(p: String) = Unit
        }
        every { invocation.method } returns TestClass::class.java.getMethod("test", String::class.java)
        every { invocation.arguments } returns arrayOf("P")

        val result = interceptor.invoke(invocation)

        assertThat(result).isEqualTo(processingReturnValue)
        val onStartSlot = slot<OnStartLogContext>()
        verify(exactly = 1) { logWriter.onStart(capture(onStartSlot)) }
        assertThat(onStartSlot.captured.parameters).containsExactly("P")
    }

    @Test
    fun `Should be able to ignore all parameters`() {
        @Loggable(ignoreAllParameters = true)
        class TestClass {
            fun test(p: String) = Unit
        }
        every { invocation.method } returns TestClass::class.java.getMethod("test", String::class.java)
        every { invocation.arguments } returns arrayOf("P")

        val result = interceptor.invoke(invocation)

        assertThat(result).isEqualTo(processingReturnValue)
        val onStartSlot = slot<OnStartLogContext>()
        verify(exactly = 1) { logWriter.onStart(capture(onStartSlot)) }
        assertThat(onStartSlot.captured.parameters).isEmpty()
    }

    @Test
    fun `Should be able to inject return value to writer`() {
        @Loggable(ignoreReturnValue = false)
        class TestClass {
            fun test() = processingReturnValue
        }
        every { invocation.method } returns TestClass::class.java.getMethod("test")
        every { invocation.arguments } returns arrayOf("P")

        val result = interceptor.invoke(invocation)

        assertThat(result).isEqualTo(processingReturnValue)
        val onFinishSlot = slot<OnFinishLogContext>()
        verify(exactly = 1) { logWriter.onFinish(capture(onFinishSlot)) }
        assertThat(onFinishSlot.captured.returnValue).isEqualTo(processingReturnValue)
    }

    @Test
    fun `Should be able to ignore return value`() {
        @Loggable(ignoreReturnValue = true)
        class TestClass {
            fun test() = processingReturnValue
        }
        every { invocation.method } returns TestClass::class.java.getMethod("test")
        every { invocation.arguments } returns arrayOf("P")

        val result = interceptor.invoke(invocation)

        assertThat(result).isEqualTo(processingReturnValue)
        val onFinishSlot = slot<OnFinishLogContext>()
        verify(exactly = 1) { logWriter.onFinish(capture(onFinishSlot)) }
        assertThat(onFinishSlot.captured.returnValue).isNull()
    }

    @Test
    fun `Should be able to process thrown exception`() {
        @Loggable(ignoreThrows = false)
        class TestClass {
            fun test() = Unit
        }
        every { invocation.method } returns TestClass::class.java.getMethod("test")
        every { invocation.arguments } returns emptyArray()
        every { invocation.proceed() } throws IllegalStateException()

        assertThrows<IllegalStateException> { interceptor.invoke(invocation) }

        val onStartSlot = slot<OnStartLogContext>()
        verify(exactly = 1) { logWriter.onStart(capture(onStartSlot)) }
        assertThat(onStartSlot.captured.action).isEqualTo("test")
        val onFailSlot = slot<OnThrowLogContext>()
        verify(exactly = 1) { logWriter.onThrow(capture(onFailSlot)) }
        assertThat(onFailSlot.captured.action).isEqualTo("test")
        assertThat(onFailSlot.captured.exception).isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `Should be able to ignore thrown exception`() {
        @Loggable(ignoreThrows = true)
        class TestClass {
            fun test() = Unit
        }
        every { invocation.method } returns TestClass::class.java.getMethod("test")
        every { invocation.arguments } returns emptyArray()
        every { invocation.proceed() } throws IllegalStateException()

        assertThrows<IllegalStateException> { interceptor.invoke(invocation) }

        val onStartSlot = slot<OnStartLogContext>()
        verify(exactly = 1) { logWriter.onStart(capture(onStartSlot)) }
        assertThat(onStartSlot.captured.action).isEqualTo("test")
        verify(exactly = 0) { logWriter.onThrow(any()) }
    }

    @Test
    fun `Should be able to set MDC action attribute`() {
        class TestClass {
            @Loggable("TEST")
            fun test() = Unit
        }
        MDC.remove(ACTION_ATTRIBUTE_NAME)
        var action: String? = null
        every { invocation.method } returns TestClass::class.java.getMethod("test")
        every { invocation.proceed() } answers {
            action = MDC.get(ACTION_ATTRIBUTE_NAME)
            processingReturnValue
        }

        interceptor.invoke(invocation)

        assertThat(action).isEqualTo("TEST")
        assertThat(MDC.get(ACTION_ATTRIBUTE_NAME)).isNull()
    }

    @Test
    fun `Should be able to reset MDC action attribute`() {
        class TestClass {
            @Loggable(action = "TEST")
            fun test() = Unit
        }
        MDC.put(ACTION_ATTRIBUTE_NAME, "PREV_ACTION")
        var action: String? = null
        every { invocation.method } returns TestClass::class.java.getMethod("test")
        every { invocation.proceed() } answers {
            action = MDC.get(ACTION_ATTRIBUTE_NAME)
            processingReturnValue
        }

        interceptor.invoke(invocation)

        assertThat(action).isEqualTo("TEST")
        assertThat(MDC.get(ACTION_ATTRIBUTE_NAME)).isEqualTo("PREV_ACTION")
    }

    @Test
    fun `Should be able to ignore logging of nested action`() {
        class TestClass {
            @Loggable(action = "TEST", nestable = true)
            fun test() = Unit
        }
        MDC.put(ACTION_ATTRIBUTE_NAME, "PREV_ACTION")
        var action: String? = null
        every { invocation.method } returns TestClass::class.java.getMethod("test")
        every { invocation.proceed() } answers {
            action = MDC.get(ACTION_ATTRIBUTE_NAME)
            processingReturnValue
        }

        interceptor.invoke(invocation)

        assertThat(action).isEqualTo("PREV_ACTION")
        assertThat(MDC.get(ACTION_ATTRIBUTE_NAME)).isEqualTo("PREV_ACTION")
    }
}
