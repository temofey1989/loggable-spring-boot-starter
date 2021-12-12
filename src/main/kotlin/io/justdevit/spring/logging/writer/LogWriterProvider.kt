package io.justdevit.spring.logging.writer

import java.lang.reflect.Method

/**
 * Represents log writer provider based on supported method.
 *
 * @see DefaultLogWriterProvider
 */
interface LogWriterProvider {

    /**
     * Finds log writer based on method.
     *
     * @param method Method for finding log writer.
     * @return Log writer based on method or null in case of no such log writer.
     */
    fun findLogWriter(method: Method): LogWriter?
}
