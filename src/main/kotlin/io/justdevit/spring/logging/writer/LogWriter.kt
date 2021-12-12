package io.justdevit.spring.logging.writer

import java.lang.reflect.Method

/**
 * Represents log writer based on method.
 *
 * @see DefaultLogWriter
 */
interface LogWriter {

    /**
     * Decides if the method is supported by this writer.
     *
     * @param method Method to be checked.
     * @return true - in case of method is supported. Otherwise - false.
     */
    fun supports(method: Method): Boolean

    /**
     * Provides logging of the action before execution of the method.
     *
     * @param context Log context.
     */
    fun onStart(context: OnStartLogContext)

    /**
     * Provides logging of the action after successful execution of the method.
     *
     * @param context Log context.
     */
    fun onFinish(context: OnFinishLogContext)

    /**
     * Provides logging of the action on exception is thrown.
     *
     * @param context Log context.
     */
    fun onThrow(context: OnThrowLogContext)
}
