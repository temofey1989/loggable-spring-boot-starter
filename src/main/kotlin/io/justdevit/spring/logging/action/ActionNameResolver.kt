package io.justdevit.spring.logging.action

import java.lang.reflect.Method

/**
 * Represents resolver for action name.
 *
 * @see DefaultActionNameResolver
 */
interface ActionNameResolver {

    /**
     * Decides if the method is supported by this resolver.
     *
     * @param method Method to be checked.
     * @return true - in case of method is supported. Otherwise - false.
     */
    fun supports(method: Method): Boolean

    /**
     * Solve action name based on method.
     *
     * @param method Method for which action name should be solved.
     * @return Action name.
     */
    fun solveActionName(method: Method): String
}
