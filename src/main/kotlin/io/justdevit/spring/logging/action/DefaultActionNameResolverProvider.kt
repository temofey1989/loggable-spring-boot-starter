package io.justdevit.spring.logging.action

import java.lang.reflect.Method

/**
 * Providers supported action name resolver.
 *
 * @param resolvers List of registered resolvers.
 */
class DefaultActionNameResolverProvider(private val resolvers: List<ActionNameResolver>) : ActionNameResolverProvider {

    /**
     * Fallback resolver if no supported resolvers.
     */
    private val fallbackResolver = DefaultActionNameResolver()

    override fun findActionNameResolver(method: Method) = resolvers.find { it.supports(method) } ?: fallbackResolver
}
