package io.justdevit.spring.logging.writer

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import org.slf4j.event.Level.DEBUG
import org.slf4j.event.Level.ERROR
import org.slf4j.event.Level.INFO
import org.slf4j.event.Level.TRACE
import org.slf4j.event.Level.WARN
import org.springframework.core.Ordered.LOWEST_PRECEDENCE
import org.springframework.core.annotation.Order
import java.lang.reflect.Method

/**
 * Implementation of the default log writer.
 *
 * @param actionLogResolver Action Log resolver.
 * @see LogWriter
 */
@Order(LOWEST_PRECEDENCE)
class DefaultLogWriter(private val actionLogResolver: ActionLogResolver) : LogWriter {

    override fun supports(method: Method) = true

    override fun onStart(context: OnStartLogContext) {
        context.method.logger.log(context.level, actionLogResolver.onStart(context))
    }

    override fun onFinish(context: OnFinishLogContext) {
        context.method.logger.log(context.level, actionLogResolver.onFinish(context))
    }

    override fun onThrow(context: OnThrowLogContext) {
        context.method.logger.log(ERROR, actionLogResolver.onThrow(context))
    }

    private val Method.logger: Logger
        get() = LoggerFactory.getLogger(this.declaringClass.name)

    private fun Logger.log(level: Level, actionLog: ActionLog) {
        val message = actionLog.message
        val args = actionLog.arguments.toTypedArray()
        when (level) {
            ERROR -> if (isErrorEnabled) error(message, *args)
            WARN -> if (isWarnEnabled) warn(message, *args)
            INFO -> if (isInfoEnabled) info(message, *args)
            DEBUG -> if (isDebugEnabled) debug(message, *args)
            TRACE -> if (isTraceEnabled) trace(message, *args)
        }
    }
}
