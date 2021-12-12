package io.justdevit.spring.logging

import io.justdevit.spring.logging.action.ActionNameResolverProvider
import io.justdevit.spring.logging.writer.LogWriterProvider
import io.justdevit.spring.logging.writer.OnFinishLogContext
import io.justdevit.spring.logging.writer.OnStartLogContext
import io.justdevit.spring.logging.writer.OnThrowLogContext
import org.aopalliance.intercept.MethodInvocation
import org.slf4j.MDC
import java.lang.reflect.Method

/**
 * Default implementation of the method interceptor for loggable methods.
 * Adds action attribute to the MDC on start of the operation and removes it in the end.
 *
 * @param logWriterProvider Log writer provider.
 * @param actionNameResolverProvider Provider for action name resolver if action is not defined in the annotation.
 *
 * @see LoggableMethodInterceptor
 */
class DefaultLoggableMethodInterceptor(
    private val actionNameResolverProvider: ActionNameResolverProvider,
    private val logWriterProvider: LogWriterProvider
) : LoggableMethodInterceptor {

    override fun invoke(invocation: MethodInvocation): Any? {
        val method = invocation.method
        val annotation = method.extractLoggableAnnotation()
        val previousAction = MDC.get(ACTION_ATTRIBUTE_NAME)
        if (previousAction != null && annotation.nestable) {
            return invocation.proceed()
        }
        val action = annotation.solveActionNameFor(method)
        val level = annotation.level
        val logWriter = logWriterProvider.findLogWriter(method)
        return try {
            MDC.put(ACTION_ATTRIBUTE_NAME, action)
            logWriter?.onStart(
                OnStartLogContext(
                    method = method,
                    action = action,
                    level = level,
                    parameters = if (annotation.ignoreAllParameters) emptyList() else invocation.arguments.toList()
                )
            )
            invocation.proceed()
                .also {
                    logWriter?.onFinish(
                        OnFinishLogContext(
                            method = method,
                            action = action,
                            level = level,
                            returnValue = if (annotation.ignoreReturnValue) null else it
                        )
                    )
                }
        } catch (exception: Throwable) {
            if (!annotation.ignoreThrows) {
                logWriter?.onThrow(
                    OnThrowLogContext(
                        method = method,
                        action = action,
                        exception = exception
                    )
                )
            }
            throw exception
        } finally {
            previousAction.reset()
        }
    }

    private fun Method.extractLoggableAnnotation() = getAnnotation(Loggable::class.java) ?: declaringClass.getAnnotation(Loggable::class.java)

    private fun Loggable.solveActionNameFor(method: Method) = action.ifBlank { actionNameResolverProvider.findActionNameResolver(method).solveActionName(method) }

    private fun String?.reset() {
        if (this != null) {
            MDC.put(ACTION_ATTRIBUTE_NAME, this)
        } else {
            MDC.remove(ACTION_ATTRIBUTE_NAME)
        }
    }
}
