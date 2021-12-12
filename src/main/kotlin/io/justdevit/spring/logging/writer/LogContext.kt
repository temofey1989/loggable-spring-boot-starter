package io.justdevit.spring.logging.writer

import org.slf4j.event.Level
import java.lang.reflect.Method

/**
 * Represents logging context.
 */
sealed class LogContext {

    /**
     * Executed method.
     */
    abstract val method: Method

    /**
     * Action name.
     */
    abstract val action: String
}

/**
 * Represents on start logging context.
 */
data class OnStartLogContext(
    override val method: Method,
    override val action: String,

    /**
     * Log level.
     */
    val level: Level,

    /**
     * Parameter list of the method.
     */
    val parameters: List<Any?> = emptyList(),
) : LogContext()

/**
 * Represents on finish logging context.
 */
data class OnFinishLogContext(
    override val method: Method,
    override val action: String,

    /**
     * Log level.
     */
    val level: Level,

    /**
     * Return value of the executed method. In case of Unit (void) is null.
     */
    val returnValue: Any? = null
) : LogContext()

/**
 * Represents on thrown logging context.
 */
data class OnThrowLogContext(
    override val method: Method,
    override val action: String,

    /**
     * Thrown exception from the method.
     */
    val exception: Throwable
) : LogContext()
