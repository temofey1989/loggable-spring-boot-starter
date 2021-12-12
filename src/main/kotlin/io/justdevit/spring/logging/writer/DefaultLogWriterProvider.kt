package io.justdevit.spring.logging.writer

import java.lang.reflect.Method

/**
 * Providers supported log writer.
 *
 * @param logWriters List of registered log writers.
 */
class DefaultLogWriterProvider(private val logWriters: List<LogWriter>) : LogWriterProvider {

    override fun findLogWriter(method: Method) = logWriters.find { it.supports(method) }
}
