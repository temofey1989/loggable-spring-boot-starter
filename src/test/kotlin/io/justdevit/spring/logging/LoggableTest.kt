@file:Suppress("unused")

package io.justdevit.spring.logging

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.stereotype.Component

@SpringBootTest(
    classes = [LoggableTest.TestService::class, LoggableAutoConfiguration::class],
    properties = [
        "spring.application.name = application",
        "logging.action-log-format = logstash-json",
        "logging.level.root = INFO",
    ]
)
internal class LoggableTest(@Autowired val testService: TestService) {

    @Component
    @Loggable("TEST_SERVICE")
    open class TestService {

        open fun test(input: String): String = response()

        @Loggable
        open fun response() = "Hello World!"

        @Loggable
        open fun fail(): String = throw RuntimeException("TEST")
    }

    @Test
    fun `Should log function with parameters`() {
        val result = testService.test("TEST")

        assertThat(result).isEqualTo("Hello World!")
    }

    @Test
    fun `Should log by throws`() {
        assertThrows<RuntimeException> { testService.fail() }
    }
}
