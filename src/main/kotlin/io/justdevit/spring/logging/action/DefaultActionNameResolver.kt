package io.justdevit.spring.logging.action

import org.springframework.core.Ordered.HIGHEST_PRECEDENCE
import org.springframework.core.annotation.Order
import java.lang.reflect.Method

/**
 * Default action name provider.
 *
 * Provides action name based on class and method names: '<simple-class-name>::<method-name>'.
 *
 * @see ActionNameResolver
 */
@Order(HIGHEST_PRECEDENCE)
class DefaultActionNameResolver : ActionNameResolver {

    override fun supports(method: Method) = true

    override fun solveActionName(method: Method) = "${method.declaringClass.simpleName}::${method.name}"
}
