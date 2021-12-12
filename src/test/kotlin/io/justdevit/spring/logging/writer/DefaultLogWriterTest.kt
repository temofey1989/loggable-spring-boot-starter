@file:Suppress("UNUSED_PARAMETER", "unused")

package io.justdevit.spring.logging.writer

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import io.justdevit.spring.logging.Sensitive
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import org.slf4j.event.Level.ERROR
import java.util.function.Consumer

@ExtendWith(MockKExtension::class)
internal class DefaultLogWriterTest {

    class TestAppender : AppenderBase<ILoggingEvent>() {
        val events = mutableListOf<ILoggingEvent>()

        override fun append(event: ILoggingEvent) {
            events += event
        }
    }

    class TestClass {
        fun onStart(p0: String, @Sensitive p1: String) = Unit

        fun onFinishUnit() = Unit

        fun onFinishString(): String = "2"
    }

    private val testAppender = TestAppender()
    private val onStartMethod = TestClass::class.java.getMethod("onStart", String::class.java, String::class.java)
    private val onFinishUnitMethod = TestClass::class.java.getMethod("onFinishUnit")
    private val onFinishStringMethod = TestClass::class.java.getMethod("onFinishString")

    @MockK
    lateinit var actionLogResolver: ActionLogResolver

    @InjectMockKs
    lateinit var writer: DefaultLogWriter

    @BeforeEach
    fun setUp() {
        (LoggerFactory.getLogger(TestClass::class.java) as Logger).also {
            it.addAppender(testAppender)
            it.level = ch.qos.logback.classic.Level.TRACE
        }
        testAppender.start()
    }

    @Test
    fun `Should support all methods`() {
        val result = writer.supports(onStartMethod)

        assertThat(result).isTrue
    }

    @Nested
    inner class OnStartTests {

        @ParameterizedTest
        @EnumSource(Level::class)
        fun `Should log on start`(level: Level) {
            val context = OnStartLogContext(
                action = "TEST",
                method = onStartMethod,
                level = level,
                parameters = listOf("V0", "V1")
            )
            every { actionLogResolver.onStart(context) } returns ActionLog("STARTED: TEST", listOf("V1"))

            writer.onStart(context)

            assertThat(testAppender.events).singleElement().satisfies(Consumer {
                assertThat(it.level.toString()).isEqualTo(level.name)
                assertThat(it.message).isEqualTo("STARTED: TEST")
                assertThat(it.argumentArray).hasSize(1)
                assertThat(it.argumentArray[0]).isEqualTo("V1")
            })
        }
    }

    @Nested
    inner class OnFinishTests {

        @ParameterizedTest
        @EnumSource(Level::class)
        fun `Should be able to log on finish of Unit function`(level: Level) {
            val context = OnFinishLogContext(
                action = "TEST",
                method = onFinishUnitMethod,
                level = level
            )
            every { actionLogResolver.onFinish(context) } returns ActionLog("FINISHED: TEST")

            writer.onFinish(context)

            assertThat(testAppender.events).singleElement().satisfies(Consumer {
                assertThat(it.level.toString()).isEqualTo(level.name)
                assertThat(it.message).isEqualTo("FINISHED: TEST")
                assertThat(it.argumentArray).isEmpty()
            })
        }

        @ParameterizedTest
        @EnumSource(Level::class)
        fun `Should be able to log on finish of returning function`(level: Level) {
            val context = OnFinishLogContext(
                action = "TEST",
                method = onFinishStringMethod,
                level = level,
                returnValue = "123"
            )
            every { actionLogResolver.onFinish(context) } returns ActionLog("FINISHED: TEST", listOf(context.returnValue))

            writer.onFinish(context)

            assertThat(testAppender.events).singleElement().satisfies(Consumer {
                assertThat(it.level.toString()).isEqualTo(level.name)
                assertThat(it.message).isEqualTo("FINISHED: TEST")
                assertThat(it.argumentArray).hasSize(1)
                assertThat(it.argumentArray[0]).isEqualTo(context.returnValue)
            })
        }
    }

    @Nested
    inner class OnThrowTests {

        @Test
        fun `Should be able to log on throw`() {
            val exception = IllegalArgumentException("MESSAGE")
            val context = OnThrowLogContext(
                action = "TEST",
                method = onStartMethod,
                exception = exception
            )
            every { actionLogResolver.onThrow(context) } returns ActionLog("THROWN: TEST", listOf(exception))

            writer.onThrow(context)

            assertThat(testAppender.events).singleElement().satisfies(Consumer {
                assertThat(it.level.toString()).isEqualTo(ERROR.name)
                assertThat(it.message).isEqualTo("THROWN: TEST")
                assertThat(it.throwableProxy.message).isEqualTo(exception.message)
            })
        }
    }

}
