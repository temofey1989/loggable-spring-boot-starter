package io.justdevit.spring.logging.writer

/**
 * Represent a component to provide message based on action.
 *
 * @see ConsoleActionLogResolver
 */
interface ActionLogResolver {

    /**
     * Message on start of the action.
     *
     * @param context Before method execution context.
     * @return Instance of Action Log object.
     */
    fun onStart(context: OnStartLogContext): ActionLog

    /**
     * Message on finish of the action.
     *
     * @param context After method execution context.
     * @return Instance of Action Log object.
     */
    fun onFinish(context: OnFinishLogContext): ActionLog

    /**
     * Message on failed of the action.
     *
     * @param context After exception thrown context.
     * @return Instance of Action Log object.
     */
    fun onThrow(context: OnThrowLogContext): ActionLog
}
