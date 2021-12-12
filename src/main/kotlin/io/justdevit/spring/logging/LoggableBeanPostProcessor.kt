package io.justdevit.spring.logging

import org.springframework.aop.framework.ProxyFactoryBean
import org.springframework.beans.factory.config.BeanPostProcessor
import java.lang.reflect.Method
import java.lang.reflect.Modifier.isFinal
import java.lang.reflect.Modifier.isPublic
import java.lang.reflect.Modifier.isStatic

/**
 * Bean Post Processor decides to wrap bean with Loggable interceptor if it is necessary.
 * The bean must fulfill the conditions for building a proxy object.
 *
 * @param interceptor Method interceptor for logging.
 * @see LoggableMethodInterceptor
 */
class LoggableBeanPostProcessor(
    private val interceptor: LoggableMethodInterceptor
) : BeanPostProcessor {

    override fun postProcessBeforeInitialization(bean: Any, beanName: String) =
        if (bean.isLoggable()) {
            val proxy = ProxyFactoryBean()
            proxy.setTarget(bean)
            proxy.addAdvice(interceptor)
            proxy.`object`
        } else {
            bean
        }

    private fun Any.isLoggable() = !isFinal(javaClass.modifiers)
            && (isAnnotatedClassWithPublicMethod() || hasAnnotatedMethod())

    private fun Any.isAnnotatedClassWithPublicMethod() =
        javaClass.isAnnotationPresent(Loggable::class.java)
                && javaClass.declaredMethods.any { it.accessible }

    private fun Any.hasAnnotatedMethod() = javaClass.declaredMethods.any {
        it.accessible && it.isAnnotationPresent(Loggable::class.java)
    }

    private val Method.accessible: Boolean
        get() = !isStatic(modifiers) && isPublic(modifiers) && !isFinal(modifiers)
}
