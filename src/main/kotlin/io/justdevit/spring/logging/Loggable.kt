package io.justdevit.spring.logging

import org.slf4j.event.Level
import org.slf4j.event.Level.INFO
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION

/**
 * Annotation works as a mark to provide logging of the action.
 * Used by the method interceptor to intercept method execution.
 */
@Target(FUNCTION, CLASS)
@Retention(RUNTIME)
annotation class Loggable(

    /**
     * Action name. If not defined, the default action name will be generated.
     */
    val action: String = "",

    /**
     * Exclude function from logging if current action is nested (default: false).
     */
    val nestable: Boolean = false,

    /**
     * Logging level (default: INFO).
     */
    val level: Level = INFO,

    /**
     * Exclude function parameters from logging (default: false).
     */
    val ignoreAllParameters: Boolean = false,

    /**
     * Exclude the return value from logging (default: false).
     */
    val ignoreReturnValue: Boolean = false,

    /**
     * Exclude exception from logging (default: false).
     */
    val ignoreThrows: Boolean = false,
)
