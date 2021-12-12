package io.justdevit.spring.logging

import org.aopalliance.intercept.MethodInterceptor

/**
 * Method Interceptor to handle annotated methods.
 *
 * @see Loggable
 * @see DefaultLoggableMethodInterceptor
 */
interface LoggableMethodInterceptor : MethodInterceptor
