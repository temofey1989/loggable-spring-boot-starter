package io.justdevit.spring.logging.action

import java.lang.reflect.Method

/**
 * Represents provider of the action name resolver.
 *
 * @see DefaultActionNameResolverProvider
 */
interface ActionNameResolverProvider {

    /**
     * Finds action name resolver based on method.
     *
     * @param method Method for finding action name resolver.
     * @return Action name resolver based on method.
     */
    fun findActionNameResolver(method: Method): ActionNameResolver
}
